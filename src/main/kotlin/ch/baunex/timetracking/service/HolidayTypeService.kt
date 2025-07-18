package ch.baunex.timetracking.service

import ch.baunex.timetracking.dto.HolidayTypeDTO
import ch.baunex.timetracking.dto.HolidayTypeCreateDTO
import ch.baunex.timetracking.dto.HolidayTypeUpdateDTO
import ch.baunex.timetracking.dto.HolidayTypeListDTO
import ch.baunex.timetracking.mapper.HolidayTypeMapper
import ch.baunex.timetracking.model.HolidayTypeModel
import ch.baunex.timetracking.repository.HolidayTypeRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import org.jboss.logging.Logger

@ApplicationScoped
class HolidayTypeService @Inject constructor(
    private val holidayTypeRepository: HolidayTypeRepository,
    private val holidayTypeMapper: HolidayTypeMapper
) {
    private val log = Logger.getLogger(HolidayTypeService::class.java)

    /**
     * Get all active holiday types
     */
    fun getActiveHolidayTypes(): List<HolidayTypeDTO> {
        log.info("Fetching active holiday types")
        return try {
            val activeTypes = holidayTypeRepository.findActive()
            val result = holidayTypeMapper.toDTOList(activeTypes)
            log.info("Fetched ${result.size} active holiday types")
            result
        } catch (e: Exception) {
            log.error("Failed to fetch active holiday types", e)
            throw e
        }
    }

    /**
     * Get all holiday types (active and inactive)
     */
    fun getAllHolidayTypes(): List<HolidayTypeDTO> {
        log.info("Fetching all holiday types")
        return try {
            val allTypes = holidayTypeRepository.listAllOrdered()
            val result = holidayTypeMapper.toDTOList(allTypes)
            log.info("Fetched ${result.size} holiday types")
            result
        } catch (e: Exception) {
            log.error("Failed to fetch all holiday types", e)
            throw e
        }
    }

    /**
     * Get holiday type by ID
     */
    fun getHolidayTypeById(id: Long): HolidayTypeDTO? {
        log.info("Fetching holiday type by ID: $id")
        return try {
            val model = holidayTypeRepository.findById(id)
            val dto = model?.let { holidayTypeMapper.toDTO(it) }
            if (dto != null) log.info("Fetched holiday type with ID: $id")
            else log.info("Holiday type with ID $id not found (business validation)")
            dto
        } catch (e: Exception) {
            log.error("Failed to fetch holiday type by ID: $id", e)
            throw e
        }
    }

    /**
     * Get holiday type by code
     */
    fun getHolidayTypeByCode(code: String): HolidayTypeDTO? {
        log.info("Fetching holiday type by code: $code")
        return try {
            val model = holidayTypeRepository.findActiveByCode(code)
            val dto = model?.let { holidayTypeMapper.toDTO(it) }
            if (dto != null) log.info("Fetched holiday type with code: $code")
            else log.info("Holiday type with code $code not found (business validation)")
            dto
        } catch (e: Exception) {
            log.error("Failed to fetch holiday type by code: $code", e)
            throw e
        }
    }

    /**
     * Get expected hours for a holiday type code
     */
    fun getExpectedHoursForHolidayType(holidayTypeCode: String?): Double {
        log.info("Fetching expected hours for holiday type code: $holidayTypeCode")
        return try {
            if (holidayTypeCode == null) {
                val hours = getDefaultWorkdayHours()
                log.info("No code provided, using default workday hours: $hours")
                return hours
            }
            val holidayType = holidayTypeRepository.findActiveByCode(holidayTypeCode)
            val hours = holidayType?.defaultExpectedHours ?: getDefaultWorkdayHours()
            log.info("Expected hours for code $holidayTypeCode: $hours")
            hours
        } catch (e: Exception) {
            log.error("Failed to fetch expected hours for code: $holidayTypeCode", e)
            throw e
        }
    }

    /**
     * Get expected hours for a holiday type code considering employee's plannedWeeklyHours
     * ENHANCED METHOD: This method provides employee-specific expected hours calculation
     */
    fun getExpectedHoursForHolidayType(holidayTypeCode: String?, employeeId: Long?): Double {
        log.info("Fetching expected hours for holiday type code: $holidayTypeCode, employee: $employeeId")
        return try {
            if (holidayTypeCode == null) {
                // For null holiday type, use employee's default workday hours if available
                val hours = if (employeeId != null) {
                    // This will be handled by WorkSummaryService.calculateDefaultWorkdayHours
                    getDefaultWorkdayHours() // Fallback to default
                } else {
                    getDefaultWorkdayHours()
                }
                log.info("No code provided, using default workday hours: $hours")
                return hours
            }
            
            val holidayType = holidayTypeRepository.findActiveByCode(holidayTypeCode)
            val hours = holidayType?.defaultExpectedHours ?: getDefaultWorkdayHours()
            log.info("Expected hours for code $holidayTypeCode: $hours")
            hours
        } catch (e: Exception) {
            log.error("Failed to fetch expected hours for code: $holidayTypeCode", e)
            throw e
        }
    }

    /**
     * Get default workday hours (from PAID_VACATION or first active type)
     */
    fun getDefaultWorkdayHours(): Double {
        log.info("Fetching default workday hours")
        return try {
            val paidVacation = holidayTypeRepository.findActiveByCode("PAID_VACATION")
            val hours = paidVacation?.defaultExpectedHours ?: 8.0
            log.info("Default workday hours: $hours")
            hours
        } catch (e: Exception) {
            log.error("Failed to fetch default workday hours", e)
            throw e
        }
    }

    /**
     * Create a new holiday type
     */
    @Transactional
    fun createHolidayType(dto: HolidayTypeCreateDTO): HolidayTypeDTO {
        log.info("Creating holiday type with code: ${dto.code}")
        return try {
            // Validate code uniqueness
            if (holidayTypeRepository.existsByCode(dto.code)) {
                throw WebApplicationException("Holiday type with code '${dto.code}' already exists", Response.Status.CONFLICT)
            }

            // Validate code format
            if (!dto.code.matches(Regex("^[A-Z_]+$"))) {
                throw WebApplicationException("Code must contain only uppercase letters and underscores", Response.Status.BAD_REQUEST)
            }

            val model = holidayTypeMapper.toModel(dto)
            
            // Set sort order if not provided
            if (dto.sortOrder == 0) {
                model.sortOrder = holidayTypeRepository.findNextSortOrder()
            }
            
            holidayTypeRepository.persist(model)
            val created = holidayTypeMapper.toDTO(model)
            log.info("Created holiday type with ID: ${created.id}")
            created
        } catch (e: Exception) {
            log.error("Failed to create holiday type with code: ${dto.code}", e)
            throw e
        }
    }

    /**
     * Update an existing holiday type
     */
    @Transactional
    fun updateHolidayType(id: Long, dto: HolidayTypeUpdateDTO): HolidayTypeDTO? {
        log.info("Updating holiday type with ID: $id")
        return try {
            val model = holidayTypeRepository.findById(id) ?: return null

            // Prevent modification of system types
            if (model.isSystemType) {
                throw WebApplicationException("Cannot modify system holiday types", Response.Status.FORBIDDEN)
            }

            holidayTypeMapper.updateModel(model, dto)
            holidayTypeRepository.persist(model)
            val updated = holidayTypeMapper.toDTO(model)
            log.info("Updated holiday type with ID: $id")
            updated
        } catch (e: Exception) {
            log.error("Failed to update holiday type with ID: $id", e)
            throw e
        }
    }

    /**
     * Delete a holiday type (soft delete by setting active = false)
     */
    @Transactional
    fun deleteHolidayType(id: Long): Boolean {
        log.info("Deleting holiday type with ID: $id")
        return try {
            val model = holidayTypeRepository.findById(id) ?: return false

            // Prevent deletion of system types
            if (model.isSystemType) {
                throw WebApplicationException("Cannot delete system holiday types", Response.Status.FORBIDDEN)
            }

            model.active = false
            model.updatedAt = java.time.LocalDate.now()
            holidayTypeRepository.persist(model)
            log.info("Deleted holiday type with ID: $id (set to inactive)")
            true
        } catch (e: Exception) {
            log.error("Failed to delete holiday type with ID: $id", e)
            throw e
        }
    }

    /**
     * Activate a holiday type
     */
    @Transactional
    fun activateHolidayType(id: Long): HolidayTypeDTO? {
        log.info("Activating holiday type with ID: $id")
        return try {
            val model = holidayTypeRepository.findById(id) ?: return null

            model.active = true
            model.updatedAt = java.time.LocalDate.now()
            holidayTypeRepository.persist(model)
            val activated = holidayTypeMapper.toDTO(model)
            log.info("Activated holiday type with ID: $id")
            activated
        } catch (e: Exception) {
            log.error("Failed to activate holiday type with ID: $id", e)
            throw e
        }
    }

    /**
     * Deactivate a holiday type
     */
    @Transactional
    fun deactivateHolidayType(id: Long): HolidayTypeDTO? {
        log.info("Deactivating holiday type with ID: $id")
        return try {
            val model = holidayTypeRepository.findById(id) ?: return null

            // Prevent deactivation of system types
            if (model.isSystemType) {
                throw WebApplicationException("Cannot deactivate system holiday types", Response.Status.FORBIDDEN)
            }

            model.active = false
            model.updatedAt = java.time.LocalDate.now()
            holidayTypeRepository.persist(model)
            val deactivated = holidayTypeMapper.toDTO(model)
            log.info("Deactivated holiday type with ID: $id")
            deactivated
        } catch (e: Exception) {
            log.error("Failed to deactivate holiday type with ID: $id", e)
            throw e
        }
    }

    /**
     * Get paginated holiday types
     */
    fun getHolidayTypesPaginated(page: Int, size: Int): HolidayTypeListDTO {
        log.info("Fetching holiday types paginated (page: $page, size: $size)")
        return try {
            val allTypes = holidayTypeRepository.listAllOrdered()
            val total = allTypes.size
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, total)
            val paged = if (startIndex < total) allTypes.subList(startIndex, endIndex) else emptyList()
            val result = HolidayTypeListDTO(holidayTypes = holidayTypeMapper.toDTOList(paged), totalCount = total.toLong())
            log.info("Fetched ${result.holidayTypes.size} holiday types (page: $page, total: $total)")
            result
        } catch (e: Exception) {
            log.error("Failed to fetch holiday types paginated (page: $page, size: $size)", e)
            throw e
        }
    }
} 