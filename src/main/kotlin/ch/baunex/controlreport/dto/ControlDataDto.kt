package ch.baunex.controlreport.dto

import ch.baunex.serialization.LocalDateSerializer
import ch.baunex.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ControlDataDto(
    @Serializable(with = LocalDateSerializer::class)
    val controlDate: LocalDate,
    val controllerId: Long?,
    val controllerFirstName: String?,
    val controllerLastName: String?,
    val phoneNumber: String?,
    val hasDefects: Boolean,
    val deadlineNote: String?
)