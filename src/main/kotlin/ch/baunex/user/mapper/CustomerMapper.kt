package ch.baunex.user.mapper

import ch.baunex.user.dto.CustomerCreateDTO
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.model.CustomerModel
import ch.baunex.user.model.PersonModel
import ch.baunex.user.model.PersonDetails

fun CustomerModel.toCustomerDTO(): CustomerDTO = CustomerDTO(
    id              = this.id!!,
    firstName       = this.person.firstName,
    lastName        = this.person.lastName,
    email           = this.person.email,
    customerNumber  = this.customerNumber,
    companyName     = this.companyName,
    paymentTerms    = this.paymentTerms,
    creditLimit     = this.creditLimit,
    industry        = this.industry,
    discountRate    = this.discountRate,
    preferredLanguage = this.preferredLanguage,
    marketingConsent  = this.marketingConsent,
    taxId             = this.taxId,
    createdAt         = this.createdAt,
    updatedAt         = this.updatedAt
)

fun CustomerCreateDTO.toCustomerModel(): CustomerModel {
    val person = PersonModel().apply {
        firstName = this@toCustomerModel.firstName
        lastName  = this@toCustomerModel.lastName
        email     = this@toCustomerModel.email
        details   = PersonDetails(
            street  = this@toCustomerModel.street,
            city    = this@toCustomerModel.city,
            zipCode = this@toCustomerModel.zipCode,
            country = this@toCustomerModel.country,
            phone   = this@toCustomerModel.phone
        )
    }
    return CustomerModel().apply {
        this.person         = person
        this.customerNumber = this@toCustomerModel.customerNumber
        this.companyName    = this@toCustomerModel.companyName
        this.paymentTerms   = this@toCustomerModel.paymentTerms
        this.creditLimit    = this@toCustomerModel.creditLimit
        this.industry       = this@toCustomerModel.industry
        this.discountRate   = this@toCustomerModel.discountRate
        this.preferredLanguage = this@toCustomerModel.preferredLanguage
        this.marketingConsent  = this@toCustomerModel.marketingConsent
        this.taxId             = this@toCustomerModel.taxId
    }
}

fun CustomerCreateDTO.applyTo(customer: CustomerModel): CustomerModel {
    customer.person.apply {
        firstName = this@applyTo.firstName
        lastName  = this@applyTo.lastName
        email     = this@applyTo.email
        details.street  = this@applyTo.street
        details.city    = this@applyTo.city
        details.zipCode = this@applyTo.zipCode
        details.country = this@applyTo.country
        details.phone   = this@applyTo.phone
    }

    customer.apply {
        customerNumber    = this@applyTo.customerNumber
        companyName       = this@applyTo.companyName
        paymentTerms      = this@applyTo.paymentTerms
        creditLimit       = this@applyTo.creditLimit
        industry          = this@applyTo.industry
        discountRate      = this@applyTo.discountRate
        preferredLanguage = this@applyTo.preferredLanguage
        marketingConsent  = this@applyTo.marketingConsent
        taxId             = this@applyTo.taxId
    }

    return customer
}