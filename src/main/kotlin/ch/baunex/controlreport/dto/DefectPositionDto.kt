package ch.baunex.controlreport.dto

import ch.baunex.notes.dto.MediaAttachmentDto
import kotlinx.serialization.Serializable

@Serializable
data class DefectPositionDto(
    val positionNumber: Int,
    val photoUrl: MediaAttachmentDto?,
    val description: String,
    val normReferences: List<String>
)