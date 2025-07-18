package ch.baunex.timetracking.service

import ch.baunex.catalog.model.CatalogItemModel
import ch.baunex.catalog.service.CatalogService
import ch.baunex.timetracking.model.TimeEntryCatalogItemModel
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.repository.TimeEntryCatalogItemRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class TimeEntryCatalogItemService @Inject constructor(
    private val repository: TimeEntryCatalogItemRepository,
    private val catalogService: CatalogService
) {
    fun getByTimeEntryId(timeEntryId: Long): List<TimeEntryCatalogItemModel> {
        return repository.findByTimeEntryId(timeEntryId)
    }

    fun getByCatalogItemId(catalogItemId: Long): List<TimeEntryCatalogItemModel> {
        return repository.findByCatalogItemId(catalogItemId)
    }

    @Transactional
    fun addCatalogItemToTimeEntry(
        timeEntry: TimeEntryModel,
        catalogItem: CatalogItemModel,
        quantity: Int
    ): TimeEntryCatalogItemModel {
        val model = TimeEntryCatalogItemModel().apply {
            this.timeEntry = timeEntry
            this.catalogItem = catalogItem
            this.quantity = quantity
            this.unitPrice = catalogItem.unitPrice
        }
        repository.persistAndFlush(model)
        return model
    }

    @Transactional
    fun updateQuantity(id: Long, quantity: Int): TimeEntryCatalogItemModel? {
        val model = repository.findById(id) ?: return null
        model.quantity = quantity
        return model
    }

    @Transactional
    fun delete(id: Long): Boolean {
        return repository.deleteById(id)
    }

    @Transactional
    fun deleteByTimeEntryId(timeEntryId: Long): Long {
        return repository.deleteByTimeEntryId(timeEntryId)
    }
} 