package ch.baunex.controlreport.dto

import ch.baunex.serialization.LocalDateSerializer
import ch.baunex.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ControlDataDto(
    @Serializable(with = LocalDateSerializer::class)
    val controlDate: LocalDate? = null,
    val controllerId: Long? = null,
    val controllerFirstName: String? = null,
    val controllerLastName: String? = null,
    val phoneNumber: String? = null,
    val hasDefects: Boolean = false,
    val deadlineNote: String? = null
)