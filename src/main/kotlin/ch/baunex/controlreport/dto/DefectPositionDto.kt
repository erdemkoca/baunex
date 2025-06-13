package ch.baunex.controlreport.dto

import kotlinx.serialization.Serializable

@Serializable
data class DefectPositionDto(
    val positionNumber: Int,
    val photoUrl: String,
    val description: String,
    val normReferences: List<String>,
    val resolutionConfirmation: ResolutionConfirmationDto?
)