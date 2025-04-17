package ch.baunex.project

import ch.baunex.project.dto.ProjectDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.project.model.toDTO
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/project")
class ProjectController {

    @Inject lateinit var projectFacade: ProjectFacade

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun addProject(dto: ProjectDTO): Response {
        val created = projectFacade.createProject(dto)
        return Response.status(Response.Status.CREATED).entity(created).build()
    }

    fun getAllProjects(): List<ProjectDTO> {
        return projectFacade.getAllProjects().map {
            ProjectDTO(
                id = it.id,
                name = it.name,
                budget = it.budget,
                client = it.client,
                contact = it.contact
            )
        }
    }

    fun getProjectById(id: Long): ProjectDTO? {
        return projectFacade.getProjectById(id)?.let {
            ProjectDTO(
                id = it.id,
                name = it.name,
                budget = it.budget,
                client = it.client,
                contact = it.contact
            )
        }
    }

//    TODO: add all dto.
//    @Transactional
//    fun updateProject(id: Long, dto: ProjectDTO): Boolean {
//        val project = projectFacade.getProjectById(id) ?: return false
//        project.name = dto.name
//        project.budget = dto.budget
//        project.client = dto.client
//        project.contact = dto.contact
//        return true
//    }

    @Transactional
    fun deleteProject(id: Long) {
        projectFacade.deleteProject(id)
    }
}
