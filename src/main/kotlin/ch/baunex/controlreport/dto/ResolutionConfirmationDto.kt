package ch.baunex.controlreport.dto

import ch.baunex.serialization.LocalDateTimeSerializer
import java.time.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ResolutionConfirmationDto(
    @Serializable(with = LocalDateTimeSerializer::class)
    val resolvedAt: LocalDateTime,
    val stamp: String?,
    val signature: String?
)
