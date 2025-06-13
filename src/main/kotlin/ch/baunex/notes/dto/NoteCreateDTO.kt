package ch.baunex.notes.dto

import ch.baunex.notes.model.NoteCategory
import kotlinx.serialization.Serializable

@Serializable
data class NoteCreateDto(
    val id: Long? = null,
    val projectId: Long? = null,
    val timeEntryId: Long? = null,
    val documentId: Long? = null,
    val title: String? = null,
    val content: String,
    val category: NoteCategory,
    val tags: List<String> = emptyList(),
    val attachments: List<Long> = emptyList(),
    val createdById: Long? = null
)
