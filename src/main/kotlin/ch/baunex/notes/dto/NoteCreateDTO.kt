package ch.baunex.notes.dto

import ch.baunex.notes.model.NoteCategory

data class NoteCreateDto(
    val projectId: Long? = null,
    val timeEntryId: Long? = null,
    val documentId: Long? = null,
    val title: String? = null,
    val content: String,
    val category: NoteCategory,
    val tags: List<String> = emptyList()
)
