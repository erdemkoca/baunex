package ch.baunex.web

import ch.baunex.project.dto.ProjectDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.project.model.ProjectModel
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
        external fun projectForm(project: ProjectDTO?, currentDate: LocalDate, activeMenu: String): TemplateInstance
        
        @JvmStatic
        external fun workers(workers: List<WorkerRequest>, currentDate: LocalDate, activeMenu: String): TemplateInstance
        
        @JvmStatic
        external fun workerForm(worker: WorkerRequest?, currentDate: LocalDate, activeMenu: String): TemplateInstance

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
            projects: List<ProjectModel>,
            entry: TimeEntryResponseDTO? = null): TemplateInstance

        @JvmStatic
        external fun timetrackingForm(
            entry: TimeEntryResponseDTO?,
            users: List<UserResponseDTO>,
            projects: List<ProjectModel>,
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
        val projects = projectFacade.getAllProjects().map { it.toDTO() }
        val workers = workerHandler.getAllWorkers()
        val timeEntries = timeTrackingFacade.getAllTimeEntries()
        val template = Templates.index(projects, workers, LocalDate.now(), "dashboard", timeEntries)
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/projects")
    @Produces(MediaType.TEXT_HTML)
    fun projects(): Response {
        val projects = projectFacade.getAllProjects().map { it.toDTO() }
        val template = Templates.projects(projects, getCurrentDate(), "projects")
        return Response.ok(template.render()).build()
    }


    @GET
    @Path("/projects/new")
    @Produces(MediaType.TEXT_HTML)
    fun newProject(): Response {
        val template = Templates.projectForm(null, getCurrentDate(), "projects")
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/projects/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    fun editProject(@PathParam("id") id: Long): Response {
        val project = projectFacade.getProjectById(id)?.toDTO()
        val template = Templates.projectForm(project, getCurrentDate(), "projects")
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/projects/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun saveProject(
        @FormParam("id") id: Long?,
        @FormParam("name") name: String,
        @FormParam("budget") budget: Int,
        @FormParam("client") client: String,
        @FormParam("contact") contact: String?
    ): Response {
        val project = ProjectDTO(name, budget, client, contact, id)
        
        if (id == null || id == 0L) {
            projectFacade.createProject(project)
        } else {
            projectFacade.updateProject(id, project)
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
    @Path("/workers")
    @Produces(MediaType.TEXT_HTML)
    fun workers(): Response {
        val template = Templates.workers(workerHandler.getAllWorkers(), getCurrentDate(), "workers")
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/workers/new")
    @Produces(MediaType.TEXT_HTML)
    fun newWorker(): Response {
        val template = Templates.workerForm(null, getCurrentDate(), "workers")
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/workers/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    fun editWorker(@PathParam("id") id: Long): Response {
        val worker = workerHandler.getWorkerById(id)
        val template = Templates.workerForm(worker, getCurrentDate(), "workers")
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/workers/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun saveWorker(
        @FormParam("id") id: Long?,
        @FormParam("firstName") firstName: String,
        @FormParam("lastName") lastName: String,
        @FormParam("email") email: String,
        @FormParam("phone") phone: String,
        @FormParam("position") position: String,
        @FormParam("hourlyRate") hourlyRate: Double
    ): Response {
        val worker = WorkerRequest(firstName, lastName, email, phone, position, hourlyRate, id)
        
        if (id == null || id == 0L) {
            workerHandler.saveWorker(worker)
        } else {
            workerHandler.updateWorker(id, worker)
        }
        
        return Response.seeOther(java.net.URI("/workers")).build()
    }

    @GET
    @Path("/workers/{id}/delete")
    fun deleteWorker(@PathParam("id") id: Long): Response {
        workerHandler.deleteWorker(id)
        return Response.seeOther(java.net.URI("/workers")).build()
    }
} 