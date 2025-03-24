package ch.baunex.project

import ch.baunex.project.dto.ProjectRequest
import io.quarkus.hibernate.orm.panache.kotlin.PanacheQuery
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@RequestScoped
class ProjectHandler {

    @Inject lateinit var projectRepo: ProjectRepository

    @Transactional
    fun saveProject(dto: ProjectRequest) {
        projectRepo.persist(dto)
    }

    fun getAllProjects(): List<ProjectRequest> {
        return projectRepo.findAll().list()
    }
    
    @Transactional
    fun deleteProject(id: Long) {
        projectRepo.deleteById(id)
    }
    
    fun getProjectById(id: Long): ProjectRequest? {
        return projectRepo.findById(id)
    }
    
    @Transactional
    fun updateProject(id: Long, dto: ProjectRequest): Boolean {
        val existingProject = projectRepo.findById(id) ?: return false
        existingProject.name = dto.name
        existingProject.budget = dto.budget
        existingProject.client = dto.client
        existingProject.contact = dto.contact
        projectRepo.persist(existingProject)
        return true
    }

    // Keep the original method for backward compatibility if needed
    // fun saveProject(project: Project) {
    //     projectRepo.persist(project)
    // }
}
