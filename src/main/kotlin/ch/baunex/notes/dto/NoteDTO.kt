package ch.baunex.notes.dto

import ch.baunex.notes.model.NoteCategory
import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class NoteDto(
    val id: Long,
    val projectId: Long?,
    val timeEntryId: Long?,
    val documentId: Long?,
    val createdById: Long,
    val createdByName: String,
    @Serializable(with = LocalDateSerializer::class) val createdAt: LocalDate?,
    @Serializable(with = LocalDateSerializer::class) val updatedAt: LocalDate?,
    val title: String?,
    val content: String,
    val category: NoteCategory,
    val tags: List<String> = emptyList(),
    val attachments: List<MediaAttachmentDto> = emptyList()
)
