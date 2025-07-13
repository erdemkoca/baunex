package ch.baunex.timetracking.dto

import ch.baunex.user.dto.EmployeeDTO
import kotlinx.serialization.Serializable

@Serializable
data class EmployeeAbsenceStatsDTO(
    val employee: EmployeeDTO,
    val totalHolidays: Int,
    val approvedDays: Int,
    val pendingDays: Int,
    val remainingDays: Int
) 