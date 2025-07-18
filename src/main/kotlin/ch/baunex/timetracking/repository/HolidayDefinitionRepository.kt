package ch.baunex.timetracking.repository

import ch.baunex.timetracking.model.HolidayDefinitionModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate
import org.jboss.logging.Logger

@ApplicationScoped
class HolidayDefinitionRepository : PanacheRepository<HolidayDefinitionModel> {
    private val log = Logger.getLogger(HolidayDefinitionRepository::class.java)
    
    fun findByYear(year: Int): List<HolidayDefinitionModel> {
        log.debug("Finding holiday definitions for year: $year")
        return list("year = ?1 AND active = true ORDER BY date", year)
    }
    
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<HolidayDefinitionModel> {
        log.debug("Finding holiday definitions from $startDate to $endDate")
        return list("date >= ?1 AND date <= ?2 AND active = true ORDER BY date", startDate, endDate)
    }
    
    fun findByYearAndCanton(year: Int, canton: String?): List<HolidayDefinitionModel> {
        log.debug("Finding holiday definitions for year $year and canton: $canton")
        return if (canton != null) {
            list("year = ?1 AND (canton = ?2 OR canton IS NULL) AND active = true ORDER BY date", year, canton)
        } else {
            list("year = ?1 AND canton IS NULL AND active = true ORDER BY date", year)
        }
    }
    
    fun findWorkFreeHolidaysByDateRange(startDate: LocalDate, endDate: LocalDate): List<HolidayDefinitionModel> {
        log.debug("Finding work-free holiday definitions from $startDate to $endDate")
        return list("date >= ?1 AND date <= ?2 AND isWorkFree = true AND active = true ORDER BY date", startDate, endDate)
    }
    
    fun existsByYear(year: Int): Boolean {
        log.debug("Checking if holiday definitions exist for year: $year")
        return count("year = ?1", year) > 0
    }
} 