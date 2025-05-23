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
    val catalogItemPrice: Double?,
    val catalogItems: List<TimeEntryCatalogItemDTO> = emptyList(),
    
    // Surcharges
    val hasNightSurcharge: Boolean = false,
    val hasWeekendSurcharge: Boolean = false,
    val hasHolidaySurcharge: Boolean = false,
    
    // Additional Costs
    val travelTimeMinutes: Int = 0,
    val disposalCost: Double = 0.0,
    val hasWaitingTime: Boolean = false,
    val waitingTimeMinutes: Int = 0,
    val costBreakdown: TimeEntryCostBreakdownDTO?
)