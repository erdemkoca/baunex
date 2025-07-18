package ch.baunex.user.mapper

import ch.baunex.user.dto.CustomerContactCreateDTO
import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.dto.CustomerContactUpdateDTO
import ch.baunex.user.model.CustomerContact
import ch.baunex.user.model.CustomerModel
import ch.baunex.user.model.PersonDetails
import ch.baunex.user.model.PersonModel
import ch.baunex.web.forms.CustomerContactForm

fun CustomerContact.toDTO(): CustomerContactDTO = CustomerContactDTO(
    id         = this.id,
    personId   = this.contactPerson.id,
    firstName  = this.contactPerson.firstName,
    lastName   = this.contactPerson.lastName,
    email      = this.contactPerson.email,
    street     = this.contactPerson.details.street,
    city       = this.contactPerson.details.city,
    zipCode    = this.contactPerson.details.zipCode,
    country    = this.contactPerson.details.country,
    phone      = this.contactPerson.details.phone,
    role       = this.role,
    isPrimary  = this.isPrimary,
    createdAt  = this.createdAt,
    updatedAt  = this.updatedAt
)

fun CustomerContactCreateDTO.toModel(customer: CustomerModel): CustomerContact {
    // create a brand‐new PersonModel
    val person = PersonModel().apply {
        firstName = this@toModel.firstName
        lastName  = this@toModel.lastName
        email     = this@toModel.email
        details   = PersonDetails(
            street  = this@toModel.street,
            city    = this@toModel.city,
            zipCode = this@toModel.zipCode,
            country = this@toModel.country,
            phone   = this@toModel.phone
        )
        persist()
    }
    return CustomerContact().apply {
        this.customer      = customer
        this.contactPerson = person
        this.role          = this@toModel.role
        this.isPrimary     = this@toModel.isPrimary
    }
}

fun CustomerContactUpdateDTO.applyTo(entity: CustomerContact) {
    // update the contact’s own fields
    entity.role       = this.role
    entity.isPrimary  = this.isPrimary

    // *and* update the existing PersonModel
    with(entity.contactPerson) {
        firstName       = this@applyTo.firstName
        lastName        = this@applyTo.lastName
        email           = this@applyTo.email
        details.street  = this@applyTo.street
        details.city    = this@applyTo.city
        details.zipCode = this@applyTo.zipCode
        details.country = this@applyTo.country
        details.phone   = this@applyTo.phone
    }
}

fun CustomerContactForm.toUpdateDTOFromForm() = CustomerContactUpdateDTO(
    personId  = this.personId!!,
    firstName = this.firstName!!,
    lastName  = this.lastName!!,
    email     = this.email,
    street    = this.street,
    city      = this.city,
    zipCode   = this.zipCode,
    country   = this.country,
    phone     = this.phone,
    role      = this.role,
    isPrimary = this.isPrimary ?: false
)

