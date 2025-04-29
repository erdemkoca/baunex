package ch.baunex.project.facade

import ch.baunex.project.dto.*
import ch.baunex.project.mapper.toDetailDTO
import ch.baunex.project.mapper.toListDTO
import ch.baunex.project.service.ProjectService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class ProjectFacade @Inject constructor(
    private val projectService: ProjectService
) {

    @Transactional
    fun createProject(createDto: ProjectCreateDTO): ProjectDetailDTO {
        val created = projectService.createProject(createDto)
        return created.toDetailDTO()
    }

    @Transactional
    fun updateProject(id: Long, updateDto: ProjectUpdateDTO): Boolean {
        return projectService.updateProject(id, updateDto)?.let { true } ?: false
    }

    fun getAllProjects(): List<ProjectListDTO> {
        return projectService.getAllProjects()
            .map { it.toListDTO() }
    }

    fun getProjectWithDetails(id: Long): ProjectDetailDTO? {
        return projectService.getProjectWithEntries(id)
            ?.toDetailDTO()
    }

    @Transactional
    fun deleteProject(id: Long) {
        projectService.deleteProject(id)
    }
}
