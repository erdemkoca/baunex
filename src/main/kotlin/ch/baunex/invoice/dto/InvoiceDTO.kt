package ch.baunex.invoice.dto

import ch.baunex.invoice.model.InvoiceStatus
import ch.baunex.notes.dto.NoteDto
import java.time.LocalDate

data class InvoiceDTO(
    val id: Long?,
    val invoiceNumber: String,
    val invoiceDate: LocalDate,
    val dueDate: LocalDate,
    val customerId: Long,
    val customerName: String,
    val customerAddress: String,
    val projectId: Long,
    val projectName: String,
    val projectDescription: String?,
    val invoiceStatus: InvoiceStatus,
    val items: List<InvoiceItemDTO>,
    val totalAmount: Double,
    val vatAmount: Double,
    val grandTotal: Double,
    val vatRate: Double,
    val notes: List<NoteDto> = emptyList(),
) {
    val status: String
        get() = invoiceStatus.name

    val formattedGrandTotal: String
        get() = "%.2f".format(grandTotal)
}
