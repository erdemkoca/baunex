package ch.baunex.catalog.model

import ch.baunex.catalog.dto.CatalogItemDTO

fun CatalogItemModel.toDTO(): CatalogItemDTO = CatalogItemDTO(
    id = this.id,
    name = this.name,
    unitPrice = this.unitPrice,
    description = this.description
)

fun CatalogItemDTO.toModel(): CatalogItemModel = CatalogItemModel().apply {
    name = this@toModel.name
    unitPrice = this@toModel.unitPrice
    description = this@toModel.description
}
