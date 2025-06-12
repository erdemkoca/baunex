package ch.baunex.documentGenerator.dto

import ch.baunex.documentGenerator.model.DocumentType
import ch.baunex.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

data class DocumentResponseDTO(
    val id: Long,
    val type: DocumentType,
    @Serializable(with = LocalDateTimeSerializer::class) val createdAt: LocalDateTime,
    val headerHtml: String?,
    val footerHtml: String?,
    val sections: List<DocumentSectionDTO>,
    val metadata: Map<String, String> = emptyMap()
)