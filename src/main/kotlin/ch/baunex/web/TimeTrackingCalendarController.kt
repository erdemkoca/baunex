package ch.baunex.web

import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import ch.baunex.user.dto.EmployeeDTO
import ch.baunex.user.facade.EmployeeFacade
import ch.baunex.project.dto.ProjectListDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.timetracking.facade.HolidayFacade
import ch.baunex.timetracking.dto.HolidayDTO
import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Path("/timetracking/calendar")
class TimeTrackingCalendarController {

    private val json = Json { ignoreUnknownKeys = true }

    @Inject
    lateinit var timeTrackingFacade: TimeTrackingFacade

    @Inject
    lateinit var employeeFacade: EmployeeFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var holidayFacade: HolidayFacade

    @CheckedTemplate(requireTypeSafeExpressions = false)
    object Templates {
        @JvmStatic
        external fun calendar(
            activeMenu: String,
            activeSubMenu: String,
            timeEntriesJson: String,
            holidaysJson: String,
            currentDate: String,
            employeesJson: String,
            projectsJson: String
        ): TemplateInstance
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun calendar(): Response {
        val entries = timeTrackingFacade.getAllTimeEntries()
        val employees = employeeFacade.listAll()
        val projects = projectFacade.getAllProjects()
        val holidays = holidayFacade.getAllHolidays()
        
        val page = Templates.calendar(
            activeMenu = "timetracking",
            activeSubMenu = "calendar",
            timeEntriesJson = json.encodeToString(entries),
            holidaysJson = json.encodeToString(holidays),
            currentDate = LocalDate.now().toString(),
            employeesJson = json.encodeToString(employees),
            projectsJson = json.encodeToString(projects)
        )
        
        return Response.ok(page.render()).build()
    }
} 