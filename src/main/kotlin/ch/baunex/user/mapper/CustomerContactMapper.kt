package ch.baunex.user.mapper

import ch.baunex.user.dto.CustomerContactCreateDTO
import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.dto.CustomerContactUpdateDTO
import ch.baunex.user.model.CustomerContact
import ch.baunex.user.model.CustomerModel
import ch.baunex.user.model.PersonModel
import ch.baunex.web.forms.CustomerContactForm

fun CustomerContact.toDTO(): CustomerContactDTO = CustomerContactDTO(
    id = this.id!!,
    personId = this.contactPerson.id!!,
    personName = "${this.contactPerson.firstName} ${this.contactPerson.lastName}",
    role = this.role,
    isPrimary = this.isPrimary,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun CustomerContactCreateDTO.toModel(
    customer: CustomerModel,
    person: PersonModel
): CustomerContact = CustomerContact().apply {
    this.customer = customer
    this.contactPerson = person
    this.role = this@toModel.role
    this.isPrimary = this@toModel.isPrimary
}

fun CustomerContactUpdateDTO.applyTo(model: CustomerContact): CustomerContact = model.apply {
    this.role = this@applyTo.role
    this.isPrimary = this@applyTo.isPrimary
}

fun CustomerContactForm.toUpdateDTO() = CustomerContactUpdateDTO(
    role      = this.role,
    isPrimary = this.isPrimary ?: false
)
