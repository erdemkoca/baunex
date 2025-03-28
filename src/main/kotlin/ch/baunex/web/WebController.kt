package ch.baunex.web

import ch.baunex.project.ProjectHandler
import ch.baunex.project.dto.ProjectRequest
import ch.baunex.worker.WorkerHandler
import ch.baunex.worker.dto.WorkerRequest
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
    lateinit var projectHandler: ProjectHandler

    @Inject
    lateinit var workerHandler: WorkerHandler

    private fun getCurrentDate(): LocalDate {
        return LocalDate.now()
    }

    @CheckedTemplate
    object Templates {
        @JvmStatic
        external fun index(projects: List<ProjectRequest>, workers: List<WorkerRequest>, currentDate: LocalDate, activeMenu: String): TemplateInstance
        
        @JvmStatic
        external fun projects(projects: List<ProjectRequest>, currentDate: LocalDate, activeMenu: String): TemplateInstance
        
        @JvmStatic
        external fun projectForm(project: ProjectRequest?, currentDate: LocalDate, activeMenu: String): TemplateInstance
        
        @JvmStatic
        external fun workers(workers: List<WorkerRequest>, currentDate: LocalDate, activeMenu: String): TemplateInstance
        
        @JvmStatic
        external fun workerForm(worker: WorkerRequest?, currentDate: LocalDate, activeMenu: String): TemplateInstance
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
        val template = Templates.index(projectHandler.getAllProjects(), workerHandler.getAllWorkers(), getCurrentDate(), "dashboard")
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/projects")
    @Produces(MediaType.TEXT_HTML)
    fun projects(): Response {
        val template = Templates.projects(projectHandler.getAllProjects(), getCurrentDate(), "projects")
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
        val project = projectHandler.getProjectById(id)
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
        val project = ProjectRequest(name, budget, client, contact, id)
        
        if (id == null || id == 0L) {
            projectHandler.saveProject(project)
        } else {
            projectHandler.updateProject(id, project)
        }
        
        return Response.seeOther(java.net.URI("/projects")).build()
    }

    @GET
    @Path("/projects/{id}/delete")
    fun deleteProject(@PathParam("id") id: Long): Response {
        projectHandler.deleteProject(id)
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