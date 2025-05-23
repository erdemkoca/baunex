package ch.baunex.timetracking.dto

import kotlinx.serialization.Serializable

@Serializable
data class TimeEntryCatalogItemDTO(
    val id: Long? = null,
    val timeEntryId: Long? = null,
    val catalogItemId: Long? = null,
    val quantity: Int,
    val itemName: String,
    val unitPrice: Double,
    val totalPrice: Double
)