package ch.baunex.timetracking.repository

import ch.baunex.timetracking.model.TimeEntryCatalogItemModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TimeEntryCatalogItemRepository : PanacheRepository<TimeEntryCatalogItemModel> {

    fun deleteByTimeEntryId(timeEntryId: Long): Long {
        return delete("timeEntry.id", timeEntryId)
    }
} 