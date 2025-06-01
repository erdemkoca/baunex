package ch.baunex.invoice.dto

data class InvoiceDraftDTO(
    val invoiceDate: String, // "2025-05-28"
    val dueDate: String,
    val invoiceNumber: String?,
    val notes: String?,
    val customerId: Long,
    val projectId: Long,
    val vatRate: Double,
    val items: List<InvoiceItemDTO>
)