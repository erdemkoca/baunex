package ch.baunex.notes.dto

import kotlinx.serialization.Serializable

@Serializable
data class AttachmentForUI(
    val id: Long,
    val url: String,
    val caption: String?,
    val filename: String,
    val contentType: String
)