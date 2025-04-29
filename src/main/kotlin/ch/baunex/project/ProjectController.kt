package ch.baunex.project

import ch.baunex.project.dto.*
import ch.baunex.project.facade.ProjectFacade
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ProjectController {

    @Inject
    lateinit var projectFacade: ProjectFacade

    /**
     * Create
     */
    @POST
    fun addProject(dto: ProjectCreateDTO): Response {
        val created: ProjectDetailDTO = projectFacade.createProject(dto)
        return Response
            .status(Response.Status.CREATED)
            .entity(created)
            .build()
    }

    /**
     * List
     */
    @GET
    fun listProjects(): List<ProjectListDTO> {
        return projectFacade.getAllProjects()
    }

    /**
     * Detail
     */
    @GET
    @Path("/{id}")
    fun getProject(@PathParam("id") id: Long): Response {
        val detail = projectFacade.getProjectWithDetails(id)
            ?: return Response.status(Response.Status.NOT_FOUND).build()
        return Response.ok(detail).build()
    }

    /**
     * Update
     */
    @PUT
    @Path("/{id}")
    @Transactional
    fun updateProject(
        @PathParam("id") id: Long,
        dto: ProjectUpdateDTO
    ): Response {
        val success = projectFacade.updateProject(id, dto)
        return if (success) Response.noContent().build()
        else          Response.status(Response.Status.NOT_FOUND).build()
    }

    /**
     * Delete
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    fun deleteProject(@PathParam("id") id: Long): Response {
        projectFacade.deleteProject(id)
        return Response.noContent().build()
    }
}
