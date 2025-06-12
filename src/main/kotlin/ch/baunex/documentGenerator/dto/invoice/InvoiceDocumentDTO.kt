package ch.baunex.documentGenerator.dto.invoice

import ch.baunex.documentGenerator.dto.DocumentSectionDTO
import ch.baunex.documentGenerator.dto.GenericDocumentDTO
import ch.baunex.documentGenerator.dto.TableDTO
import ch.baunex.documentGenerator.model.DocumentType

data class InvoiceDocumentDTO(
    val request: InvoiceDocumentRequestDTO,
    val companyName: String,
    val companyAddress: String,
    val projectName: String?,
    val projectPeriod: String?,
    val markdownHeader: String?,
    val markdownFooter: String?
    // …
)