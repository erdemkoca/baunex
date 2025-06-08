package ch.baunex.invoice.dto

import ch.baunex.notes.dto.NoteDto
import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class InvoiceNewDraftDTO(
    @Serializable(with = LocalDateSerializer::class) val invoiceDate: LocalDate,
    val dueDate: String,
    val invoiceNumber: String?,
    val notes: List<NoteDto> = emptyList(),
    val customerId: Long,
    val projectId: Long,
    val vatRate: Double,
    val items: List<InvoiceItemDTO>
)