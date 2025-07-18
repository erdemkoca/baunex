package ch.baunex.timetracking.repository

import ch.baunex.timetracking.model.TimeEntryModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate

@ApplicationScoped
class TimeEntryRepository : PanacheRepository<TimeEntryModel> {

    fun findByEmployeeAndDateRange(employeeId: Long, start: LocalDate, end: LocalDate): List<TimeEntryModel> {
        return list("employee.id = ?1 AND date BETWEEN ?2 AND ?3", employeeId, start, end)
    }
    
    /**
     * Get all time entries without loading collections to avoid Hibernate warnings
     */
    fun findAllWithoutCollections(): List<TimeEntryModel> {
        return find("FROM TimeEntryModel t").list()
    }
    
    /**
     * Find time entry by ID without loading collections to avoid Hibernate warnings
     */
    fun findByIdWithoutCollections(id: Long): TimeEntryModel? {
        return find("FROM TimeEntryModel t WHERE t.id = ?1", id).firstResult()
    }
}


