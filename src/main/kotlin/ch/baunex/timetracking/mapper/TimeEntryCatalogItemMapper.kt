package ch.baunex.timetracking.mapper

import ch.baunex.catalog.model.CatalogItemModel
import ch.baunex.timetracking.dto.TimeEntryCatalogItemDTO
import ch.baunex.timetracking.model.TimeEntryCatalogItemModel
import ch.baunex.timetracking.model.TimeEntryModel

fun TimeEntryCatalogItemModel.toTimeEntryCatalogItemDTO(): TimeEntryCatalogItemDTO = TimeEntryCatalogItemDTO(
    id = this.id,
    timeEntryId = this.timeEntry.id!!,
    catalogItemId = this.catalogItem.id!!,
    quantity = this.quantity,
    itemName = this.catalogItem.name,
    unitPrice = this.unitPrice,
    totalPrice = this.totalPrice
)

fun TimeEntryCatalogItemDTO.toTimeEntryCatalogItemModel(
    timeEntry: TimeEntryModel,
    catalogItem: CatalogItemModel
): TimeEntryCatalogItemModel = TimeEntryCatalogItemModel().apply {
    this.timeEntry = timeEntry
    this.catalogItem = catalogItem
    this.quantity = this@toTimeEntryCatalogItemModel.quantity
    this.unitPrice = this@toTimeEntryCatalogItemModel.unitPrice
}