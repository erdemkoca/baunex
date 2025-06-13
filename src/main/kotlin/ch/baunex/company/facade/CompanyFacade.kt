package ch.baunex.company.facade

import ch.baunex.company.dto.CompanyDTO
import ch.baunex.company.mapper.toCompanyDTO
import ch.baunex.company.mapper.toCompanyModel
import ch.baunex.company.service.CompanyService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class CompanyFacade @Inject constructor(
    private val service: CompanyService
) {
    fun getCompany(): CompanyDTO? = service.getCompany()?.toCompanyDTO()

    fun createCompany(dto: CompanyDTO): CompanyDTO {
        val model = dto.toCompanyModel()
        service.save(model)
        return model.toCompanyDTO()
    }

    fun updateCompany(id: Long, dto: CompanyDTO) {
        service.update(id, dto.toCompanyModel())
    }

    fun deleteCompany(id: Long) = service.delete(id)
} 