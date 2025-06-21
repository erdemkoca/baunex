package ch.baunex.timetracking.dto

import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class WeeklyWorkSummaryDTO(
    val employeeId: Long,
    val employeeName: String,
    @Serializable(with = LocalDateSerializer::class)
    val weekStart: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val weekEnd: LocalDate,
    val totalWorked: Double,
    val totalExpected: Double,
    val overtime: Double,
    val undertime: Double,
    val holidayDays: Int,
    val pendingHolidayRequests: Int,
    val dailySummaries: List<EmployeeDailyWorkDTO> = emptyList()
) 