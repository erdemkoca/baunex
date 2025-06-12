package ch.baunex.documentGenerator.dto.invoice

import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

data class InvoiceDocumentRequestDTO(
    val customerName: String,
    val customerAddress: String,
    val invoiceNumber: String,
    @Serializable(with = LocalDateSerializer::class) val invoiceDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class) val dueDate: LocalDate,
    val vatRate: Double,
    val items: List<InvoiceLineItemDTO>
)