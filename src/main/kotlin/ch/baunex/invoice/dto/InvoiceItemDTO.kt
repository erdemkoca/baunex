package ch.baunex.invoice.dto

import kotlinx.serialization.Serializable

@Serializable
data class InvoiceItemDTO(
    val id: Long?,
    val type: String,
    val description: String?,
    val quantity: Double,
    val unitPrice: Double,
    val vatRate: Double,
    val totalAmount: Double,
    val vatAmount: Double,
    val grandTotal: Double,
    val order: Int,
    val timeEntryId: Long?,
    val projectCatalogItemId: Long?,
    val price: Double
)