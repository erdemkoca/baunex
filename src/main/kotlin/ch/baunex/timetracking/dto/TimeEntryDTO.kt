package ch.baunex.timetracking.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class TimeEntryDTO(
    val employeeId: Long,
    val projectId: Long,
    @Contextual val date: LocalDate,
    val hoursWorked: Double,
    val note: String? = null,
    val hourlyRate: Double? = null,
    val billable: Boolean = false,
    val invoiced: Boolean = false,
    val catalogItemDescription: String? = null,
    val catalogItemPrice: Double? = null
)

