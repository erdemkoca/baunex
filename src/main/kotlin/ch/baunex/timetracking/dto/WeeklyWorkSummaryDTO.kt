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
    val totalVacationDays: Int,  // Total vacation days allocated per year
    val usedVacationDays: Int,   // Vacation days used this year
    val remainingVacationDays: Int, // Remaining vacation days
    val dailySummaries: List<EmployeeDailyWorkDTO> = emptyList()
) 