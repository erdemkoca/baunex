package ch.baunex.timetracking.service

import ch.baunex.timetracking.dto.EmployeeDailyWorkDTO
import ch.baunex.timetracking.dto.WeeklyWorkSummaryDTO
import ch.baunex.timetracking.model.ApprovalStatus
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.timetracking.repository.HolidayRepository
import ch.baunex.user.repository.EmployeeRepository
import ch.baunex.timetracking.mapper.TimeEntryMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.*

@ApplicationScoped
class WorkSummaryService @Inject constructor(
    private val timeEntryRepository: TimeEntryRepository,
    private val holidayRepository: HolidayRepository,
    private val employeeRepository: EmployeeRepository,
    private val timeEntryMapper: TimeEntryMapper
) {

    /**
     * Calculate expected working hours for a specific date
     * Default: 8 hours per working day (Monday-Friday)
     */
    fun calculateExpectedHours(employeeId: Long, date: LocalDate): Double {
        val employee = employeeRepository.findById(employeeId) ?: return 0.0
        
        // Check if it's a weekend
        if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
            return 0.0
        }
        
        // For now, use 8 hours per day as default
        // TODO: Could be enhanced with employee-specific contracts or public holiday detection
        return 8.0
    }

    /**
     * Get daily work summary for an employee in a date range
     */
    fun getDailyWorkSummary(employeeId: Long, from: LocalDate, to: LocalDate): List<EmployeeDailyWorkDTO> {
        val employee = employeeRepository.findById(employeeId) ?: return emptyList()
        val employeeName = "${employee.person.firstName} ${employee.person.lastName}"
        
        // Get time entries for the date range
        val timeEntries = timeEntryRepository.findByEmployeeAndDateRange(employeeId, from, to)
        val timeEntriesByDate = timeEntries.groupBy { it.date }
        
        // Get holidays for the date range
        val holidays = holidayRepository.findByEmployeeAndDateRange(employeeId, from, to)
        val holidaysByDate = holidays.groupBy { it.startDate }
        
        val result = mutableListOf<EmployeeDailyWorkDTO>()
        
        var currentDate = from
        while (!currentDate.isAfter(to)) {
            val workedHours = timeEntriesByDate[currentDate]?.sumOf { it.hoursWorked } ?: 0.0
            val expectedHours = calculateExpectedHours(employeeId, currentDate)
            val delta = workedHours - expectedHours
            
            // Check for holidays on this date
            val holiday = holidays.find { holiday ->
                currentDate >= holiday.startDate && currentDate <= holiday.endDate
            }
            
            val isWeekend = currentDate.dayOfWeek == DayOfWeek.SATURDAY || currentDate.dayOfWeek == DayOfWeek.SUNDAY
            
            result.add(
                EmployeeDailyWorkDTO(
                    employeeId = employeeId,
                    employeeName = employeeName,
                    date = currentDate,
                    workedHours = workedHours,
                    expectedHours = expectedHours,
                    delta = delta,
                    holidayType = holiday?.type?.name,
                    holidayApproved = holiday?.status == ApprovalStatus.APPROVED,
                    holidayReason = holiday?.reason,
                    timeEntries = timeEntriesByDate[currentDate]?.map { timeEntryMapper.toTimeEntryResponseDTO(it) } ?: emptyList(),
                    isWeekend = isWeekend,
                    isPublicHoliday = false // TODO: Implement public holiday detection
                )
            )
            
            currentDate = currentDate.plusDays(1)
        }
        
        return result
    }

    /**
     * Get weekly work summary for an employee
     */
    fun getWeeklyWorkSummary(employeeId: Long, year: Int, week: Int): WeeklyWorkSummaryDTO? {
        val employee = employeeRepository.findById(employeeId) ?: return null
        val employeeName = "${employee.person.firstName} ${employee.person.lastName}"
        
        // Calculate week start and end dates
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekStart = LocalDate.now().withYear(year).with(weekFields.weekOfYear(), week.toLong())
            .with(weekFields.dayOfWeek(), 1L) // Monday
        val weekEnd = weekStart.plusDays(6) // Sunday
        
        val dailySummaries = getDailyWorkSummary(employeeId, weekStart, weekEnd)
        
        val totalWorked = dailySummaries.sumOf { it.workedHours }
        val totalExpected = dailySummaries.sumOf { it.expectedHours }
        val overtime = dailySummaries.sumOf { if (it.delta > 0) it.delta else 0.0 }
        val undertime = dailySummaries.sumOf { if (it.delta < 0) -it.delta else 0.0 }
        
        val holidayDays = dailySummaries.count { it.holidayType != null && it.holidayApproved == true }
        val pendingHolidayRequests = dailySummaries.count { it.holidayType != null && it.holidayApproved == false }
        
        return WeeklyWorkSummaryDTO(
            employeeId = employeeId,
            employeeName = employeeName,
            weekStart = weekStart,
            weekEnd = weekEnd,
            totalWorked = totalWorked,
            totalExpected = totalExpected,
            overtime = overtime,
            undertime = undertime,
            holidayDays = holidayDays,
            pendingHolidayRequests = pendingHolidayRequests,
            dailySummaries = dailySummaries
        )
    }

    /**
     * Get daily work summary for all employees in a date range
     */
    fun getAllEmployeesDailyWorkSummary(from: LocalDate, to: LocalDate): List<EmployeeDailyWorkDTO> {
        val employees = employeeRepository.listAll()
        return employees.flatMap { employee ->
            getDailyWorkSummary(employee.id!!, from, to)
        }
    }

    /**
     * Get weekly work summary for all employees
     */
    fun getAllEmployeesWeeklyWorkSummary(year: Int, week: Int): List<WeeklyWorkSummaryDTO> {
        val employees = employeeRepository.listAll()
        return employees.mapNotNull { employee ->
            getWeeklyWorkSummary(employee.id!!, year, week)
        }
    }
} 