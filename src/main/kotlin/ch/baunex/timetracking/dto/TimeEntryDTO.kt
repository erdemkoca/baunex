package ch.baunex.timetracking.dto

import ch.baunex.notes.dto.NoteDto
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class TimeEntryDTO(
    val employeeId: Long,
    val projectId: Long,
    @Contextual val date: LocalDate,
    val hoursWorked: Double,
    val notes: List<NoteDto> = emptyList(),
    val hourlyRate: Double? = null,
    val billable: Boolean = false,
    val invoiced: Boolean = false,
    val catalogItemDescription: String? = null,
    val catalogItemPrice: Double? = null,
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
    val costBreakdown: TimeEntryCostBreakdownDTO? = null
)