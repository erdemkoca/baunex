package ch.baunex.project

import ch.baunex.project.dto.ProjectRequest
import ch.baunex.project.model.ProjectModel
import ch.baunex.project.repository.ProjectRepository
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@RequestScoped
class ProjectHandler {

    @Inject lateinit var projectRepo: ProjectRepository

    @Transactional
    fun saveProject(dto: ProjectRequest) {
        val project = ProjectModel().apply {
            name = dto.name
            budget = dto.budget
            client = dto.client
            contact = dto.contact
        }
        projectRepo.persist(project)
    }



    fun getAllProjects(): List<ProjectModel> {
        return projectRepo.findAll().list()
    }

    @Transactional
    fun deleteProject(id: Long) {
        projectRepo.deleteById(id)
    }

    fun getProjectById(id: Long): ProjectModel? {
        return projectRepo.findById(id)
    }

    @Transactional
    fun updateProject2(id: Long, dto: ProjectRequest): Boolean {
        val existingProject = projectRepo.findById(id) ?: return false
        existingProject.name = dto.name
        existingProject.budget = dto.budget
        existingProject.client = dto.client
        existingProject.contact = dto.contact
        projectRepo.persist(existingProject)
        return true
    }

    @Transactional
    fun updateProject(id: Long, dto: ProjectRequest): Boolean {
        val existingProject = projectRepo.findById(id) ?: return false
        existingProject.name = dto.name
        existingProject.budget = dto.budget
        existingProject.client = dto.client
        existingProject.contact = dto.contact
        return true
    }

    // Keep the original method for backward compatibility if needed
    // fun saveProject(project: Project) {
    //     projectRepo.persist(project)
    // }
}
