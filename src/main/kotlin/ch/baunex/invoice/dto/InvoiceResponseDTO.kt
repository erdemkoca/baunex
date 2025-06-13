package ch.baunex.invoice.dto

import ch.baunex.invoice.model.InvoiceStatus

data class InvoiceResponseDTO(
    val id: Long,
    val invoiceNumber: String,
    val invoiceDate: String,
    val dueDate: String,
    val status: InvoiceStatus,
    val totalNetto: Double,
    val vatAmount: Double,
    val totalBrutto: Double,
    val items: List<InvoiceItemDTO>
)
