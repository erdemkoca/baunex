package ch.baunex.documentGenerator.dto

import ch.baunex.documentGenerator.model.DocumentType

data class DocumentDTO(
    val type: DocumentType,
    val customerName: String,
    val markdownHeader: String?,
    val markdownFooter: String?,
    val entries: List<DocumentEntryDTO>
)