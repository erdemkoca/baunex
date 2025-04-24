package ch.baunex.project.mapper

import ch.baunex.catalog.mapper.toProjectCatalogItemDTO
import ch.baunex.project.dto.ProjectDTO
import ch.baunex.project.model.ProjectModel
import ch.baunex.timetracking.mapper.toTimeEntryResponseDTO

fun ProjectModel.toDTO(): ProjectDTO = ProjectDTO(
    id = this.id,
    name = this.name,
    client = this.client,
    budget = this.budget,
    contact = this.contact,
    startDate = this.startDate,
    endDate = this.endDate,
    description = this.description,
    status = this.status,
    street = this.street,
    city = this.city,
    timeEntries = this.timeEntries.sortedBy { it.date }.map { it.toTimeEntryResponseDTO() },
    catalogItems = this.usedItems.map { it.toProjectCatalogItemDTO() }
)

fun ProjectDTO.toProjectModel(): ProjectModel = ProjectModel().apply {
    name = this@toProjectModel.name
    client = this@toProjectModel.client
    budget = this@toProjectModel.budget
    contact = this@toProjectModel.contact
    startDate = this@toProjectModel.startDate
    endDate = this@toProjectModel.endDate
    description = this@toProjectModel.description
    status = this@toProjectModel.status
    street = this@toProjectModel.street
    city = this@toProjectModel.city
}
