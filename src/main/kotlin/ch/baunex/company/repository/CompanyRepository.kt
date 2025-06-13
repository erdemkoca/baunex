package ch.baunex.company.repository

import ch.baunex.company.model.CompanyModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CompanyRepository : PanacheRepository<CompanyModel> {
    fun findFirst(): CompanyModel? = find("1=1").firstResult()
} 