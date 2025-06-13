package ch.baunex.timetracking.repository

import ch.baunex.timetracking.model.TimeEntryModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TimeEntryRepository : PanacheRepository<TimeEntryModel> {

    fun findByUserId(employeeId: Long): List<TimeEntryModel> {
        return find("employee.id", employeeId).list()
    }

    fun findByProjectId(projectId: Long): List<TimeEntryModel> {
        return find("project.id", projectId).list()
    }

    fun findByUserIdAndDate(employeeId: Long, date: java.time.LocalDate): List<TimeEntryModel> {
        return find("employee.id = ?1 and date = ?2", employeeId, date).list()
    }

    fun deleteAllByUserId(employeeId: Long): Long {
        return delete("employee.id", employeeId)
    }

    fun deleteAllByProjectId(projectId: Long): Long {
        return delete("project.id", projectId)
    }

    fun findByIdWithCatalogItems(id: Long): TimeEntryModel? {
        return find(
            "FROM TimeEntryModel t LEFT JOIN FETCH t.usedCatalogItems WHERE t.id = ?1",
            id
        ).firstResult()
    }
}
