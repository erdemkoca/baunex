package ch.baunex.timetracking.dto

import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class HolidayDTO(
    val id: Long? = null,
    val employeeId: Long,
    val employeeName: String = "",
    @Serializable(with = LocalDateSerializer::class)
    val startDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val endDate: LocalDate,
    val type: String, // "PAID_VACATION", "SICK_LEAVE", etc.
    val reason: String? = null,
    val status: String = "PENDING"  // optional default for response
)
