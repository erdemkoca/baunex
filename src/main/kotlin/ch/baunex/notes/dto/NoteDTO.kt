package ch.baunex.notes.dto

import ch.baunex.notes.model.NoteCategory
import java.time.LocalDateTime

data class NoteDto(
    val id: Long,
    val projectId: Long?,
    val timeEntryId: Long?,
    val documentId: Long?,
    val createdById: Long,
    val createdByName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val title: String?,
    val content: String,
    val category: NoteCategory,
    val tags: List<String> = emptyList(),
    val attachments: List<MediaAttachmentDto> = emptyList()
)
