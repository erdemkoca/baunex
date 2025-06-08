package ch.baunex.documentGenerator.dto

import ch.baunex.documentGenerator.model.DocumentType
import ch.baunex.invoice.model.InvoiceStatus
import java.time.LocalDate

data class DocumentDTO(
    val type: DocumentType,
    val customerName: String,
    val markdownHeader: String?,
    val markdownFooter: String?,
    val invoiceNumber: String?,
    val invoiceDate: LocalDate?,
    val dueDate: LocalDate?,
    val invoiceStatus: InvoiceStatus?,
    val notes: String?,
    val vatRate: Double?,
    val projectId: Long?,
    val projectName: String?,
    val customerAddress: String?,
    val entries: List<DocumentEntryDTO>
)