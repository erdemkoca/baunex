package ch.baunex.company.repository

import ch.baunex.company.model.CompanySettingsModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@ApplicationScoped
class CompanySettingsRepository : PanacheRepository<CompanySettingsModel> {
    
    fun findFirst(): CompanySettingsModel? {
        return find("1=1").firstResult()
    }
    
    @Transactional
    fun findOrCreateDefault(): CompanySettingsModel {
        return findFirst() ?: createDefault()
    }
    
    private fun createDefault(): CompanySettingsModel {
        val settings = CompanySettingsModel()
        persist(settings)
        return settings
    }
} 