package ch.baunex.controlreport.dto

data class DefectPositionDto(
    val positionNumber: Int,
    val photoUrl: String,
    val description: String,
    val normReferences: List<String>,
    val resolutionConfirmation: ResolutionConfirmationDto?
)