package ch.baunex.timetracking.repository

import ch.baunex.timetracking.model.TimeEntryModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TimeEntryRepository : PanacheRepository<TimeEntryModel> {

    fun findByUserId(userId: Long): List<TimeEntryModel> {
        return find("user.id", userId).list()
    }

    fun findByProjectId(projectId: Long): List<TimeEntryModel> {
        return find("project.id", projectId).list()
    }

    fun findByUserIdAndDate(userId: Long, date: java.time.LocalDate): List<TimeEntryModel> {
        return find("user.id = ?1 and date = ?2", userId, date).list()
    }

    fun deleteAllByUserId(userId: Long): Long {
        return delete("user.id", userId)
    }

    fun deleteAllByProjectId(projectId: Long): Long {
        return delete("project.id", projectId)
    }
}
