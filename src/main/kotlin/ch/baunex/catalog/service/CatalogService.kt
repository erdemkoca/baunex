package ch.baunex.catalog.service

import ch.baunex.catalog.model.CatalogItemModel
import ch.baunex.catalog.repository.CatalogRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class CatalogService @Inject constructor(
    private val repo: CatalogRepository
) {
    fun getAll(): List<CatalogItemModel> = repo.listAll()

    fun findById(id: Long): CatalogItemModel? = repo.findById(id)

    @Transactional
    fun save(item: CatalogItemModel) = repo.persist(item)

    @Transactional
    fun update(id: Long, updated: CatalogItemModel) {
        val existing = repo.findById(id) ?: return
        existing.name = updated.name
        existing.unitPrice = updated.unitPrice
        existing.description = updated.description
    }

    fun delete(id: Long) = repo.deleteById(id)
}
