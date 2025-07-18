package ch.baunex.timetracking.facade

import ch.baunex.timetracking.dto.HolidayTypeDTO
import ch.baunex.timetracking.dto.HolidayTypeCreateDTO
import ch.baunex.timetracking.dto.HolidayTypeUpdateDTO
import ch.baunex.timetracking.dto.HolidayTypeListDTO
import ch.baunex.timetracking.service.HolidayTypeService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger

@ApplicationScoped
class HolidayTypeFacade @Inject constructor(
    private val holidayTypeService: HolidayTypeService
) {
    private val log = Logger.getLogger(HolidayTypeFacade::class.java)

    /**
     * Get all active holiday types for frontend consumption
     */
    fun getActiveHolidayTypes(): List<HolidayTypeDTO> {
        log.debug("Getting active holiday types")
        return holidayTypeService.getActiveHolidayTypes()
    }

    /**
     * Get all holiday types (for admin interface)
     */
    fun getAllHolidayTypes(): List<HolidayTypeDTO> {
        log.debug("Getting all holiday types")
        return holidayTypeService.getAllHolidayTypes()
    }

    /**
     * Get holiday type by ID
     */
    fun getHolidayTypeById(id: Long): HolidayTypeDTO? {
        log.debug("Getting holiday type by ID: $id")
        return holidayTypeService.getHolidayTypeById(id)
    }

    /**
     * Get holiday type by code
     */
    fun getHolidayTypeByCode(code: String): HolidayTypeDTO? {
        log.debug("Getting holiday type by code: $code")
        return holidayTypeService.getHolidayTypeByCode(code)
    }

    /**
     * Get expected hours for a holiday type code
     */
    fun getExpectedHoursForHolidayType(holidayTypeCode: String?): Double {
        log.debug("Getting expected hours for holiday type code: $holidayTypeCode")
        return holidayTypeService.getExpectedHoursForHolidayType(holidayTypeCode)
    }

    /**
     * Get expected hours for a holiday type code considering employee's plannedWeeklyHours
     */
    fun getExpectedHoursForHolidayType(holidayTypeCode: String?, employeeId: Long?): Double {
        log.debug("Getting expected hours for holiday type code: $holidayTypeCode, employee: $employeeId")
        return holidayTypeService.getExpectedHoursForHolidayType(holidayTypeCode, employeeId)
    }

    /**
     * Get default workday hours
     */
    fun getDefaultWorkdayHours(): Double {
        log.debug("Getting default workday hours")
        return holidayTypeService.getDefaultWorkdayHours()
    }

    /**
     * Create a new holiday type
     */
    fun createHolidayType(dto: HolidayTypeCreateDTO): HolidayTypeDTO {
        log.info("Creating holiday type with code: ${dto.code}")
        return holidayTypeService.createHolidayType(dto)
    }

    /**
     * Update an existing holiday type
     */
    fun updateHolidayType(id: Long, dto: HolidayTypeUpdateDTO): HolidayTypeDTO? {
        log.info("Updating holiday type with ID: $id")
        return holidayTypeService.updateHolidayType(id, dto)
    }

    /**
     * Delete a holiday type (soft delete)
     */
    fun deleteHolidayType(id: Long): Boolean {
        log.info("Deleting holiday type with ID: $id")
        return holidayTypeService.deleteHolidayType(id)
    }

    /**
     * Activate a holiday type
     */
    fun activateHolidayType(id: Long): HolidayTypeDTO? {
        log.info("Activating holiday type with ID: $id")
        return holidayTypeService.activateHolidayType(id)
    }

    /**
     * Deactivate a holiday type
     */
    fun deactivateHolidayType(id: Long): HolidayTypeDTO? {
        log.info("Deactivating holiday type with ID: $id")
        return holidayTypeService.deactivateHolidayType(id)
    }

    /**
     * Get paginated holiday types
     */
    fun getHolidayTypesPaginated(page: Int, size: Int): HolidayTypeListDTO {
        log.debug("Getting paginated holiday types - page: $page, size: $size")
        return holidayTypeService.getHolidayTypesPaginated(page, size)
    }
} 