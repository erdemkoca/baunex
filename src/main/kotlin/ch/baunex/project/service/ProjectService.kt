package ch.baunex.project.service

import ch.baunex.project.dto.ProjectDTO
import ch.baunex.project.model.ProjectModel
import ch.baunex.project.model.toDTO
import ch.baunex.project.model.toModel
import ch.baunex.project.repository.ProjectRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class ProjectService @Inject constructor(
    private val projectRepository: ProjectRepository
) {

    fun getProjectById(id: Long): ProjectModel? {
        return projectRepository.findById(id)
    }

    fun getAllProjects(): List<ProjectModel> = projectRepository.listAll()

    @Transactional
    fun createProject(dto: ProjectDTO): ProjectModel {
        val project = dto.toModel()
        projectRepository.persist(project)
        return project
    }

    @Transactional
    fun updateProject(id: Long, dto: ProjectDTO): ProjectModel? {
        val existing = projectRepository.findById(id) ?: return null

        existing.name = dto.name
        existing.budget = dto.budget
        existing.client = dto.client
        existing.contact = dto.contact

        return existing
    }

    @Transactional
    fun deleteProject(id: Long): Boolean {
        return projectRepository.deleteById(id)
    }
}
