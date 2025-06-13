package ch.baunex.controlreport.dto

data class DefectPositionCreateDto(
    val positionNumber: Int,
    val photoUrl: String?,
    val description: String,
    val normReferences: List<String>
)