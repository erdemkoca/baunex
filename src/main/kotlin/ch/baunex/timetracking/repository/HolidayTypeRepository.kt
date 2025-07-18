package ch.baunex.timetracking.repository

import ch.baunex.timetracking.model.HolidayTypeModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger

@ApplicationScoped
class HolidayTypeRepository : PanacheRepository<HolidayTypeModel> {
    private val log = Logger.getLogger(HolidayTypeRepository::class.java)

    fun findActive(): List<HolidayTypeModel> {
        log.debug("Finding active holiday types")
        return find("FROM HolidayTypeModel h WHERE h.active = true ORDER BY h.sortOrder").list<HolidayTypeModel>()
    }

    fun findActiveByCode(code: String): HolidayTypeModel? {
        log.debug("Finding active holiday type by code: $code")
        return find("code = ?1 and active = ?2", code, true).firstResult<HolidayTypeModel>()
    }

    fun existsByCode(code: String): Boolean {
        log.debug("Checking if holiday type exists by code: $code")
        return count("code", code) > 0
    }

    fun findNextSortOrder(): Int {
        log.debug("Finding next sort order for holiday type")
        val allTypes = find("FROM HolidayTypeModel h").list<HolidayTypeModel>()
        val maxSortOrder = allTypes.maxOfOrNull { it.sortOrder } ?: 0
        return maxSortOrder + 1
    }

    fun listAllOrdered(): List<HolidayTypeModel> {
        log.debug("Finding all holiday types ordered by sort order")
        return find("FROM HolidayTypeModel h ORDER BY h.sortOrder").list<HolidayTypeModel>()
    }
} 