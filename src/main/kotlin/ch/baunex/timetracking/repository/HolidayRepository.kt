package ch.baunex.timetracking.repository

import ch.baunex.timetracking.model.ApprovalStatus
import ch.baunex.timetracking.model.HolidayModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate
import org.jboss.logging.Logger

@ApplicationScoped
class HolidayRepository : PanacheRepository<HolidayModel> {
    private val log = Logger.getLogger(HolidayRepository::class.java)
    
    /**
     * Find holidays for a specific employee within a date range
     */
    fun findByEmployeeAndDateRange(
        employeeId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<HolidayModel> {
        log.debug("Finding holidays for employee $employeeId from $startDate to $endDate")
        return find("employee.id = ?1 and " +
                "((startDate between ?2 and ?3) or " +
                "(endDate between ?2 and ?3) or " +
                "(startDate <= ?2 and endDate >= ?3))",
                employeeId, startDate, endDate).list()
    }
    
    fun findByStatus(status: ApprovalStatus): List<HolidayModel> {
        log.debug("Finding holidays with status: $status")
        return list("approvalStatus = ?1", status)
    }
    
    /**
     * Get all holidays without loading collections to avoid Hibernate warnings
     */
    fun findAllWithoutCollections(): List<HolidayModel> {
        log.debug("Finding all holidays without collections")
        return find("FROM HolidayModel h").list()
    }
}
