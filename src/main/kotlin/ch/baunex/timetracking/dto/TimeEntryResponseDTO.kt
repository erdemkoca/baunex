package ch.baunex.timetracking.dto

import ch.baunex.timetracking.model.TimeEntryModel
import kotlinx.serialization.Serializable

@Serializable
data class TimeEntryResponseDTO(
    val id: Long?,
    val userId: Long,
    val userEmail: String,
    val projectId: Long,
    val projectName: String,
    val date: String,
    val hoursWorked: Double,
    val notes: String?,
    val hourlyRate: Double?,
    val billable: Boolean,
    val invoiced: Boolean,
    val catalogItemDescription: String?,
    val catalogItemPrice: Double?
)