package ch.baunex.timetracking.dto

import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class OverviewKPIDTO(
    val weeklyBalance: Double, // Wochensaldo (Soll vs. Ist)
    val workedHoursThisWeek: Double, // Gearbeitete Stunden dieser Woche
    val cumulativeBalance: Double, // Kumuliertes Stundenkonto
    val remainingVacationDays: Int, // Verbleibende Urlaubstage
    val approvedAbsenceDays: Int, // Genehmigte Abwesenheitstage
    val pendingVacationRequests: Int, // Offene Urlaubsanträge
    val missingTimeEntries: Int // Fehlende Zeiteinträge
)

@Serializable
data class OverviewAlertDTO(
    val id: Long,
    val type: String, // "MISSING_ENTRY", "PENDING_VACATION", "OVERDUE_APPROVAL"
    val title: String,
    val message: String,
    val severity: String, // "LOW", "MEDIUM", "HIGH"
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate?,
    val employeeId: Long?,
    val employeeName: String?,
    val actionUrl: String?,
    val actionLabel: String?
)

@Serializable
data class OverviewQuickActionDTO(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val url: String,
    val color: String
)

@Serializable
data class OverviewCalendarDayDTO(
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val hasTimeEntries: Boolean,
    val hasHolidays: Boolean,
    val isPublicHoliday: Boolean,
    val isWeekend: Boolean,
    val workedHours: Double,
    val expectedHours: Double,
    val holidayType: String?,
    val holidayName: String?
)

@Serializable
data class OverviewActivityDTO(
    val id: Long,
    val type: String, // "TIME_ENTRY_CREATED", "VACATION_REQUESTED", "APPROVAL_GIVEN"
    val title: String,
    val description: String,
    val timestamp: String,
    val employeeId: Long,
    val employeeName: String,
    val projectName: String?,
    val hours: Double?
)

@Serializable
data class OverviewTeamMemberDTO(
    val employeeId: Long,
    val employeeName: String,
    val workedHoursThisWeek: Double,
    val expectedHoursThisWeek: Double,
    val balance: Double,
    val pendingApprovals: Int,
    val upcomingVacations: List<UpcomingVacationDTO>
)

@Serializable
data class UpcomingVacationDTO(
    val id: Long,
    @Serializable(with = LocalDateSerializer::class)
    val startDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val endDate: LocalDate,
    val type: String,
    val reason: String?
)

@Serializable
data class OverviewTrendDTO(
    val period: String, // "WEEK", "MONTH"
    val label: String,
    val workedHours: Double,
    val expectedHours: Double,
    val balance: Double
)

@Serializable
data class OverviewDTO(
    val kpis: OverviewKPIDTO,
    val alerts: List<OverviewAlertDTO>,
    val quickActions: List<OverviewQuickActionDTO>,
    val calendarDays: List<OverviewCalendarDayDTO>,
    val recentActivities: List<OverviewActivityDTO>,
    val teamMembers: List<OverviewTeamMemberDTO>,
    val trends: List<OverviewTrendDTO>,
    val currentWeek: Int,
    val currentYear: Int,
    val selectedEmployeeId: Long?
) 