package ch.baunex.project.repository

import ch.baunex.project.model.ProjectModel
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProjectRepository : PanacheRepository<ProjectModel> {

    fun findByIdWithTimeEntries(id: Long): ProjectModel? {
        return find(
            "FROM ProjectModel p LEFT JOIN FETCH p.timeEntries WHERE p.id = ?1",
            id
        ).firstResult()
    }
    
    fun findByIdWithoutTimeEntries(id: Long): ProjectModel? {
        return find("FROM ProjectModel p WHERE p.id = ?1", id).firstResult()
    }
    
    fun listAllProjects(): List<ProjectModel> =
        find("FROM ProjectModel p").list()
}
