package ch.baunex.timetracking.repository

import ch.baunex.timetracking.model.HolidayDefinitionModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate

@ApplicationScoped
class HolidayDefinitionRepository : PanacheRepository<HolidayDefinitionModel> {
    
    fun findByYear(year: Int): List<HolidayDefinitionModel> {
        return list("year = ?1 AND active = true ORDER BY date", year)
    }
    
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<HolidayDefinitionModel> {
        return list("date >= ?1 AND date <= ?2 AND active = true ORDER BY date", startDate, endDate)
    }
    
    fun findByYearAndCanton(year: Int, canton: String?): List<HolidayDefinitionModel> {
        return if (canton != null) {
            list("year = ?1 AND (canton = ?2 OR canton IS NULL) AND active = true ORDER BY date", year, canton)
        } else {
            list("year = ?1 AND canton IS NULL AND active = true ORDER BY date", year)
        }
    }
    
    fun findWorkFreeHolidaysByDateRange(startDate: LocalDate, endDate: LocalDate): List<HolidayDefinitionModel> {
        return list("date >= ?1 AND date <= ?2 AND isWorkFree = true AND active = true ORDER BY date", startDate, endDate)
    }
    
    fun existsByYear(year: Int): Boolean {
        return count("year = ?1", year) > 0
    }
} 