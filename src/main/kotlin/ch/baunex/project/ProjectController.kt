package ch.baunex.project

import ch.baunex.project.dto.ProjectRequest
import ch.baunex.project.dto.ProjectResponse
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/project")
class ProjectController {

    @Inject lateinit var projectHandler: ProjectHandler

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun addProject(dto: ProjectRequest): Response {
        projectHandler.saveProject(dto)
        return Response.ok().build()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getAllProject(): Response {
        return Response.ok(ProjectResponse(projectHandler.getAllProjects())).build()
    }

}
