package ch.baunex.catalog.model

import ch.baunex.catalog.dto.ProjectCatalogItemDTO
import ch.baunex.project.model.ProjectModel

fun ProjectCatalogItemModel.toDTO(): ProjectCatalogItemDTO = ProjectCatalogItemDTO(
    id = this.id,
    projectId = this.project.id!!,
    itemName = this.itemName,
    quantity = this.quantity,
    unitPrice = this.unitPrice,
    totalPrice = this.totalPrice
)

fun ProjectCatalogItemDTO.toModel(project: ProjectModel): ProjectCatalogItemModel = ProjectCatalogItemModel().apply {
    this.project = project
    this.itemName = this@toModel.itemName
    this.quantity = this@toModel.quantity
    this.unitPrice = this@toModel.unitPrice
}
