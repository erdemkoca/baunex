package ch.baunex.notes.dto

import ch.baunex.notes.model.NoteCategory
import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class NoteDto(
    val id: Long? = null,
    val projectId: Long? = null,
    val timeEntryId: Long? = null,
    val documentId: Long? = null,
    val title: String? = null,
    val content: String,
    val category: NoteCategory,
    val tags: List<String> = emptyList(),
    val attachments: List<MediaAttachmentDto> = emptyList(),
    val createdById: Long,
    @Serializable(with = LocalDateSerializer::class)
    val createdAt: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class)
    val updatedAt: LocalDate? = null
)
