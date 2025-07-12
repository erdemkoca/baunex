package ch.baunex.timetracking.facade

import ch.baunex.timetracking.dto.HolidayDTO
import ch.baunex.timetracking.dto.HolidayApprovalDTO
import ch.baunex.timetracking.mapper.HolidayMapper
import ch.baunex.timetracking.service.HolidayService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class HolidayFacade @Inject constructor(
    private val holidayService: HolidayService,
    private val holidayMapper: HolidayMapper
) {

    @Transactional
    fun requestHoliday(dto: HolidayDTO): HolidayDTO {
        val model = holidayMapper.toModel(dto)
        val saved = holidayService.createHoliday(model, dto.employeeId)
        return holidayMapper.toDTO(saved)
    }

    fun getAllHolidays(): List<HolidayDTO> {
        return holidayService.getAllHolidays().map { holidayMapper.toDTO(it) }
    }

    fun getHolidaysForEmployee(employeeId: Long): List<HolidayDTO> {
        return holidayService.getHolidaysForEmployee(employeeId).map { holidayMapper.toDTO(it) }
    }

    @Transactional
    fun approveHoliday(holidayId: Long, dto: HolidayApprovalDTO): HolidayDTO? {
        val updated = holidayService.approveHoliday(
            holidayId = holidayId,
            approverId = dto.approval.approverId ?: throw IllegalArgumentException("Approver ID is required"),
            approvalStatus = dto.approval.status
        ) ?: return null

        return holidayMapper.toDTO(updated)
    }
}
