package ch.baunex.company.mapper

import ch.baunex.company.dto.CompanyDTO
import ch.baunex.company.model.CompanyModel

fun CompanyModel.toCompanyDTO(): CompanyDTO = CompanyDTO(
    id = this.id,
    name = this.name,
    street = this.street,
    city = this.city,
    zipCode = this.zipCode,
    country = this.country,
    phone = this.phone,
    email = this.email,
    website = this.website,
    iban = this.iban,
    bic = this.bic,
    bankName = this.bankName,
    vatNumber = this.vatNumber,
    taxNumber = this.taxNumber,
    logo = this.logo,
    defaultInvoiceFooter = this.defaultInvoiceFooter,
    defaultInvoiceTerms = this.defaultInvoiceTerms,
    defaultVatRate = this.defaultVatRate
)

fun CompanyDTO.toCompanyModel(): CompanyModel = CompanyModel().apply {
    name = this@toCompanyModel.name
    street = this@toCompanyModel.street
    city = this@toCompanyModel.city
    zipCode = this@toCompanyModel.zipCode
    country = this@toCompanyModel.country
    phone = this@toCompanyModel.phone
    email = this@toCompanyModel.email
    website = this@toCompanyModel.website
    iban = this@toCompanyModel.iban
    bic = this@toCompanyModel.bic
    bankName = this@toCompanyModel.bankName
    vatNumber = this@toCompanyModel.vatNumber
    taxNumber = this@toCompanyModel.taxNumber
    logo = this@toCompanyModel.logo
    defaultInvoiceFooter = this@toCompanyModel.defaultInvoiceFooter
    defaultInvoiceTerms = this@toCompanyModel.defaultInvoiceTerms
    defaultVatRate = this@toCompanyModel.defaultVatRate
} 