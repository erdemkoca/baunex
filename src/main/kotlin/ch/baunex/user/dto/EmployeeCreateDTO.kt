package ch.baunex.user.dto

import java.time.LocalDate

data class EmployeeCreateDTO(
    val firstName: String,
    val lastName: String,
    val street: String?,
    val city: String?,
    val zipCode: String?,
    val country: String?,
    val phone: String?,
    val email: String,
    val password: String,
    val role: String,
    val ahvNumber: String,
    val bankIban: String?,
    val hourlyRate: Double = 150.0,
    val vacationDays: Int = 25,
    val startDate: LocalDate = LocalDate.now()
)