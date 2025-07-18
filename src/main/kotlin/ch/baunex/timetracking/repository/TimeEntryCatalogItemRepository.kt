package ch.baunex.timetracking.repository

import ch.baunex.timetracking.model.TimeEntryCatalogItemModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger

@ApplicationScoped
class TimeEntryCatalogItemRepository : PanacheRepository<TimeEntryCatalogItemModel> {
    private val log = Logger.getLogger(TimeEntryCatalogItemRepository::class.java)

    fun deleteByTimeEntryId(timeEntryId: Long): Long {
        log.debug("Deleting catalog items for time entry ID: $timeEntryId")
        return delete("timeEntry.id", timeEntryId)
    }
} 