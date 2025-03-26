package ch.baunex.project.facade

import ch.baunex.project.dto.ProjectRequest
import ch.baunex.project.model.ProjectModel
import ch.baunex.project.service.ProjectService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class ProjectFacade @Inject constructor(
    private val projectService: ProjectService
) {
    fun createProject(dto: ProjectRequest): ProjectModel {
        return projectService.createProject(dto)
    }

    fun getProjectById(id: Long): ProjectModel? = projectService.getProjectById(id)

    fun getAllProjects(): List<ProjectModel> = projectService.getAllProjects()

    fun deleteProject(id: Long) { projectService.deleteProject(id)}

    @Transactional
    fun updateProject(id: Long, dto: ProjectRequest): Boolean {
        val project = projectService.getProjectById(id) ?: return false

        project.name = dto.name
        project.budget = dto.budget
        project.client = dto.client
        project.contact = dto.contact

        // Optional: validate or enrich before save
        projectService.saveProject(project)
        return true
    }
}
