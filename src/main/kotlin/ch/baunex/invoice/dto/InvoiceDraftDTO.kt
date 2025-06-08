package ch.baunex.invoice.dto

import ch.baunex.notes.dto.NoteDto
import kotlinx.serialization.Serializable

@Serializable
data class InvoiceDraftDTO(
    val invoiceDate: String, // "2025-05-28"
    val dueDate: String,
    val invoiceNumber: String?,
    val notes: List<NoteDto> = emptyList(),
    val customerId: Long,
    val projectId: Long,
    val vatRate: Double,
    val items: List<InvoiceItemDTO>
)