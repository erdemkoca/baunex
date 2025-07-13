package ch.baunex.timetracking.dto

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class PublicHolidayDTO(
    val id: Long,
    val year: Int,
    val holidayDate: String, // LocalDate as string
    val name: String,
    val canton: String?,
    val isFixed: Boolean,
    val isEditable: Boolean,
    val active: Boolean,
    val isWorkFree: Boolean,
    val holidayType: String,
    val description: String?
) 