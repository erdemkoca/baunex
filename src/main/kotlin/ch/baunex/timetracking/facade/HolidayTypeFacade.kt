package ch.baunex.timetracking.facade

import ch.baunex.timetracking.dto.HolidayTypeDTO
import ch.baunex.timetracking.dto.HolidayTypeCreateDTO
import ch.baunex.timetracking.dto.HolidayTypeUpdateDTO
import ch.baunex.timetracking.dto.HolidayTypeListDTO
import ch.baunex.timetracking.service.HolidayTypeService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class HolidayTypeFacade @Inject constructor(
    private val holidayTypeService: HolidayTypeService
) {

    /**
     * Get all active holiday types for frontend consumption
     */
    fun getActiveHolidayTypes(): List<HolidayTypeDTO> {
        return holidayTypeService.getActiveHolidayTypes()
    }

    /**
     * Get all holiday types (for admin interface)
     */
    fun getAllHolidayTypes(): List<HolidayTypeDTO> {
        return holidayTypeService.getAllHolidayTypes()
    }

    /**
     * Get holiday type by ID
     */
    fun getHolidayTypeById(id: Long): HolidayTypeDTO? {
        return holidayTypeService.getHolidayTypeById(id)
    }

    /**
     * Get holiday type by code
     */
    fun getHolidayTypeByCode(code: String): HolidayTypeDTO? {
        return holidayTypeService.getHolidayTypeByCode(code)
    }

    /**
     * Get expected hours for a holiday type code
     */
    fun getExpectedHoursForHolidayType(holidayTypeCode: String?): Double {
        return holidayTypeService.getExpectedHoursForHolidayType(holidayTypeCode)
    }

    /**
     * Get default workday hours
     */
    fun getDefaultWorkdayHours(): Double {
        return holidayTypeService.getDefaultWorkdayHours()
    }

    /**
     * Create a new holiday type
     */
    fun createHolidayType(dto: HolidayTypeCreateDTO): HolidayTypeDTO {
        return holidayTypeService.createHolidayType(dto)
    }

    /**
     * Update an existing holiday type
     */
    fun updateHolidayType(id: Long, dto: HolidayTypeUpdateDTO): HolidayTypeDTO? {
        return holidayTypeService.updateHolidayType(id, dto)
    }

    /**
     * Delete a holiday type (soft delete)
     */
    fun deleteHolidayType(id: Long): Boolean {
        return holidayTypeService.deleteHolidayType(id)
    }

    /**
     * Activate a holiday type
     */
    fun activateHolidayType(id: Long): HolidayTypeDTO? {
        return holidayTypeService.activateHolidayType(id)
    }

    /**
     * Deactivate a holiday type
     */
    fun deactivateHolidayType(id: Long): HolidayTypeDTO? {
        return holidayTypeService.deactivateHolidayType(id)
    }

    /**
     * Get paginated holiday types
     */
    fun getHolidayTypesPaginated(page: Int, size: Int): HolidayTypeListDTO {
        return holidayTypeService.getHolidayTypesPaginated(page, size)
    }
} 