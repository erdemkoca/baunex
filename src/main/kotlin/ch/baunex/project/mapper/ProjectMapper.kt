package ch.baunex.project.mapper

import ch.baunex.catalog.mapper.toProjectCatalogItemDTO
import ch.baunex.project.dto.*
import ch.baunex.project.model.ProjectModel
import ch.baunex.timetracking.mapper.TimeEntryMapper
import ch.baunex.user.mapper.toContactDTO
import ch.baunex.user.mapper.toCustomerDTO
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class ProjectMapper @Inject constructor(
    private val timeEntryMapper: TimeEntryMapper
) {
    fun toListDTO(model: ProjectModel) = ProjectListDTO(
        id           = model.id!!,
        name         = model.name,
        customerName = model.customer.companyName ?: "",
        budget       = model.budget,
        status       = model.status
    )

    fun toDetailDTO(model: ProjectModel) = ProjectDetailDTO(
        id           = model.id!!,
        name         = model.name,
        customerId   = model.customer.id!!,
        customerName = model.customer.companyName ?: "",
        budget       = model.budget,
        customer      = model.customer.toCustomerDTO(),
        startDate    = model.startDate,
        endDate      = model.endDate,
        description  = model.description,
        status       = model.status,
        street       = model.street,
        city         = model.city,
        timeEntries  = model.timeEntries.sortedBy { it.date }.map { timeEntryMapper.toTimeEntryResponseDTO(it) },
        catalogItems = model.usedItems.map { it.toProjectCatalogItemDTO() },
        contacts     = model.customer.contacts.map { it.toContactDTO() }
    )
}

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
