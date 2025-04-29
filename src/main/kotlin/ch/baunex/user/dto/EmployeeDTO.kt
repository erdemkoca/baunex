package ch.baunex.user.dto

import java.time.LocalDateTime

data class EmployeeDTO(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val ahvNumber: String,
    val bankIban: String?,
    val hourlyRate: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)