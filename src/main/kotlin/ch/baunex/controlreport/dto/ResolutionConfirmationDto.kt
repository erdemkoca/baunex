package ch.baunex.controlreport.dto

import java.time.LocalDateTime

data class ResolutionConfirmationDto(
    val resolvedAt: LocalDateTime,
    val stamp: String?,
    val signature: String?
)
