package ch.baunex.controlreport.dto

import java.time.LocalDateTime

data class CompletionConfirmationDto(
    val resolvedAt: LocalDateTime,
    val companyStamp: String,
    val signature: String
)