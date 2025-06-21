package ch.baunex.timetracking.service

import ch.baunex.timetracking.model.HolidayModel
import ch.baunex.timetracking.model.ApprovalStatus
import ch.baunex.timetracking.model.HolidayType
import ch.baunex.timetracking.repository.HolidayRepository
import ch.baunex.user.repository.EmployeeRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate

@ApplicationScoped
class HolidayService @Inject constructor(
    private val holidayRepository: HolidayRepository,
    private val employeeRepository: EmployeeRepository
) {

    @Transactional
    fun createHoliday(model: HolidayModel, employeeId: Long): HolidayModel {
        val employee = employeeRepository.findById(employeeId)
            ?: throw IllegalArgumentException("Employee not found with id: $employeeId")
        model.employee = employee
        model.status = ApprovalStatus.PENDING
        holidayRepository.persist(model)
        return model
    }

    fun getHolidaysForEmployee(employeeId: Long): List<HolidayModel> {
        return holidayRepository.list("employee.id", employeeId)
    }

    fun getAllHolidays(): List<HolidayModel> {
        return holidayRepository.listAll()
    }

    @Transactional
    fun approveHoliday(holidayId: Long, approverId: Long, approved: Boolean): HolidayModel? {
        val holiday = holidayRepository.findById(holidayId) ?: return null
        holiday.status = if (approved) ApprovalStatus.APPROVED else ApprovalStatus.REJECTED
        return holiday
    }
}
