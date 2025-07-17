package ch.baunex.timetracking.service

import ch.baunex.catalog.model.CatalogItemModel
import ch.baunex.catalog.service.CatalogService
import ch.baunex.timetracking.model.TimeEntryCatalogItemModel
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.repository.TimeEntryCatalogItemRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.jboss.logging.Logger

@ApplicationScoped
class TimeEntryCatalogItemService @Inject constructor(
    private val repository: TimeEntryCatalogItemRepository
) {
    private val log = Logger.getLogger(TimeEntryCatalogItemService::class.java)

    @Transactional
    fun addCatalogItemToTimeEntry(
        timeEntry: TimeEntryModel,
        catalogItem: CatalogItemModel,
        quantity: Int
    ): TimeEntryCatalogItemModel {
        log.info("Adding catalog item ${catalogItem.id} to time entry ${timeEntry.id} (quantity: $quantity)")
        return try {
            val model = TimeEntryCatalogItemModel().apply {
                this.timeEntry = timeEntry
                this.catalogItem = catalogItem
                this.quantity = quantity
                this.unitPrice = catalogItem.unitPrice
            }
            repository.persistAndFlush(model)
            log.info("Added catalog item ${catalogItem.id} to time entry ${timeEntry.id}")
            model
        } catch (e: Exception) {
            log.error("Failed to add catalog item ${catalogItem.id} to time entry ${timeEntry.id}", e)
            throw e
        }
    }

    @Transactional
    fun delete(id: Long): Boolean {
        log.info("Deleting time entry catalog item with ID: $id")
        return try {
            val result = repository.deleteById(id)
            log.info("Deleted time entry catalog item with ID: $id")
            result
        } catch (e: Exception) {
            log.error("Failed to delete time entry catalog item with ID: $id", e)
            throw e
        }
    }

    @Transactional
    fun deleteByTimeEntryId(timeEntryId: Long): Long {
        log.info("Deleting catalog items for time entry ID: $timeEntryId")
        return try {
            val result = repository.deleteByTimeEntryId(timeEntryId)
            log.info("Deleted $result catalog items for time entry ID: $timeEntryId")
            result
        } catch (e: Exception) {
            log.error("Failed to delete catalog items for time entry ID: $timeEntryId", e)
            throw e
        }
    }
} 