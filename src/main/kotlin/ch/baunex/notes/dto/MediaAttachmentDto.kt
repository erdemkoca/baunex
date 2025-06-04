package ch.baunex.notes.dto

import ch.baunex.notes.model.MediaType

data class MediaAttachmentDto(
    val id: Long,
    val url: String,
    val type: MediaType,
    val caption: String?
)
