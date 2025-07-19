package ch.baunex.timetracking.service

import ch.baunex.timetracking.dto.*
import ch.baunex.timetracking.facade.TimeTrackingFacade
import ch.baunex.timetracking.facade.HolidayFacade
import ch.baunex.user.facade.EmployeeFacade
import ch.baunex.user.model.EmployeeModel
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.*

@ApplicationScoped
class OverviewService {

    @Inject lateinit var timeTrackingFacade: TimeTrackingFacade
    @Inject lateinit var holidayFacade: HolidayFacade
    @Inject lateinit var employeeFacade: EmployeeFacade
    @Inject lateinit var workSummaryService: WorkSummaryService
    @Inject lateinit var holidayDefinitionService: HolidayDefinitionService

    private val log = Logger.getLogger(OverviewService::class.java)

    fun getOverviewData(employeeId: Long? = null): OverviewDTO {
        log.info("Generating overview data for employee: $employeeId")
        
        val currentDate = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val currentWeek = currentDate.get(weekFields.weekOfYear())
        val currentYear = currentDate.year
        
        val selectedEmployeeId = employeeId ?: employeeFacade.listAll().firstOrNull()?.id ?: 0L
        
        // KPI-Daten berechnen
        val kpis = calculateKPIs(selectedEmployeeId, currentWeek, currentYear)
        
        // Alerts generieren
        val alerts = generateAlerts(selectedEmployeeId, currentDate)
        
        // Quick Actions
        val quickActions = generateQuickActions()
        
        // Kalender-Daten für aktuelle Woche
        val calendarDays = generateCalendarDays(selectedEmployeeId, currentWeek, currentYear)
        
        // Letzte Aktivitäten
        val recentActivities = generateRecentActivities()
        
        // Team-Übersicht (nur wenn Admin)
        val teamMembers = generateTeamOverview(currentWeek, currentYear)
        
        // Trends
        val trends = generateTrends(selectedEmployeeId, currentWeek, currentYear)
        
        return OverviewDTO(
            kpis = kpis,
            alerts = alerts,
            quickActions = quickActions,
            calendarDays = calendarDays,
            recentActivities = recentActivities,
            teamMembers = teamMembers,
            trends = trends,
            currentWeek = currentWeek,
            currentYear = currentYear,
            selectedEmployeeId = selectedEmployeeId
        )
    }

    private fun calculateKPIs(employeeId: Long, week: Int, year: Int): OverviewKPIDTO {
        log.debug("Calculating KPIs for employee $employeeId, week $week, year $year")
        
        // Wochensaldo berechnen
        val weeklySummary = workSummaryService.getWeeklyWorkSummary(employeeId, year, week)
        val weeklyBalance = weeklySummary?.let { it.totalWorked - it.totalExpected } ?: 0.0
        val workedHoursThisWeek = weeklySummary?.totalWorked ?: 0.0
        
        // Kumuliertes Stundenkonto
        val cumulativeBalance = workSummaryService.getCumulativeHoursAccountUpToWeek(employeeId, year, week)?.cumulativeBalance ?: 0.0
        
        // Urlaubsdaten
        val employee = employeeFacade.findById(employeeId)
        val holidays = holidayFacade.getHolidaysForEmployee(employeeId)
        val approvedHolidays = holidays.filter { it.status == "APPROVED" }
        val pendingHolidays = holidays.filter { it.status == "PENDING" }
        
        val approvedAbsenceDays = approvedHolidays.sumOf { 
            java.time.temporal.ChronoUnit.DAYS.between(it.startDate, it.endDate) + 1 
        }.toInt()
        
        val remainingVacationDays = (employee?.vacationDays ?: 0) - approvedAbsenceDays
        
        // Fehlende Zeiteinträge
        val missingTimeEntries = calculateMissingTimeEntries(employeeId, week, year)
        
        return OverviewKPIDTO(
            weeklyBalance = weeklyBalance,
            workedHoursThisWeek = workedHoursThisWeek,
            cumulativeBalance = cumulativeBalance,
            remainingVacationDays = remainingVacationDays,
            approvedAbsenceDays = approvedAbsenceDays,
            pendingVacationRequests = pendingHolidays.size,
            missingTimeEntries = missingTimeEntries
        )
    }

    private fun generateAlerts(employeeId: Long, currentDate: LocalDate): List<OverviewAlertDTO> {
        log.debug("Generating alerts for employee $employeeId")
        
        val alerts = mutableListOf<OverviewAlertDTO>()
        
        // Fehlende Zeiteinträge für gestern
        val yesterday = currentDate.minusDays(1)
        if (yesterday.dayOfWeek != DayOfWeek.SATURDAY && yesterday.dayOfWeek != DayOfWeek.SUNDAY) {
            val dailySummary = workSummaryService.getDailyWorkSummary(employeeId, yesterday, yesterday)
            val hasEntries = dailySummary.any { it.timeEntries.isNotEmpty() }
            
            if (!hasEntries) {
                alerts.add(OverviewAlertDTO(
                    id = 1L,
                    type = "MISSING_ENTRY",
                    title = "Fehlender Zeiteintrag",
                    message = "Für gestern (${yesterday.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))}) fehlt ein Zeiteintrag",
                    severity = "MEDIUM",
                    date = yesterday,
                    employeeId = employeeId,
                    employeeName = employeeFacade.findById(employeeId)?.let { "${it.firstName} ${it.lastName}" },
                    actionUrl = "/timetracking/0?date=${yesterday}",
                    actionLabel = "Jetzt erfassen"
                ))
            }
        }
        
        // Ausstehende Urlaubsanträge
        val pendingHolidays = holidayFacade.getHolidaysForEmployee(employeeId)
            .filter { it.status == "PENDING" }
        
        if (pendingHolidays.isNotEmpty()) {
            alerts.add(OverviewAlertDTO(
                id = 2L,
                type = "PENDING_VACATION",
                title = "Ausstehende Urlaubsanträge",
                message = "${pendingHolidays.size} Urlaubsantrag${if (pendingHolidays.size > 1) "e" else ""} warten auf Genehmigung",
                severity = "LOW",
                date = null,
                employeeId = employeeId,
                employeeName = employeeFacade.findById(employeeId)?.let { "${it.firstName} ${it.lastName}" },
                actionUrl = "/timetracking/absences",
                actionLabel = "Jetzt überprüfen"
            ))
        }
        
        return alerts
    }

    private fun generateQuickActions(): List<OverviewQuickActionDTO> {
        return listOf(
            OverviewQuickActionDTO(
                id = "new-time-entry",
                title = "Neue Zeit erfassen",
                description = "Zeit für heute oder ein anderes Datum erfassen",
                icon = "bi-plus-circle",
                url = "/timetracking/0",
                color = "primary"
            ),
            OverviewQuickActionDTO(
                id = "request-vacation",
                title = "Urlaub beantragen",
                description = "Neuen Urlaubsantrag erstellen",
                icon = "bi-calendar-check",
                url = "/timetracking/absences",
                color = "success"
            ),
            OverviewQuickActionDTO(
                id = "report-absence",
                title = "Abwesenheit melden",
                description = "Krankheit oder andere Abwesenheit melden",
                icon = "bi-exclamation-triangle",
                url = "/timetracking/absences",
                color = "warning"
            )
        )
    }

    private fun generateCalendarDays(employeeId: Long, week: Int, year: Int): List<OverviewCalendarDayDTO> {
        log.debug("Generating calendar days for employee $employeeId, week $week, year $year")
        
        val weekStart = getWeekStartDate(year, week)
        val days = mutableListOf<OverviewCalendarDayDTO>()
        
        for (i in 0..6) {
            val date = weekStart.plusDays(i.toLong())
            val dailySummary = workSummaryService.getDailyWorkSummary(employeeId, date, date)
            val summary = dailySummary.firstOrNull()
            
            // Feiertage prüfen
            val publicHolidays = holidayDefinitionService.getHolidaysForDateRange(date, date)
            val isPublicHoliday = publicHolidays.isNotEmpty()
            val publicHolidayName = publicHolidays.firstOrNull()?.name
            
            // Urlaube prüfen
            val holidays = holidayFacade.getHolidaysForEmployee(employeeId)
                .filter { it.startDate <= date && it.endDate >= date }
            val hasHolidays = holidays.isNotEmpty()
            val holidayType = holidays.firstOrNull()?.type
            
            days.add(OverviewCalendarDayDTO(
                date = date,
                hasTimeEntries = summary?.timeEntries?.isNotEmpty() ?: false,
                hasHolidays = hasHolidays,
                isPublicHoliday = isPublicHoliday,
                isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY,
                workedHours = summary?.workedHours ?: 0.0,
                expectedHours = summary?.expectedHours ?: 0.0,
                holidayType = holidayType,
                holidayName = publicHolidayName
            ))
        }
        
        return days
    }

    private fun generateRecentActivities(): List<OverviewActivityDTO> {
        log.debug("Generating recent activities")
        
        val timeEntries = timeTrackingFacade.getAllTimeEntries()
            .sortedByDescending { it.date }
            .take(10)
        
        return timeEntries.map { entry ->
            OverviewActivityDTO(
                id = entry.id ?: 0L,
                type = "TIME_ENTRY_CREATED",
                title = "Zeiteintrag erstellt",
                description = "${entry.employeeFirstName} ${entry.employeeLastName} hat ${entry.hoursWorked}h für ${entry.projectName} erfasst",
                timestamp = entry.date.toString(),
                employeeId = entry.employeeId,
                employeeName = "${entry.employeeFirstName} ${entry.employeeLastName}",
                projectName = entry.projectName,
                hours = entry.hoursWorked
            )
        }
    }

    private fun generateTeamOverview(week: Int, year: Int): List<OverviewTeamMemberDTO> {
        log.debug("Generating team overview for week $week, year $year")
        
        val employees = employeeFacade.listAll()
        return employees.map { employee ->
            val weeklySummary = workSummaryService.getWeeklyWorkSummary(employee.id, year, week)
            val holidays = holidayFacade.getHolidaysForEmployee(employee.id)
                .filter { it.startDate >= LocalDate.now() && it.status == "APPROVED" }
                .take(3)
            
            val upcomingVacations = holidays.map { holiday ->
                UpcomingVacationDTO(
                    id = holiday.id ?: 0L,
                    startDate = holiday.startDate,
                    endDate = holiday.endDate,
                    type = holiday.type,
                    reason = holiday.reason
                )
            }
            
            OverviewTeamMemberDTO(
                employeeId = employee.id ?: 0L,
                employeeName = "${employee.firstName} ${employee.lastName}",
                workedHoursThisWeek = weeklySummary?.totalWorked ?: 0.0,
                expectedHoursThisWeek = weeklySummary?.totalExpected ?: 0.0,
                balance = (weeklySummary?.totalWorked ?: 0.0) - (weeklySummary?.totalExpected ?: 0.0),
                pendingApprovals = 0, // TODO: Implement pending approvals count
                upcomingVacations = upcomingVacations
            )
        }
    }

    private fun generateTrends(employeeId: Long, week: Int, year: Int): List<OverviewTrendDTO> {
        log.debug("Generating trends for employee $employeeId")
        
        val trends = mutableListOf<OverviewTrendDTO>()
        
        // Letzte 4 Wochen
        for (i in 3 downTo 0) {
            val targetWeek = if (week - i <= 0) 52 + (week - i) else week - i
            val targetYear = if (week - i <= 0) year - 1 else year
            
            val weeklySummary = workSummaryService.getWeeklyWorkSummary(employeeId, targetYear, targetWeek)
            
            trends.add(OverviewTrendDTO(
                period = "WEEK",
                label = "KW $targetWeek",
                workedHours = weeklySummary?.totalWorked ?: 0.0,
                expectedHours = weeklySummary?.totalExpected ?: 0.0,
                balance = (weeklySummary?.totalWorked ?: 0.0) - (weeklySummary?.totalExpected ?: 0.0)
            ))
        }
        
        return trends
    }

    private fun calculateMissingTimeEntries(employeeId: Long, week: Int, year: Int): Int {
        val weekStart = getWeekStartDate(year, week)
        var missingCount = 0
        
        for (i in 0..4) { // Montag bis Freitag
            val date = weekStart.plusDays(i.toLong())
            if (date <= LocalDate.now()) { // Nur vergangene Tage
                val dailySummary = workSummaryService.getDailyWorkSummary(employeeId, date, date)
                val hasEntries = dailySummary.any { it.timeEntries.isNotEmpty() }
                
                if (!hasEntries) {
                    missingCount++
                }
            }
        }
        
        return missingCount
    }

    private fun getWeekStartDate(year: Int, week: Int): LocalDate {
        val simple = LocalDate.of(year, 1, 1).plusWeeks(week.toLong() - 1)
        val dow = simple.dayOfWeek.value
        val weekStart = simple.minusDays((dow - 1).toLong())
        return weekStart
    }
} 