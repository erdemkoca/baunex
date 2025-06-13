package ch.baunex.controlreport.dto

import ch.baunex.serialization.LocalDateTimeSerializer
import java.time.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CompletionConfirmationDto(
    @Serializable(with = LocalDateTimeSerializer::class)
    val resolvedAt: LocalDateTime,
    val companyStamp: String,
    val signature: String
)