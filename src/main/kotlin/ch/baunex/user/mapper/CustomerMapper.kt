package ch.baunex.user.mapper

import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.dto.CustomerCreateDTO
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.model.CustomerContact
import ch.baunex.user.model.CustomerModel
import ch.baunex.user.model.PersonModel
import ch.baunex.user.model.PersonDetails
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CustomerMapper {
    fun toDTO(model: CustomerModel): CustomerDTO {
        return CustomerDTO(
            id              = model.id!!,
            firstName       = model.person.firstName,
            lastName        = model.person.lastName,
            email           = model.person.email,
            street = model.person.details.street,
            city = model.person.details.city,
            zipCode = model.person.details.zipCode,
            country = model.person.details.country,
            phone = model.person.details.phone,
            customerNumber  = model.customerNumber,
            formattedCustomerNumber = "CUST-" + model.customerNumber.toString().padStart(4, '0'),
            companyName     = model.companyName,
            paymentTerms    = model.paymentTerms,
            creditLimit     = model.creditLimit,
            industry        = model.industry,
            discountRate    = model.discountRate,
            preferredLanguage = model.preferredLanguage,
            marketingConsent  = model.marketingConsent,
            taxId             = model.taxId,
            createdAt         = model.createdAt,
            updatedAt         = model.updatedAt,
            contacts         = model.contacts.map { toContactDTO(it) }
        )
    }

    fun toEntity(dto: CustomerDTO): CustomerModel {
        val person = PersonModel().apply {
            firstName = dto.firstName
            lastName = dto.lastName
            email = dto.email
            details = PersonDetails(
                street = dto.street,
                city = dto.city,
                zipCode = dto.zipCode,
                country = dto.country,
                phone = dto.phone
            )
        }
        
        return CustomerModel().apply {
            id = dto.id
            this.person = person
            customerNumber = dto.customerNumber
            companyName = dto.companyName
            paymentTerms = dto.paymentTerms
            creditLimit = dto.creditLimit
            industry = dto.industry
            discountRate = dto.discountRate
            preferredLanguage = dto.preferredLanguage
            marketingConsent = dto.marketingConsent
            taxId = dto.taxId
        }
    }

    fun toEntity(dto: CustomerCreateDTO): CustomerModel {
        val person = PersonModel().apply {
            firstName = dto.firstName
            lastName = dto.lastName
            email = dto.email
            details = PersonDetails(
                street = dto.street,
                city = dto.city,
                zipCode = dto.zipCode,
                country = dto.country,
                phone = dto.phone
            )
        }
        
        return CustomerModel().apply {
            this.person = person
            companyName = dto.companyName
            paymentTerms = dto.paymentTerms
            creditLimit = dto.creditLimit
            industry = dto.industry
            discountRate = dto.discountRate
            preferredLanguage = dto.preferredLanguage
            marketingConsent = dto.marketingConsent
            taxId = dto.taxId
        }
    }

    fun applyTo(dto: CustomerCreateDTO, customer: CustomerModel): CustomerModel {
        customer.person.apply {
            firstName = dto.firstName
            lastName = dto.lastName
            email = dto.email
            details.street = dto.street
            details.city = dto.city
            details.zipCode = dto.zipCode
            details.country = dto.country
            details.phone = dto.phone
        }

        customer.apply {
            companyName = dto.companyName
            paymentTerms = dto.paymentTerms
            creditLimit = dto.creditLimit
            industry = dto.industry
            discountRate = dto.discountRate
            preferredLanguage = dto.preferredLanguage
            marketingConsent = dto.marketingConsent
            taxId = dto.taxId
        }

        return customer
    }

    fun toContactDTO(contact: CustomerContact): CustomerContactDTO {
        return CustomerContactDTO(
            id         = contact.id!!,
            personId   = contact.contactPerson.id!!,
            firstName  = contact.contactPerson.firstName,
            lastName   = contact.contactPerson.lastName,
            email      = contact.contactPerson.email,
            street     = contact.contactPerson.details.street,
            city       = contact.contactPerson.details.city,
            zipCode    = contact.contactPerson.details.zipCode,
            country    = contact.contactPerson.details.country,
            phone      = contact.contactPerson.details.phone,
            role       = contact.role,
            isPrimary  = contact.isPrimary,
            createdAt  = contact.createdAt,
            updatedAt  = contact.updatedAt
        )
    }
}