package ch.baunex.user.dto

import ch.baunex.serialization.LocalDateSerializer
import ch.baunex.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class EmployeeDTO(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val ahvNumber: String,
    val bankIban: String?,
    val hourlyRate: Double,
    val street: String?,
    val city: String?,
    val zipCode: String?,
    val country: String?,
    val phone: String?,
    @Serializable(with = LocalDateTimeSerializer::class) val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val updatedAt: LocalDateTime
)