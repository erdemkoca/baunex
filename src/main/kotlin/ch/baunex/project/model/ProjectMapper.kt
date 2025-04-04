package ch.baunex.project.model

import ch.baunex.project.dto.ProjectDTO

fun ProjectModel.toDTO(): ProjectDTO = ProjectDTO(
    id = this.id,
    name = this.name,
    budget = this.budget,
    client = this.client,
    contact = this.contact
)

fun ProjectDTO.toModel(): ProjectModel = ProjectModel().apply {
    name = this@toModel.name
    budget = this@toModel.budget
    client = this@toModel.client
    contact = this@toModel.contact
}
