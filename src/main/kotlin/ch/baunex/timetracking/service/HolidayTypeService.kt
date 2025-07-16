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

@ApplicationScoped
class HolidayTypeService @Inject constructor(
    private val holidayTypeRepository: HolidayTypeRepository,
    private val holidayTypeMapper: HolidayTypeMapper
) {

    /**
     * Get all active holiday types
     */
    fun getActiveHolidayTypes(): List<HolidayTypeDTO> {
        val activeTypes = holidayTypeRepository.findActive()
        return holidayTypeMapper.toDTOList(activeTypes)
    }

    /**
     * Get all holiday types (active and inactive)
     */
    fun getAllHolidayTypes(): List<HolidayTypeDTO> {
        val allTypes = holidayTypeRepository.listAllOrdered()
        return holidayTypeMapper.toDTOList(allTypes)
    }

    /**
     * Get holiday type by ID
     */
    fun getHolidayTypeById(id: Long): HolidayTypeDTO? {
        val model = holidayTypeRepository.findById(id)
        return model?.let { holidayTypeMapper.toDTO(it) }
    }

    /**
     * Get holiday type by code
     */
    fun getHolidayTypeByCode(code: String): HolidayTypeDTO? {
        val model = holidayTypeRepository.findActiveByCode(code)
        return model?.let { holidayTypeMapper.toDTO(it) }
    }

    /**
     * Get expected hours for a holiday type code
     */
    fun getExpectedHoursForHolidayType(holidayTypeCode: String?): Double {
        if (holidayTypeCode == null) {
            return getDefaultWorkdayHours()
        }
        
        val holidayType = holidayTypeRepository.findActiveByCode(holidayTypeCode)
        return holidayType?.defaultExpectedHours ?: getDefaultWorkdayHours()
    }

    /**
     * Get default workday hours (from PAID_VACATION or first active type)
     */
    fun getDefaultWorkdayHours(): Double {
        val paidVacation = holidayTypeRepository.findActiveByCode("PAID_VACATION")
        return paidVacation?.defaultExpectedHours ?: 8.0
    }

    /**
     * Create a new holiday type
     */
    @Transactional
    fun createHolidayType(dto: HolidayTypeCreateDTO): HolidayTypeDTO {
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
        return holidayTypeMapper.toDTO(model)
    }

    /**
     * Update an existing holiday type
     */
    @Transactional
    fun updateHolidayType(id: Long, dto: HolidayTypeUpdateDTO): HolidayTypeDTO? {
        val model = holidayTypeRepository.findById(id)
            ?: throw WebApplicationException("Holiday type not found", Response.Status.NOT_FOUND)

        // Prevent modification of system types
        if (model.isSystemType) {
            throw WebApplicationException("Cannot modify system holiday types", Response.Status.FORBIDDEN)
        }

        holidayTypeMapper.updateModel(model, dto)
        holidayTypeRepository.persist(model)
        return holidayTypeMapper.toDTO(model)
    }

    /**
     * Delete a holiday type (soft delete by setting active = false)
     */
    @Transactional
    fun deleteHolidayType(id: Long): Boolean {
        val model = holidayTypeRepository.findById(id)
            ?: throw WebApplicationException("Holiday type not found", Response.Status.NOT_FOUND)

        // Prevent deletion of system types
        if (model.isSystemType) {
            throw WebApplicationException("Cannot delete system holiday types", Response.Status.FORBIDDEN)
        }

        model.active = false
        model.updatedAt = java.time.LocalDate.now()
        holidayTypeRepository.persist(model)
        return true
    }

    /**
     * Activate a holiday type
     */
    @Transactional
    fun activateHolidayType(id: Long): HolidayTypeDTO? {
        val model = holidayTypeRepository.findById(id)
            ?: throw WebApplicationException("Holiday type not found", Response.Status.NOT_FOUND)

        model.active = true
        model.updatedAt = java.time.LocalDate.now()
        holidayTypeRepository.persist(model)
        return holidayTypeMapper.toDTO(model)
    }

    /**
     * Deactivate a holiday type
     */
    @Transactional
    fun deactivateHolidayType(id: Long): HolidayTypeDTO? {
        val model = holidayTypeRepository.findById(id)
            ?: throw WebApplicationException("Holiday type not found", Response.Status.NOT_FOUND)

        // Prevent deactivation of system types
        if (model.isSystemType) {
            throw WebApplicationException("Cannot deactivate system holiday types", Response.Status.FORBIDDEN)
        }

        model.active = false
        model.updatedAt = java.time.LocalDate.now()
        holidayTypeRepository.persist(model)
        return holidayTypeMapper.toDTO(model)
    }

    /**
     * Get paginated holiday types
     */
    fun getHolidayTypesPaginated(page: Int, size: Int): HolidayTypeListDTO {
        val allTypes = holidayTypeRepository.listAllOrdered()
        val totalCount = allTypes.size
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, totalCount)
        val types = if (startIndex < totalCount) allTypes.subList(startIndex, endIndex) else emptyList()
        
        return HolidayTypeListDTO(
            holidayTypes = holidayTypeMapper.toDTOList(types),
            totalCount = totalCount.toLong()
        )
    }
} 