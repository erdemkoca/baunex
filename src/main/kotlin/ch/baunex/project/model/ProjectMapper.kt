package ch.baunex.project.model

import ch.baunex.project.dto.ProjectDTO

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
    city = this.city
)

fun ProjectDTO.toModel(): ProjectModel = ProjectModel().apply {
    name = this@toModel.name
    client = this@toModel.client
    budget = this@toModel.budget
    contact = this@toModel.contact
    startDate = this@toModel.startDate
    endDate = this@toModel.endDate
    description = this@toModel.description
    status = this@toModel.status
    street = this@toModel.street
    city = this@toModel.city
}
