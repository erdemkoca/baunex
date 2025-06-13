package ch.baunex.timetracking.repository

import ch.baunex.timetracking.model.TimeEntryCatalogItemModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TimeEntryCatalogItemRepository : PanacheRepository<TimeEntryCatalogItemModel> {
    
    fun findByTimeEntryId(timeEntryId: Long): List<TimeEntryCatalogItemModel> {
        return find("timeEntry.id", timeEntryId).list()
    }

    fun findByCatalogItemId(catalogItemId: Long): List<TimeEntryCatalogItemModel> {
        return find("catalogItem.id", catalogItemId).list()
    }

    fun deleteByTimeEntryId(timeEntryId: Long): Long {
        return delete("timeEntry.id", timeEntryId)
    }
} 