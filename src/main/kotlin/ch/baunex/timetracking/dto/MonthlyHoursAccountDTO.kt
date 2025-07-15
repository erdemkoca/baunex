package ch.baunex.timetracking.dto

import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class MonthlyHoursAccountDTO(
    val employeeId: Long,
    val employeeName: String,
    @Serializable(with = LocalDateSerializer::class) val startDate: LocalDate,
    
    // Current week balance for summary
    val currentWeekBalance: Double,
    val currentWeekBalanceFormatted: String,
    val currentWeekWorkedHours: Double,
    val currentWeekWorkedHoursFormatted: String,
    val currentWeekExpectedHours: Double,
    val currentWeekExpectedHoursFormatted: String,
    // Cumulative balance since start date
    val cumulativeBalance: Double,
    val cumulativeBalanceFormatted: String,
    val cumulativeWorkedHours: Double,
    val cumulativeExpectedHours: Double,
    // Monthly data
    val monthlyData: List<MonthDataDTO>
)

@Serializable
data class MonthDataDTO(
    val year: Int,
    val month: Int,
    val monthName: String,
    val weeks: List<WeekDataDTO>,
    val monthTotal: Double,
    val monthTotalFormatted: String,
    val monthWorkedHours: Double,
    val monthWorkedHoursFormatted: String,
    val monthExpectedHours: Double,
    val monthExpectedHoursFormatted: String
)

@Serializable
data class WeekDataDTO(
    val weekNumber: Int,
    @Serializable(with = LocalDateSerializer::class) val weekStart: LocalDate,
    @Serializable(with = LocalDateSerializer::class) val weekEnd: LocalDate,
    val days: List<DayDataDTO>,
    val weekTotal: Double,
    val weekTotalFormatted: String,
    val weekWorkedHours: Double,
    val weekExpectedHours: Double
)

@Serializable
data class DayDataDTO(
    @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
    val dateDay: String,
    val dayOfWeek: Int, // 1=Monday, 7=Sunday
    val dayName: String,
    val workedHours: Double,
    val expectedHours: Double,
    val balance: Double, // workedHours - expectedHours
    val saldo: Double, // workedHours - expectedHours (for template compatibility)
    val saldoFormatted: String,
    val isWeekend: Boolean,
    val isHoliday: Boolean,
    val holidayType: String?,
    val holidayApproved: Boolean?,
    val isEmpty: Boolean = false, // Marks days outside the month
    val isFuture: Boolean = false // Marks future dates
) 