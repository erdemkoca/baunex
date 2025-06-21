package ch.baunex.timetracking.repository

import ch.baunex.timetracking.model.HolidayModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate

@ApplicationScoped
class HolidayRepository : PanacheRepository<HolidayModel> {
    fun findByEmployeeAndDateRange(employeeId: Long, start: LocalDate, end: LocalDate): List<HolidayModel> {
        return list(
            "employee.id = ?1 AND (startDate <= ?3 AND endDate >= ?2)",
            employeeId, start, end
        )
    }
}
