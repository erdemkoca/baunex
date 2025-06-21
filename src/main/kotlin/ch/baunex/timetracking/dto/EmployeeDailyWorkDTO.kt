package ch.baunex.timetracking.dto

import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class EmployeeDailyWorkDTO(
    val employeeId: Long,
    val employeeName: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val workedHours: Double,
    val expectedHours: Double,
    val delta: Double,
    val holidayType: String? = null,
    val holidayApproved: Boolean? = null,
    val holidayReason: String? = null,
    val timeEntries: List<TimeEntryDTO> = emptyList(),
    val isWeekend: Boolean = false,
    val isPublicHoliday: Boolean = false
) 