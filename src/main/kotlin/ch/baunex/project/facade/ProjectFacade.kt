package ch.baunex.project.facade

import ch.baunex.project.dto.ProjectDTO
import ch.baunex.project.model.toDTO
import ch.baunex.project.service.ProjectService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class ProjectFacade @Inject constructor(
    private val projectService: ProjectService
) {
    fun createProject(dto: ProjectDTO): ProjectDTO {
        return projectService.createProject(dto).toDTO()
    }

    fun getProjectById(id: Long): ProjectDTO? {
        return projectService.getProjectById(id)?.toDTO()
    }

    fun getAllProjects(): List<ProjectDTO> {
        return projectService.getAllProjects().map { it.toDTO() }
    }


    fun deleteProject(id: Long) {
        projectService.deleteProject(id)
    }

    @Transactional
    fun updateProject(id: Long, dto: ProjectDTO): Boolean {
        val updated = projectService.updateProject(id, dto)
        return updated != null
    }

    fun getProjectWithDetails(id: Long): ProjectDTO? {
        return projectService.getProjectWithEntries(id)?.toDTO()
    }

}
