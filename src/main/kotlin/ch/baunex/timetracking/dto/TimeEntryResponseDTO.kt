package ch.baunex.timetracking.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class TimeEntryResponseDTO(
    val id: Long?,
    val employeeId: Long,
    val employeeEmail: String,
    val projectId: Long,
    val projectName: String,
    @Contextual val date: LocalDate,
    val hoursWorked: Double,
    val notes: String?,
    val hourlyRate: Double?,
    val cost: Double?,
    val billable: Boolean,
    val invoiced: Boolean,
    val catalogItemDescription: String?,
    val catalogItemPrice: Double?
)