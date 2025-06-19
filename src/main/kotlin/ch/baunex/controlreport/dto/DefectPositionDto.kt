package ch.baunex.controlreport.dto

import ch.baunex.notes.dto.MediaAttachmentDto
import kotlinx.serialization.Serializable

@Serializable
data class DefectPositionDto(
    val id: Long? = null,
    val positionNumber: Int,
    val description: String,
    val buildingLocation: String?,
    val noteId: Long?,
    val noteContent: String?,
    val photoUrls: List<MediaAttachmentDto>,
    val normReferences: List<String> = emptyList()
)