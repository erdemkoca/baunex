package ch.baunex.controlreport.dto

import ch.baunex.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

data class CompletionConfirmationCreateDto(
    @Serializable(with = LocalDateTimeSerializer::class)
    val resolvedAt: LocalDateTime,
    val companyStamp: String,
    val signature: String
)