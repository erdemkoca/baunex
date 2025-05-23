package ch.baunex.invoice.dto

data class InvoiceDraftItemDTO(
    val id: Long? = null,
    val type: String = "",
    val description: String = "",
    val quantity: Double = 0.0,
    val unitPrice: Double = 0.0,
    val vatRate: Double = 0.0,
    val totalAmount: Double = 0.0,
    val vatAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val order: Int = 0,
    val timeEntryId: Long? = null,
    val catalogItemId: Long? = null
) 