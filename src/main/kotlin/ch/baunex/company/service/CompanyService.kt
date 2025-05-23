package ch.baunex.company.service

import ch.baunex.company.model.CompanyModel
import ch.baunex.company.repository.CompanyRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class CompanyService @Inject constructor(
    private val repository: CompanyRepository
) {
    fun getCompany(): CompanyModel? = repository.findFirst()

    @Transactional
    fun save(company: CompanyModel) {
        repository.persist(company)
    }

    @Transactional
    fun update(id: Long, updated: CompanyModel) {
        val existing = repository.findById(id) ?: return
        existing.apply {
            name = updated.name
            street = updated.street
            city = updated.city
            zipCode = updated.zipCode
            country = updated.country
            phone = updated.phone
            email = updated.email
            website = updated.website
            iban = updated.iban
            bic = updated.bic
            bankName = updated.bankName
            vatNumber = updated.vatNumber
            taxNumber = updated.taxNumber
            logo = updated.logo
            defaultInvoiceFooter = updated.defaultInvoiceFooter
            defaultInvoiceTerms = updated.defaultInvoiceTerms
            defaultVatRate = updated.defaultVatRate
        }
    }

    @Transactional
    fun delete(id: Long) = repository.deleteById(id)
} 