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
    fun getAllProjects(): Response {
        return Response.ok(ProjectResponse(projectHandler.getAllProjects())).build()
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getProjectById(@PathParam("id") id: Long): Response {
        val project = projectHandler.getProjectById(id) ?: return Response.status(Response.Status.NOT_FOUND).build()
        return Response.ok(project).build()
    }
    
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun updateProject(@PathParam("id") id: Long, dto: ProjectRequest): Response {
        val updated = projectHandler.updateProject(id, dto)
        return if (updated) Response.ok().build() else Response.status(Response.Status.NOT_FOUND).build()
    }
    
    @DELETE
    @Path("/{id}")
    fun deleteProject(@PathParam("id") id: Long): Response {
        projectHandler.deleteProject(id)
        return Response.ok().build()
    }
}
