package ch.baunex.timetracking.dto

import java.time.LocalDate

/**
 * DTO for representing holiday conflicts
 * Used when a new holiday request conflicts with existing holidays
 */
data class HolidayConflictDTO(
    val employeeId: Long,
    val requestedStartDate: LocalDate,
    val requestedEndDate: LocalDate,
    val conflictingHolidays: List<ConflictingHolidayDTO>
)

/**
 * DTO for representing a single conflicting holiday
 */
data class ConflictingHolidayDTO(
    val id: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val type: String,
    val status: String,
    val reason: String?
) 