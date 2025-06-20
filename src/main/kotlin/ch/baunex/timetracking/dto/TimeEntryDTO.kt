package ch.baunex.timetracking.dto

import ch.baunex.notes.dto.NoteDto
import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class TimeEntryDTO(
    val id: Long? = null,
    val employeeId: Long,
    val projectId: Long,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val hoursWorked: Double,
    val title: String,
    val notes: List<NoteDto> = emptyList(),

    // billing flags
    val hourlyRate: Double? = null,
    val billable: Boolean = false,
    val invoiced: Boolean = false,

    // catalog‐line items
    val catalogItems: List<TimeEntryCatalogItemDTO> = emptyList(),

    // input surcharges
    val hasNightSurcharge: Boolean = false,
    val hasWeekendSurcharge: Boolean = false,
    val hasHolidaySurcharge: Boolean = false,

    // input additional costs
    val travelTimeMinutes: Int = 0,
    val waitingTimeMinutes: Int = 0,
    val disposalCost: Double = 0.0,

    // flattened cost‐breakdown fields
    val costBreakdown: TimeEntryCostBreakdownDTO? = null,

    val employeeEmail:     String = "",
    val employeeFirstName: String = "",
    val employeeLastName:  String = "",
    val projectName:       String = "",
    val cost:              Double? = null,
    val approval:          ApprovalDTO = ApprovalDTO()
)
