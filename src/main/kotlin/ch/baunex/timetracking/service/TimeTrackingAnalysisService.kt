package ch.baunex.timetracking.service

import ch.baunex.user.model.EmployeeModel
import ch.baunex.timetracking.model.HolidayModel
import ch.baunex.timetracking.repository.HolidayRepository
import ch.baunex.timetracking.repository.TimeEntryRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.YearMonth

@ApplicationScoped
class TimeTrackingAnalysisService @Inject constructor(
    private val timeEntryRepository: TimeEntryRepository,
    private val holidayRepository: HolidayRepository
) {

    fun calculateMonthlyWorkedHours(employee: EmployeeModel, month: YearMonth): Double {
        val start = month.atDay(1)
        val end = month.atEndOfMonth()

        return timeEntryRepository.findByEmployeeAndDateRange(employee.id!!, start, end)
            .sumOf { it.hoursWorked }
    }

    fun calculatePlannedMonthlyHours(employee: EmployeeModel, month: YearMonth): Double {
        val weeksInMonth = month.lengthOfMonth() / 7.0
        return employee.plannedWeeklyHours * weeksInMonth
    }

    fun calculateBalance(employee: EmployeeModel, month: YearMonth): Double {
        val worked = calculateMonthlyWorkedHours(employee, month)
        val planned = calculatePlannedMonthlyHours(employee, month)
        return worked - planned
    }

    fun getHolidaysInMonth(employee: EmployeeModel, month: YearMonth): List<HolidayModel> {
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        return holidayRepository.findByEmployeeAndDateRange(employee.id!!, start, end)
    }
}
