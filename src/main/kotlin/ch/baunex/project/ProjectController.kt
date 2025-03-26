package ch.baunex.project

import ch.baunex.project.dto.ProjectRequest
import ch.baunex.project.dto.ProjectResponse
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.project.model.toDTO
import io.vertx.ext.web.client.WebClient
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/project")
class ProjectController {

    @Inject lateinit var projectHandler: ProjectHandler

    @Inject lateinit var projectFacade: ProjectFacade

//    @POST
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    fun addProject2(dto: ProjectRequest): Response {
//        projectHandler.saveProject(dto)
//        return Response.ok().build()
//    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun addProject(dto: ProjectRequest): Response {
        val created = projectFacade.createProject(dto)
        return Response.status(Response.Status.CREATED).entity(created.toDTO()).build()
    }

    fun getAllProjects(): List<ProjectRequest> {
        return projectFacade.getAllProjects().map {
            ProjectRequest(
                id = it.id,
                name = it.name,
                budget = it.budget,
                client = it.client,
                contact = it.contact
            )
        }
    }

    fun getProjectById(id: Long): ProjectRequest? {
        return projectFacade.getProjectById(id)?.let {
            ProjectRequest(
                id = it.id,
                name = it.name,
                budget = it.budget,
                client = it.client,
                contact = it.contact
            )
        }
    }

    @Transactional
    fun updateProject(id: Long, dto: ProjectRequest): Boolean {
        val project = projectFacade.getProjectById(id) ?: return false
        project.name = dto.name
        project.budget = dto.budget
        project.client = dto.client
        project.contact = dto.contact
        return true
    }

    @Transactional
    fun deleteProject(id: Long) {
        projectFacade.deleteProject(id)
    }
}
