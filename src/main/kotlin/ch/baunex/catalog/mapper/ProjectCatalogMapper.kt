package ch.baunex.catalog.mapper

import ch.baunex.catalog.dto.ProjectCatalogItemDTO
import ch.baunex.catalog.model.ProjectCatalogItemModel
import ch.baunex.project.model.ProjectModel

fun ProjectCatalogItemModel.toProjectCatalogItemDTO(): ProjectCatalogItemDTO = ProjectCatalogItemDTO(
    id = this.id,
    projectId = this.project.id!!,
    itemName = this.itemName,
    quantity = this.quantity,
    unitPrice = this.unitPrice,
    totalPrice = this.totalPrice
)

fun ProjectCatalogItemDTO.toProjectCatalogItemModel(project: ProjectModel): ProjectCatalogItemModel = ProjectCatalogItemModel().apply {
    this.project = project
    this.itemName = this@toProjectCatalogItemModel.itemName
    this.quantity = this@toProjectCatalogItemModel.quantity
    this.unitPrice = this@toProjectCatalogItemModel.unitPrice
}
