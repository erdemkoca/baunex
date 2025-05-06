package ch.baunex.project.mapper

import ch.baunex.catalog.mapper.toProjectCatalogItemDTO
import ch.baunex.project.dto.*
import ch.baunex.project.model.ProjectModel
import ch.baunex.timetracking.mapper.toTimeEntryResponseDTO
import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.mapper.toContactDTO
import ch.baunex.user.mapper.toCustomerDTO
import ch.baunex.user.model.CustomerContact

fun ProjectModel.toListDTO() = ProjectListDTO(
    id           = this.id!!,
    name         = this.name,
    customerName = this.customer.companyName ?: "",
    budget       = this.budget,
    status       = this.status
)

fun ProjectModel.toDetailDTO() = ProjectDetailDTO(
    id           = this.id!!,
    name         = this.name,
    customerId   = this.customer.id!!,
    customerName = this.customer.companyName ?: "",
    budget       = this.budget,
    customer      = this.customer.toCustomerDTO(),
    startDate    = this.startDate,
    endDate      = this.endDate,
    description  = this.description,
    status       = this.status,
    street       = this.street,
    city         = this.city,
    timeEntries  = this.timeEntries.sortedBy { it.date }.map { it.toTimeEntryResponseDTO() },
    catalogItems = this.usedItems.map { it.toProjectCatalogItemDTO() },
    contacts     = this.customer.contacts.map { it.toContactDTO() }
)

fun ProjectCreateDTO.toModel(customer: ch.baunex.user.model.CustomerModel) = ProjectModel().apply {
    name        = this@toModel.name
    this.customer = customer
    budget      = this@toModel.budget
    startDate   = this@toModel.startDate
    endDate     = this@toModel.endDate
    description = this@toModel.description
    status      = this@toModel.status
    street      = this@toModel.street
    city        = this@toModel.city
}

fun ProjectUpdateDTO.applyTo(model: ProjectModel) {
    name        ?.let { model.name        = it }
    budget      ?.let { model.budget      = it }
    startDate   ?.let { model.startDate   = it }
    endDate     ?.let { model.endDate     = it }
    description ?.let { model.description = it }
    status      ?.let { model.status      = it }
    street      ?.let { model.street      = it }
    city        ?.let { model.city        = it }
}
