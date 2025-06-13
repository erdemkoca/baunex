package ch.baunex.documentGenerator.dto

import java.time.LocalDateTime

data class DocumentResponseDTO(
    val id: Long,
    val type: String,
    val customerName: String,
    val markdownHeader: String?,
    val markdownFooter: String?,
    val createdAt: LocalDateTime,
    val entries: List<DocumentEntryResponseDTO>
)
