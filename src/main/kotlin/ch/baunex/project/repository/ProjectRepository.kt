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
}
