package ch.baunex.notes.dto

import ch.baunex.notes.model.MediaType
import kotlinx.serialization.Serializable

@Serializable
data class MediaAttachmentDto(
    val id: Long,
    val url: String,
    val type: MediaType,
    val caption: String?
)
