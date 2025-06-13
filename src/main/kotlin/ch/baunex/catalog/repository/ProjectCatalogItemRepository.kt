package ch.baunex.catalog.repository

import ch.baunex.catalog.model.ProjectCatalogItemModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProjectCatalogItemRepository : PanacheRepository<ProjectCatalogItemModel> {

    fun findByProjectId(projectId: Long): List<ProjectCatalogItemModel> {
        return list("project.id", projectId)
    }
}