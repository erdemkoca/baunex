package ch.baunex.web

import ch.baunex.project.dto.ProjectDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.user.dto.UserResponseDTO
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate

@Path("/")
class WebController {

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var timeTrackingFacade: TimeTrackingFacade

    @CheckedTemplate
    object Templates {
        @JvmStatic
        external fun index(projects: List<ProjectDTO>, currentDate: LocalDate, activeMenu: String, timeEntries: List<TimeEntryResponseDTO>): TemplateInstance

        @JvmStatic
        external fun projects(projects: List<ProjectDTO>, currentDate: LocalDate, activeMenu: String): TemplateInstance

        @JvmStatic
        external fun projectDetail(project: ProjectDTO, activeMenu: String, currentDate: LocalDate): TemplateInstance

        @JvmStatic
        external fun users(users: List<UserResponseDTO>, currentDate: LocalDate, activeMenu: String): TemplateInstance

        @JvmStatic
        external fun userForm(user: UserResponseDTO?, currentDate: LocalDate, activeMenu: String, roles: List<String>): TemplateInstance

        @JvmStatic
        external fun timetracking(
            activeMenu: String,
            timeEntries: List<TimeEntryResponseDTO>,
            currentDate: String,
            users: List<UserResponseDTO>,
            projects: List<ProjectDTO>,
            entry: TimeEntryResponseDTO? = null): TemplateInstance

        @JvmStatic
        external fun timetrackingForm(
            entry: TimeEntryResponseDTO?,
            users: List<UserResponseDTO>,
            projects: List<ProjectDTO>,
            currentDate: String,
            activeMenu: String): TemplateInstance
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun index(): Response {
        return Response.seeOther(java.net.URI("/dashboard")).build()
    }

    @GET
    @Path("/dashboard")
    @Produces(MediaType.TEXT_HTML)
    fun dashboard(): Response {
        val projects = projectFacade.getAllProjects().map { it }
        val timeEntries = timeTrackingFacade.getAllTimeEntries()
        val template = Templates.index(projects, LocalDate.now(), "dashboard", timeEntries)
        return Response.ok(template.render()).build()
    }
} 