package ch.baunex.notes.model

import kotlinx.serialization.Serializable

@Serializable
enum class MediaType {
    IMAGE, PDF, VIDEO
}