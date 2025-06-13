package ch.baunex.catalog.service

import ch.baunex.catalog.model.ProjectCatalogItemModel
import ch.baunex.catalog.repository.ProjectCatalogItemRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class ProjectCatalogItemService @Inject constructor(
    private val repository: ProjectCatalogItemRepository
) {

    fun getByProjectId(projectId: Long): List<ProjectCatalogItemModel> {
        return repository.findByProjectId(projectId)
    }

    @Transactional
    fun save(model: ProjectCatalogItemModel) {
        repository.persist(model)
    }

    @Transactional
    fun update(id: Long, updated: ProjectCatalogItemModel) {
        val existing = repository.findById(id) ?: return
        existing.itemName = updated.itemName
        existing.quantity = updated.quantity
        existing.unitPrice = updated.unitPrice
        // project shouldn't change, so we skip it
    }

    @Transactional
    fun delete(id: Long) {
        repository.deleteById(id)
    }
}
