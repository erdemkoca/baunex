package ch.baunex.project.service

import ch.baunex.project.model.ProjectModel
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

    fun getAllProjects(): List<ProjectModel> {
        return projectRepository.listAll()
    }

    @Transactional
    fun deleteProject(id: Long): Boolean {
        return projectRepository.deleteById(id)
    }

    @Transactional
    fun saveProject(project: ProjectModel): ProjectModel {
        projectRepository.persist(project)
        return project
    }

    // You can add update logic or mapping with DTOs later as needed
}
