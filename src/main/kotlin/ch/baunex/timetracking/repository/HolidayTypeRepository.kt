package ch.baunex.timetracking.repository

import ch.baunex.timetracking.model.HolidayTypeModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class HolidayTypeRepository : PanacheRepository<HolidayTypeModel> {

    fun findActive(): List<HolidayTypeModel> {
        return find("active", true).list<HolidayTypeModel>().sortedBy { it.sortOrder }
    }

    fun findActiveByCode(code: String): HolidayTypeModel? {
        return find("code = ?1 and active = ?2", code, true).firstResult<HolidayTypeModel>()
    }

    fun existsByCode(code: String): Boolean {
        return count("code", code) > 0
    }

    fun findNextSortOrder(): Int {
        val allTypes = listAll()
        val maxSortOrder = allTypes.maxOfOrNull { it.sortOrder } ?: 0
        return maxSortOrder + 1
    }

    fun listAllOrdered(): List<HolidayTypeModel> {
        return listAll().sortedBy { it.sortOrder }
    }
} 