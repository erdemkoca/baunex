package ch.baunex.timetracking.dto

import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class WeeklyApprovalDTO(
    val employeeId: Long,
    val employeeName: String,
    @Serializable(with = LocalDateSerializer::class)
    val weekStart: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val weekEnd: LocalDate,
    val totalEntries: Int,
    val pendingEntries: Int,
    val approvedEntries: Int,
    val totalHours: Double,
    val approvalStatus: String, // PENDING, APPROVED, REJECTED
    val approverId: Long?,
    val approverName: String?,
    @Serializable(with = LocalDateSerializer::class)
    val approvedAt: LocalDate?,
    val entries: List<TimeEntryDTO> = emptyList()
) 