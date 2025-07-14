package ch.baunex.timetracking.dto

import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class CumulativeHoursAccountDTO(
    val employeeId: Long,
    val employeeName: String,
    @Serializable(with = LocalDateSerializer::class)
    val startDate: LocalDate,
    
    // Current week balance
    val weeklyBalance: Double, // overtime - undertime for current week
    val weeklyWorkedHours: Double,
    val weeklyExpectedHours: Double,
    
    // Cumulative balance since start date
    val cumulativeBalance: Double, // total overtime - undertime since start date
    val cumulativeWorkedHours: Double,
    val cumulativeExpectedHours: Double,
    
    // Current week info
    @Serializable(with = LocalDateSerializer::class)
    val currentWeekStart: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val currentWeekEnd: LocalDate,
    val currentWeekNumber: Int,
    val currentYear: Int,
    
    // Vacation and absence info
    val holidayDays: Int,
    val pendingHolidayRequests: Int,
    val totalVacationDays: Int,
    val usedVacationDays: Int,
    val remainingVacationDays: Int
) 