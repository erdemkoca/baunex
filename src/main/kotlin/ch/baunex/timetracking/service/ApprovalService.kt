package ch.baunex.timetracking.service

import ch.baunex.timetracking.dto.WeeklyApprovalDTO
import ch.baunex.timetracking.dto.VacationApprovalDTO
import ch.baunex.timetracking.model.ApprovalStatus
import ch.baunex.timetracking.model.HolidayModel
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.repository.HolidayRepository
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.user.model.EmployeeModel
import ch.baunex.user.repository.EmployeeRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.*

@ApplicationScoped
class ApprovalService @Inject constructor(
    private val timeEntryRepository: TimeEntryRepository,
    private val holidayRepository: HolidayRepository,
    private val employeeRepository: EmployeeRepository,
    private val timeEntryMapper: ch.baunex.timetracking.mapper.TimeEntryMapper
) {

    /**
     * Approve a single time entry
     */
    @Transactional
    fun approveTimeEntry(entryId: Long, approverId: Long): Boolean {
        val entry = timeEntryRepository.findById(entryId) ?: return false
        val approver = employeeRepository.findById(approverId) ?: return false

        entry.approvalStatus = ApprovalStatus.APPROVED
        entry.approvedBy = approver
        entry.approvedAt = LocalDate.now()
        
        return true
    }

    /**
     * Approve all time entries for an employee in a specific week
     */
    @Transactional
    fun approveWeeklyEntries(employeeId: Long, year: Int, week: Int, approverId: Long): Boolean {
        val approver = employeeRepository.findById(approverId) ?: return false
        
        // Calculate week start and end dates
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekStart = LocalDate.now().withYear(year).with(weekFields.weekOfYear(), week.toLong())
            .with(weekFields.dayOfWeek(), 1L) // Monday
        val weekEnd = weekStart.plusDays(6) // Sunday
        
        // Get all time entries for the employee in the week
        val entries = timeEntryRepository.findByEmployeeAndDateRange(employeeId, weekStart, weekEnd)
        
        // Approve all entries
        entries.forEach { entry ->
            entry.approvalStatus = ApprovalStatus.APPROVED
            entry.approvedBy = approver
            entry.approvedAt = LocalDate.now()
        }
        
        return entries.isNotEmpty()
    }

    /**
     * Approve a vacation request
     */
    @Transactional
    fun approveVacation(vacationId: Long, approverId: Long): Boolean {
        val vacation = holidayRepository.findById(vacationId) ?: return false
        val approver = employeeRepository.findById(approverId) ?: return false

        vacation.approvalStatus = ApprovalStatus.APPROVED
        vacation.approvedBy = approver
        vacation.approvedAt = LocalDate.now()
        
        return true
    }

    /**
     * Reject a vacation request
     */
    @Transactional
    fun rejectVacation(vacationId: Long, approverId: Long, reason: String? = null): Boolean {
        val vacation = holidayRepository.findById(vacationId) ?: return false
        val approver = employeeRepository.findById(approverId) ?: return false

        vacation.approvalStatus = ApprovalStatus.REJECTED
        vacation.approvedBy = approver
        vacation.approvedAt = LocalDate.now()
        if (reason != null) {
            vacation.reason = reason
        }
        
        return true
    }

    /**
     * Get weekly approval summary for an employee
     */
    fun getWeeklyApprovalSummary(employeeId: Long, year: Int, week: Int): WeeklyApprovalDTO? {
        val employee = employeeRepository.findById(employeeId) ?: return null
        
        // Calculate week start and end dates
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekStart = LocalDate.now().withYear(year).with(weekFields.weekOfYear(), week.toLong())
            .with(weekFields.dayOfWeek(), 1L) // Monday
        val weekEnd = weekStart.plusDays(6) // Sunday
        
        // Get all time entries for the employee in the week
        val entries = timeEntryRepository.findByEmployeeAndDateRange(employeeId, weekStart, weekEnd)
        
        val totalEntries = entries.size
        val pendingEntries = entries.count { it.approvalStatus == ApprovalStatus.PENDING }
        val approvedEntries = entries.count { it.approvalStatus == ApprovalStatus.APPROVED }
        val totalHours = entries.sumOf { it.hoursWorked }
        
        // Determine overall approval status
        val approvalStatus = when {
            approvedEntries == totalEntries -> "APPROVED"
            pendingEntries == totalEntries -> "PENDING"
            else -> "PARTIAL"
        }
        
        return WeeklyApprovalDTO(
            employeeId = employeeId,
            employeeName = "${employee.person.firstName} ${employee.person.lastName}",
            weekStart = weekStart,
            weekEnd = weekEnd,
            totalEntries = totalEntries,
            pendingEntries = pendingEntries,
            approvedEntries = approvedEntries,
            totalHours = totalHours,
            approvalStatus = approvalStatus,
            approverId = null, // Would need to track weekly approval
            approverName = null,
            approvedAt = null,
            entries = entries.map { timeEntryMapper.toTimeEntryResponseDTO(it) }
        )
    }

    /**
     * Get all pending vacation requests
     */
    fun getPendingVacationRequests(): List<VacationApprovalDTO> {
        return holidayRepository.findByStatus(ApprovalStatus.PENDING)
            .map { holiday ->
                val workingDays = calculateWorkingDays(holiday.startDate, holiday.endDate)
                VacationApprovalDTO(
                    id = holiday.id!!,
                    employeeId = holiday.employee.id!!,
                    employeeName = "${holiday.employee.person.firstName} ${holiday.employee.person.lastName}",
                    startDate = holiday.startDate,
                    endDate = holiday.endDate,
                    type = holiday.type.displayName,
                    reason = holiday.reason,
                    approvalStatus = holiday.approvalStatus.name,
                    approverId = holiday.approvedBy?.id,
                    approverName = holiday.approvedBy?.let { "${it.person.firstName} ${it.person.lastName}" },
                    approvedAt = holiday.approvedAt,
                    workingDays = workingDays
                )
            }
    }

    /**
     * Calculate working days between two dates (excluding weekends)
     */
    private fun calculateWorkingDays(startDate: LocalDate, endDate: LocalDate): Int {
        var workingDays = 0
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            if (currentDate.dayOfWeek != DayOfWeek.SATURDAY && currentDate.dayOfWeek != DayOfWeek.SUNDAY) {
                workingDays++
            }
            currentDate = currentDate.plusDays(1)
        }
        return workingDays
    }
} 