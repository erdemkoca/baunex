package ch.baunex.invoice.dto

data class InvoiceItemDTO(
    val id: Long? = null,
    val type: String,
    val description: String? = null,
    val quantity: Double,
    val price: Double,
    val unitPrice: Double,
    val vatRate: Double,
    val totalAmount: Double,
    val vatAmount: Double,
    val grandTotal: Double,
    val order: Int = 0,
    val timeEntryId: Long? = null,
    val projectCatalogItemId: Long? = null
)