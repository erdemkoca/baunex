package ch.baunex.web

import ch.baunex.project.dto.ProjectDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.project.model.ProjectModel
import ch.baunex.project.model.ProjectStatus
import ch.baunex.user.dto.UserResponseDTO
import ch.baunex.worker.WorkerHandler
import ch.baunex.worker.dto.WorkerRequest
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate
import ch.baunex.project.model.toDTO


@Path("/")
class WebController {

    @Inject
    lateinit var workerHandler: WorkerHandler

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var timeTrackingFacade: TimeTrackingFacade

    private fun getCurrentDate(): LocalDate {
        return LocalDate.now()
    }

    @CheckedTemplate
    object Templates {
        @JvmStatic
        external fun index(projects: List<ProjectDTO>, workers: List<WorkerRequest>, currentDate: LocalDate, activeMenu: String, timeEntries: List<TimeEntryResponseDTO>): TemplateInstance

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
        val workers = workerHandler.getAllWorkers()
        val timeEntries = timeTrackingFacade.getAllTimeEntries()
        val template = Templates.index(projects, workers, LocalDate.now(), "dashboard", timeEntries)
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/projects")
    @Produces(MediaType.TEXT_HTML)
    fun projects(): Response {
        val projects = projectFacade.getAllProjects().map { it }
        val template = Templates.projects(projects, getCurrentDate(), "projects")
        return Response.ok(template.render()).build()
    }


    @GET
    @Path("/projects/new")
    @Produces(MediaType.TEXT_HTML)
    fun newProject(): Response {
        val emptyProject = ProjectDTO(
            id = null,
            name = "",
            client = "",
            budget = 0,
            contact = "",
            startDate = null,
            endDate = null,
            description = "",
            status = ProjectStatus.PLANNED,
            street = "",
            city = ""
        )

        val template = Templates.projectDetail(
            project = emptyProject,
            activeMenu = "projects",
            currentDate = getCurrentDate()
        )

        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/projects/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    fun editProject(@PathParam("id") id: Long): Response {
        val project = projectFacade.getProjectById(id) ?: return Response.status(Response.Status.NOT_FOUND).build()
        val template = Templates.projectDetail(project, "projects", getCurrentDate())
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/projects/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun saveProject(
        @FormParam("id") id: Long?,
        @FormParam("name") name: String,
        @FormParam("client") client: String,
        @FormParam("budget") budget: Int,
        @FormParam("contact") contact: String?,
        @FormParam("startDate") startDate: LocalDate?,
        @FormParam("endDate") endDate: LocalDate?,
        @FormParam("description") description: String?,
        @FormParam("status") status: ProjectStatus?,
        @FormParam("street") street: String?,
        @FormParam("city") city: String?
    ): Response {
        val dto = ProjectDTO(
            id = id,
            name = name,
            client = client,
            budget = budget,
            contact = contact,
            startDate = startDate,
            endDate = endDate,
            description = description,
            status = status ?: ProjectStatus.PLANNED,
            street = street,
            city = city
        )

        if (id == null || id == 0L) {
            projectFacade.createProject(dto)
        } else {
            projectFacade.updateProject(id, dto)
        }

        return Response.seeOther(java.net.URI("/projects")).build()
    }


    @GET
    @Path("/projects/{id}/delete")
    fun deleteProject(@PathParam("id") id: Long): Response {
        projectFacade.deleteProject(id)
        return Response.seeOther(java.net.URI("/projects")).build()
    }

    @GET
    @Path("/projects/{id}")
    @Produces(MediaType.TEXT_HTML)
    fun viewProject(@PathParam("id") id: Long): Response {
        val project = projectFacade.getProjectWithDetails(id) ?: return Response.status(404).build()
        val template = Templates.projectDetail(project, "projects", getCurrentDate())
        return Response.ok(template.render()).build()
    }

} 