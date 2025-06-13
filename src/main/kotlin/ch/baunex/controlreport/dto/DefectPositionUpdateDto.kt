package ch.baunex.controlreport.dto

import ch.baunex.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

data class DefectPositionUpdateDto(
    val photoUrl: String?,
    val description: String?,
    val normReferences: List<String>,
    @Serializable(with = LocalDateTimeSerializer::class)
    val resolvedAt: LocalDateTime?,
    val resolutionStamp: String?,
    val resolutionSignature: String?
)