package ch.baunex.documentGenerator.dto.invoice

import ch.baunex.documentGenerator.dto.DocumentSectionDTO
import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

data class InvoiceDocumentResponseDTO(
    val id: Long,
    val invoiceNumber: String?,
    @Serializable(with = LocalDateSerializer::class) val invoiceDate: LocalDate?,
    @Serializable(with = LocalDateSerializer::class) val dueDate: LocalDate?,
    val invoiceStatus: String?,
    val customerName: String,
    val customerAddress: String?,
    val projectName: String?,
    val totalNet: Double,
    val vatAmount: Double,
    val totalGross: Double,
    val sections: List<DocumentSectionDTO>,
    val metadata: Map<String, String> = emptyMap()
)