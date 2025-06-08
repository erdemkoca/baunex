package ch.baunex.invoice.dto

import ch.baunex.invoice.model.InvoiceStatus
import ch.baunex.notes.dto.NoteDto
import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class InvoiceDTO(
    val id: Long? = null,
    val invoiceNumber: String,
    @Serializable(with = LocalDateSerializer::class) val invoiceDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class) val dueDate: LocalDate,
    val customerId: Long,
    val customerName: String = "",
    val customerAddress: String = "",
    val projectId: Long,
    val projectName: String = "",
    val projectDescription: String? = null,
    val invoiceStatus: InvoiceStatus,
    val items: List<InvoiceItemDTO>,
    val totalAmount: Double = 0.0,
    val vatAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val vatRate: Double,
    val notes: List<NoteDto> = emptyList(),
) {
    val status: String
        get() = invoiceStatus.name

    val formattedGrandTotal: String
        get() = "%.2f".format(grandTotal)
}
