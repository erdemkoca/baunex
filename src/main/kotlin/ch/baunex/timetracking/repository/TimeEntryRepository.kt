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

    fun findRelatedEntries(employeeId: Long, projectId: Long, date: LocalDate): List<TimeEntryModel> {
        return list("employee.id = ?1 AND project.id = ?2 AND date = ?3 ORDER BY startTime", employeeId, projectId, date)
    }
}


