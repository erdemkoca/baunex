package ch.baunex.catalog.mapper

import ch.baunex.catalog.dto.CatalogItemDTO
import ch.baunex.catalog.model.CatalogItemModel

fun CatalogItemModel.toCatalogItemDTO(): CatalogItemDTO = CatalogItemDTO(
    id = this.id,
    name = this.name,
    unitPrice = this.unitPrice,
    description = this.description
)

fun CatalogItemDTO.toCatalogItemModel(): CatalogItemModel = CatalogItemModel().apply {
    name = this@toCatalogItemModel.name
    unitPrice = this@toCatalogItemModel.unitPrice
    description = this@toCatalogItemModel.description
}
