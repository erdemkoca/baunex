package ch.baunex.timetracking.service

import ch.baunex.timetracking.dto.EmployeeDailyWorkDTO
import ch.baunex.timetracking.dto.WeeklyWorkSummaryDTO
import ch.baunex.timetracking.dto.CumulativeHoursAccountDTO
import ch.baunex.timetracking.dto.MonthlyHoursAccountDTO
import ch.baunex.timetracking.dto.MonthDataDTO
import ch.baunex.timetracking.dto.WeekDataDTO
import ch.baunex.timetracking.dto.DayDataDTO
import ch.baunex.timetracking.model.ApprovalStatus
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.timetracking.repository.HolidayRepository
import ch.baunex.user.repository.EmployeeRepository
import ch.baunex.timetracking.mapper.TimeEntryMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*
import org.jboss.logging.Logger

@ApplicationScoped
class WorkSummaryService @Inject constructor(
    private val timeEntryRepository: TimeEntryRepository,
    private val holidayRepository: HolidayRepository,
    private val employeeRepository: EmployeeRepository,
    private val timeEntryMapper: TimeEntryMapper,
    private val holidayDefinitionService: ch.baunex.timetracking.service.HolidayDefinitionService,
    private val holidayTypeService: ch.baunex.timetracking.service.HolidayTypeService
) {
    private val log = Logger.getLogger(WorkSummaryService::class.java)

    /**
     * Calculate expected working hours for a specific date
     * Uses the holiday configuration to determine expected hours for different holiday types
     */
    fun calculateExpectedHours(employeeId: Long, date: LocalDate): Double {
        log.info("Calculating expected hours for employee $employeeId on $date")
        val employee = employeeRepository.findById(employeeId) ?: return 0.0
        
        // Check if it's a weekend
        if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
            return 0.0
        }
        
        // Check if it's a public holiday
        if (holidayDefinitionService.isHoliday(date)) {
            return holidayTypeService.getExpectedHoursForHolidayType("PUBLIC_HOLIDAY")
        }

        // Check if employee has approved vacation on this date
        val approvedHoliday = holidayRepository.findByEmployeeAndDateRange(employeeId, date, date)
            .find { holiday ->
                date >= holiday.startDate && 
                date <= holiday.endDate && 
                holiday.approvalStatus == ApprovalStatus.APPROVED
            }
        
        if (approvedHoliday != null) {
            // Use the holiday type service to determine expected hours for this holiday type
            return holidayTypeService.getExpectedHoursForHolidayType(approvedHoliday.holidayType.code)
        }

        // Default workday hours from service
        return holidayTypeService.getDefaultWorkdayHours()
    }

    /**
     * Get daily work summary for an employee in a date range
     */
    fun getDailyWorkSummary(employeeId: Long, from: LocalDate, to: LocalDate): List<EmployeeDailyWorkDTO> {
        log.info("Fetching daily work summary for employee $employeeId from $from to $to")
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
            // Don't calculate expected hours for future dates
            val expectedHours = if (currentDate.isAfter(LocalDate.now())) 0.0 else calculateExpectedHours(employeeId, currentDate)
            val delta = workedHours - expectedHours
            
            // Check for holidays on this date
            val holiday = holidays.find { holiday ->
                currentDate >= holiday.startDate && currentDate <= holiday.endDate
            }
            
            val isWeekend = currentDate.dayOfWeek == DayOfWeek.SATURDAY || currentDate.dayOfWeek == DayOfWeek.SUNDAY
            val isPublicHoliday = holidayDefinitionService.isHoliday(currentDate)
            
            // Get public holiday name if it's a public holiday
            val publicHolidayName = if (isPublicHoliday) {
                val holidayDefinitions = holidayDefinitionService.getHolidaysForDateRange(currentDate, currentDate)
                holidayDefinitions.firstOrNull()?.name
            } else null
            
            result.add(
                EmployeeDailyWorkDTO(
                    employeeId = employeeId,
                    employeeName = employeeName,
                    date = currentDate,
                    workedHours = workedHours,
                    expectedHours = expectedHours,
                    delta = delta,
                    holidayTypeCode = holiday?.holidayType?.code,
                    holidayTypeDisplayName = holiday?.holidayType?.displayName,
                    holidayId = holiday?.id,
                    holidayApproved = holiday?.approvalStatus == ApprovalStatus.APPROVED,
                    holidayReason = holiday?.reason,
                    timeEntries = timeEntriesByDate[currentDate]?.map { timeEntryMapper.toTimeEntryResponseDTO(it) } ?: emptyList(),
                    isWeekend = isWeekend,
                    isPublicHoliday = isPublicHoliday,
                    publicHolidayName = publicHolidayName
                )
            )
            
            currentDate = currentDate.plusDays(1)
        }
        
        log.info("Fetched daily work summary for employee $employeeId from $from to $to")
        return result
    }

    /**
     * Calculate remaining vacation days for an employee in a given year
     */
    fun calculateRemainingVacationDays(employeeId: Long, year: Int): Int {
        val employee = employeeRepository.findById(employeeId) ?: return 0
        
        // Get all approved vacation days for the year
        val yearStart = LocalDate.of(year, 1, 1)
        val yearEnd = LocalDate.of(year, 12, 31)
        
        val vacationDays = holidayRepository.findByEmployeeAndDateRange(employeeId, yearStart, yearEnd)
            .filter { it.holidayType.code == "PAID_VACATION" && it.approvalStatus == ApprovalStatus.APPROVED }
            .sumOf { holiday ->
                // Calculate the number of working days in the holiday period (excluding weekends and public holidays)
                holidayDefinitionService.calculateWorkingDays(holiday.startDate, holiday.endDate)
            }
        
        return employee.vacationDays - vacationDays
    }

    /**
     * Calculate used vacation days for an employee in a given year
     */
    fun calculateUsedVacationDays(employeeId: Long, year: Int): Int {
        val yearStart = LocalDate.of(year, 1, 1)
        val yearEnd = LocalDate.of(year, 12, 31)
        
        return holidayRepository.findByEmployeeAndDateRange(employeeId, yearStart, yearEnd)
            .filter { it.holidayType.code == "PAID_VACATION" && it.approvalStatus == ApprovalStatus.APPROVED }
            .sumOf { holiday ->
                // Calculate the number of working days in the holiday period (excluding weekends and public holidays)
                holidayDefinitionService.calculateWorkingDays(holiday.startDate, holiday.endDate)
            }
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
        
        val holidayDays = dailySummaries.count { it.holidayTypeCode != null && it.holidayApproved == true }
        val pendingHolidayRequests = dailySummaries.count { it.holidayTypeCode != null && it.holidayApproved == false }
        
        // Calculate vacation statistics
        val currentYear = weekStart.year
        val totalVacationDays = employee.vacationDays
        val usedVacationDays = calculateUsedVacationDays(employeeId, currentYear)
        val remainingVacationDays = totalVacationDays - usedVacationDays
        
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
            totalVacationDays = totalVacationDays,
            usedVacationDays = usedVacationDays,
            remainingVacationDays = remainingVacationDays,
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

    /**
     * Get cumulative hours account for an employee
     * Calculates the balance since the employee's start date
     */
    fun getCumulativeHoursAccount(employeeId: Long, year: Int, week: Int): CumulativeHoursAccountDTO? {
        log.info("Fetching cumulative hours account for employee $employeeId, year $year, week $week")
        val employee = employeeRepository.findById(employeeId) ?: return null
        val employeeName = "${employee.person.firstName} ${employee.person.lastName}"
        
        // Calculate current week start and end dates
        val weekFields = WeekFields.of(Locale.getDefault())
        val currentWeekStart = LocalDate.now().withYear(year).with(weekFields.weekOfYear(), week.toLong())
            .with(weekFields.dayOfWeek(), 1L) // Monday
        val currentWeekEnd = currentWeekStart.plusDays(6) // Sunday
        
        // Get current week summary
        val currentWeekSummary = getWeeklyWorkSummary(employeeId, year, week) ?: return null
        
        // Calculate cumulative balance since start date
        val startDate = employee.startDate
        val endDate = currentWeekEnd
        
        // Get all daily summaries from start date to current week end
        val allDailySummaries = getDailyWorkSummary(employeeId, startDate, endDate)
        
        // Calculate cumulative totals
        val cumulativeWorked = allDailySummaries.sumOf { it.workedHours }
        val cumulativeExpected = allDailySummaries.sumOf { it.expectedHours }
        val cumulativeOvertime = allDailySummaries.sumOf { if (it.delta > 0) it.delta else 0.0 }
        val cumulativeUndertime = allDailySummaries.sumOf { if (it.delta < 0) -it.delta else 0.0 }
        val cumulativeBalance = cumulativeOvertime - cumulativeUndertime
        
        // Calculate vacation statistics
        val currentYear = currentWeekStart.year
        val totalVacationDays = employee.vacationDays
        val usedVacationDays = calculateUsedVacationDays(employeeId, currentYear)
        val remainingVacationDays = totalVacationDays - usedVacationDays
        
        log.info("Fetched cumulative hours account for employee $employeeId, year $year, week $week")
        return CumulativeHoursAccountDTO(
            employeeId = employeeId,
            employeeName = employeeName,
            startDate = startDate,
            
            // Current week balance
            weeklyBalance = currentWeekSummary.overtime - currentWeekSummary.undertime,
            weeklyWorkedHours = currentWeekSummary.totalWorked,
            weeklyExpectedHours = currentWeekSummary.totalExpected,
            
            // Cumulative balance since start date
            cumulativeBalance = cumulativeBalance,
            cumulativeWorkedHours = cumulativeWorked,
            cumulativeExpectedHours = cumulativeExpected,
            
            // Current week info
            currentWeekStart = currentWeekStart,
            currentWeekEnd = currentWeekEnd,
            currentWeekNumber = week,
            currentYear = year,
            
            // Vacation and absence info
            holidayDays = currentWeekSummary.holidayDays,
            pendingHolidayRequests = currentWeekSummary.pendingHolidayRequests,
            totalVacationDays = totalVacationDays,
            usedVacationDays = usedVacationDays,
            remainingVacationDays = remainingVacationDays
        )
    }

    /**
     * Format hours with sign and specified decimal places
     */
    private fun formatHours(value: Double, decimals: Int = 1): String {
        val sign = if (value >= 0) "+" else ""
        return sign + "%.${decimals}f".format(value)
    }

    /**
     * Format date to day string (e.g., "07")
     */
    private fun formatDateDay(date: LocalDate): String {
        return "%02d".format(date.dayOfMonth)
    }

    /**
     * Get monthly hours account for an employee
     * Returns detailed monthly breakdown with weeks and days
     */
    fun getMonthlyHoursAccount(employeeId: Long, year: Int): MonthlyHoursAccountDTO? {
        log.info("Fetching monthly hours account for employee $employeeId, year $year")
        val employee = employeeRepository.findById(employeeId) ?: return null
        val employeeName = "${employee.person.firstName} ${employee.person.lastName}"
        
        val currentDate = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val currentWeek = currentDate.get(weekFields.weekOfYear())
        val currentYear = currentDate.year
        
        // Get current week summary for the summary section
        val currentWeekSummary = getWeeklyWorkSummary(employeeId, currentYear, currentWeek)
        
        // Calculate cumulative balance since start date
        val startDate = employee.startDate
        val yearStart = LocalDate.of(year, 1, 1)
        val yearEnd = LocalDate.of(year, 12, 31)
        
        // Determine the effective start date for cumulative calculation
        // Use the same logic as for month range calculation
        val effectiveStartDate = if (startDate.isAfter(yearStart)) {
            // Employee started after January 1st of this year
            startDate
        } else {
            // Employee started before or on January 1st of this year
            yearStart
        }
        
        // Get all daily summaries from effective start date to today (or year end if future year)
        val endDate = if (year > currentDate.year) yearEnd else currentDate
        val allDailySummaries = getDailyWorkSummary(employeeId, effectiveStartDate, endDate)
        
        // Calculate cumulative totals
        val cumulativeWorked = allDailySummaries.sumOf { it.workedHours }
        val cumulativeExpected = allDailySummaries.sumOf { it.expectedHours }
        val cumulativeBalance = cumulativeWorked - cumulativeExpected
        
        // Build monthly data
        val monthlyData = mutableListOf<MonthDataDTO>()
        
        // Determine which months to show based on employee start date and current date
        val startMonth: Int
        val endMonth: Int
        
        // Determine start month based on employee start date vs year start
        startMonth = effectiveStartDate.monthValue
        endMonth = if (year == currentDate.year) currentDate.monthValue else 12
        
        // Don't show future months
        if (year > currentDate.year) {
            log.warn("Requested year is in the future, returning null for employee $employeeId, year $year")
            return null
        }
        
        for (month in startMonth..endMonth) {
            val monthStart = LocalDate.of(year, month, 1)
            val monthEnd = monthStart.plusMonths(1).minusDays(1)
            
            // Get daily summaries for this month
            val monthDailySummaries = getDailyWorkSummary(employeeId, monthStart, monthEnd)
            
            // Find the Monday of the week containing the first day of the month
            val firstDayOfMonth = monthStart
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1=Monday, 7=Sunday
            val daysToSubtract = if (firstDayOfWeek == 1) 0 else firstDayOfWeek - 1
            val firstMondayOfMonth = firstDayOfMonth.minusDays(daysToSubtract.toLong())
            
            // Find the last Sunday of the month
            val lastDayOfMonth = monthEnd
            val lastDayOfWeek = lastDayOfMonth.dayOfWeek.value // 1=Monday, 7=Sunday
            val daysToAdd = if (lastDayOfWeek == 7) 0 else 7 - lastDayOfWeek
            val lastSundayOfMonth = lastDayOfMonth.plusDays(daysToAdd.toLong())
            
            // Group by weeks
            val weeks = mutableListOf<WeekDataDTO>()
            var currentWeekStart = firstMondayOfMonth
            
            while (!currentWeekStart.isAfter(lastSundayOfMonth)) {
                val weekNumber = currentWeekStart.get(weekFields.weekOfYear())
                val weekEnd = currentWeekStart.plusDays(6)
                
                // Build day data for the full week (Monday to Sunday)
                val days = mutableListOf<DayDataDTO>()
                
                for (i in 0..6) {
                    val currentDay = currentWeekStart.plusDays(i.toLong())
                    
                    // Check if this day is within the month
                    if (currentDay.isBefore(monthStart) || currentDay.isAfter(monthEnd)) {
                        // This day is outside the month - create empty day
                        days.add(
                            DayDataDTO(
                                date = currentDay,
                                dateDay = "", // Empty for days outside month
                                dayOfWeek = currentDay.dayOfWeek.value,
                                dayName = currentDay.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.GERMAN),
                                workedHours = 0.0,
                                expectedHours = 0.0,
                                balance = 0.0,
                                saldo = 0.0,
                                saldoFormatted = "",
                                isWeekend = currentDay.dayOfWeek == DayOfWeek.SATURDAY || currentDay.dayOfWeek == DayOfWeek.SUNDAY,
                                isHoliday = false,
                                holidayType = null,
                                holidayApproved = null,
                                isPublicHoliday = false,
                                publicHolidayName = null,
                                isEmpty = true, // Mark as empty for styling
                                isFuture = false
                            )
                        )
                    } else {
                        // This day is within the month
                        val isFuture = currentDay.isAfter(currentDate)
                        val isBeforeStartDate = currentDay.isBefore(startDate)
                        val dailySummary = monthDailySummaries.find { it.date == currentDay }
                        
                        if (dailySummary != null && !isFuture && !isBeforeStartDate) {
                            val saldo = dailySummary.delta
                            days.add(
                                DayDataDTO(
                                    date = currentDay,
                                    dateDay = formatDateDay(currentDay),
                                    dayOfWeek = currentDay.dayOfWeek.value,
                                    dayName = currentDay.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.GERMAN),
                                    workedHours = dailySummary.workedHours,
                                    expectedHours = dailySummary.expectedHours,
                                    balance = dailySummary.delta,
                                    saldo = saldo,
                                    saldoFormatted = formatHours(saldo),
                                    isWeekend = dailySummary.isWeekend,
                                    isHoliday = dailySummary.holidayTypeCode != null,
                                    holidayType = dailySummary.holidayTypeDisplayName,
                                    holidayApproved = dailySummary.holidayApproved,
                                    isPublicHoliday = dailySummary.isPublicHoliday,
                                    publicHolidayName = dailySummary.publicHolidayName,
                                    isEmpty = false,
                                    isFuture = false
                                )
                            )
                        } else {
                            // Add empty day within month (either no data, future date, or before start date)
                            // Check if it's a public holiday for this day
                            val isPublicHoliday = holidayDefinitionService.isHoliday(currentDay)
                            val publicHolidayName = if (isPublicHoliday) {
                                val holidayDefinitions = holidayDefinitionService.getHolidaysForDateRange(currentDay, currentDay)
                                holidayDefinitions.firstOrNull()?.name
                            } else null
                            
                            days.add(
                                DayDataDTO(
                                    date = currentDay,
                                    dateDay = formatDateDay(currentDay),
                                    dayOfWeek = currentDay.dayOfWeek.value,
                                    dayName = currentDay.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.GERMAN),
                                    workedHours = 0.0,
                                    expectedHours = 0.0,
                                    balance = 0.0,
                                    saldo = 0.0,
                                    saldoFormatted = "",
                                    isWeekend = currentDay.dayOfWeek == DayOfWeek.SATURDAY || currentDay.dayOfWeek == DayOfWeek.SUNDAY,
                                    isHoliday = false,
                                    holidayType = null,
                                    holidayApproved = null,
                                    isPublicHoliday = isPublicHoliday,
                                    publicHolidayName = publicHolidayName,
                                    isEmpty = false,
                                    isFuture = isFuture || isBeforeStartDate
                                )
                            )
                        }
                    }
                }
                
                // Calculate week totals (only for days within the month, not future, and not before start date)
                val weekWorkedHours = days.filter { !it.isEmpty && !it.isFuture }.sumOf { it.workedHours }
                val weekExpectedHours = days.filter { !it.isEmpty && !it.isFuture }.sumOf { it.expectedHours }
                val weekTotal = weekWorkedHours - weekExpectedHours
                
                weeks.add(
                    WeekDataDTO(
                        weekNumber = weekNumber,
                        weekStart = currentWeekStart,
                        weekEnd = weekEnd,
                        days = days,
                        weekTotal = weekTotal,
                        weekTotalFormatted = formatHours(weekTotal),
                        weekWorkedHours = weekWorkedHours,
                        weekExpectedHours = weekExpectedHours
                    )
                )
                
                currentWeekStart = currentWeekStart.plusWeeks(1)
            }
            
            // Calculate month totals (exclude future dates and dates before start date)
            val monthWorkedHours = monthDailySummaries.filter { it.date <= currentDate && !it.date.isBefore(startDate) }.sumOf { it.workedHours }
            val monthExpectedHours = monthDailySummaries.filter { it.date <= currentDate && !it.date.isBefore(startDate) }.sumOf { it.expectedHours }
            val monthTotal = monthWorkedHours - monthExpectedHours
            
            monthlyData.add(
                MonthDataDTO(
                    year = year,
                    month = month,
                    monthName = monthStart.month.getDisplayName(TextStyle.FULL, Locale.GERMAN),
                    weeks = weeks,
                    monthTotal = monthTotal,
                    monthTotalFormatted = formatHours(monthTotal, 2),
                    monthWorkedHours = monthWorkedHours,
                    monthWorkedHoursFormatted = "%.1f".format(monthWorkedHours),
                    monthExpectedHours = monthExpectedHours,
                    monthExpectedHoursFormatted = "%.1f".format(monthExpectedHours)
                )
            )
        }
        
        // Sort months in descending order (newest first)
        monthlyData.sortByDescending { monthData -> monthData.month }
        
        log.info("Fetched monthly hours account for employee $employeeId, year $year")
        val currentWeekBalance = currentWeekSummary?.let { it.overtime - it.undertime } ?: 0.0
        val currentWeekWorkedHours = currentWeekSummary?.totalWorked ?: 0.0
        val currentWeekExpectedHours = currentWeekSummary?.totalExpected ?: 0.0
        
        return MonthlyHoursAccountDTO(
            employeeId = employeeId,
            employeeName = employeeName,
            startDate = startDate,
            currentWeekBalance = currentWeekBalance,
            currentWeekBalanceFormatted = formatHours(currentWeekBalance, 2),
            currentWeekWorkedHours = currentWeekWorkedHours,
            currentWeekWorkedHoursFormatted = "%.1f".format(currentWeekWorkedHours),
            currentWeekExpectedHours = currentWeekExpectedHours,
            currentWeekExpectedHoursFormatted = "%.1f".format(currentWeekExpectedHours),
            cumulativeBalance = cumulativeBalance,
            cumulativeBalanceFormatted = formatHours(cumulativeBalance, 2),
            cumulativeWorkedHours = cumulativeWorked,
            cumulativeWorkedHoursFormatted = "%.1f".format(cumulativeWorked),
            cumulativeExpectedHours = cumulativeExpected,
            cumulativeExpectedHoursFormatted = "%.1f".format(cumulativeExpected),
            monthlyData = monthlyData
        )
    }
} 