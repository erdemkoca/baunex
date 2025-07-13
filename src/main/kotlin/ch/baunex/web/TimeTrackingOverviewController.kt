package ch.baunex.web

import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.WeeklyWorkSummaryDTO
import ch.baunex.timetracking.dto.ProjectStatsDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import ch.baunex.user.dto.EmployeeDTO
import ch.baunex.user.facade.EmployeeFacade
import ch.baunex.project.dto.ProjectListDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.timetracking.facade.HolidayFacade
import ch.baunex.timetracking.dto.HolidayDTO
import ch.baunex.timetracking.service.WorkSummaryService
import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Path("/timetracking/overview")
class TimeTrackingOverviewController {

    private val json = Json { ignoreUnknownKeys = true }

    @Inject
    lateinit var timeTrackingFacade: TimeTrackingFacade

    @Inject
    lateinit var employeeFacade: EmployeeFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var holidayFacade: HolidayFacade

    @Inject
    lateinit var workSummaryService: WorkSummaryService

    @CheckedTemplate(requireTypeSafeExpressions = false)
    object Templates {
        @JvmStatic
        external fun overview(
            activeMenu: String,
            activeSubMenu: String,
            currentDate: LocalDate,
            employeesJson: String,
            projectsJson: String,
            pendingApprovalsJson: String,
            recentTimeEntriesJson: String,
            weeklyStatsJson: String,
            pendingHolidaysJson: String,
            projectStatsJson: String
        ): TemplateInstance
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun overview(): Response {
        val currentDate = LocalDate.now()
        val employees = employeeFacade.listAll()
        val projects = projectFacade.getAllProjects()
        
        // Get current week
        val weekFields = WeekFields.of(Locale.getDefault())
        val currentWeek = currentDate.get(weekFields.weekOfYear())
        val currentYear = currentDate.year
        
        // Get pending approvals (time entries not approved)
        val pendingApprovals = timeTrackingFacade.getAllTimeEntries()
            .filter { !it.approval.approved }
            .take(10) // Limit to 10 most recent
        
        // Get recent time entries
        val recentTimeEntries = timeTrackingFacade.getAllTimeEntries()
            .sortedByDescending { it.date }
            .take(10)
        
        // Get weekly statistics for all employees
        val weeklyStats = workSummaryService.getAllEmployeesWeeklyWorkSummary(currentYear, currentWeek)
        
        // Get pending holiday requests
        val pendingHolidays = holidayFacade.getAllHolidays()
            .filter { it.status == "PENDING" }
            .take(10)
        
        // Get project statistics (active projects with time entries)
        val projectStats = projects.map { project ->
            val projectTimeEntries = timeTrackingFacade.getAllTimeEntries()
                .filter { it.projectId == project.id }
            val totalHours = projectTimeEntries.sumOf { it.hoursWorked }
            val employeeCount = projectTimeEntries.map { it.employeeId }.distinct().size
            
            ProjectStatsDTO(
                project = project,
                totalHours = totalHours,
                employeeCount = employeeCount,
                lastActivity = projectTimeEntries.maxByOrNull { it.date }?.date
            )
        }.sortedByDescending { it.totalHours }
        
        val page = Templates.overview(
            activeMenu = "timetracking",
            activeSubMenu = "overview",
            currentDate = currentDate,
            employeesJson = json.encodeToString(employees),
            projectsJson = json.encodeToString(projects),
            pendingApprovalsJson = json.encodeToString(pendingApprovals),
            recentTimeEntriesJson = json.encodeToString(recentTimeEntries),
            weeklyStatsJson = json.encodeToString(weeklyStats),
            pendingHolidaysJson = json.encodeToString(pendingHolidays),
            projectStatsJson = json.encodeToString(projectStats)
        )
        
        return Response.ok(page.render()).build()
    }
} 