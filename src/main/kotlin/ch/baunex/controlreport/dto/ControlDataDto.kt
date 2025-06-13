package ch.baunex.controlreport.dto

import ch.baunex.serialization.LocalDateTimeSerializer
import java.time.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ControlDataDto(
    @Serializable(with = LocalDateTimeSerializer::class)
    val controlDate: LocalDateTime,
    val controllerName: String,
    val phoneNumber: String,
    val hasDefects: Boolean,
    val deadlineNote: String?
)