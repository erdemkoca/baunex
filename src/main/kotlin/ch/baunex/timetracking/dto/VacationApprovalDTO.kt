package ch.baunex.timetracking.dto

import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class VacationApprovalDTO(
    val id: Long,
    val employeeId: Long,
    val employeeName: String,
    @Serializable(with = LocalDateSerializer::class)
    val startDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val endDate: LocalDate,
    val type: String,
    val reason: String?,
    val approvalStatus: String, // PENDING, APPROVED, REJECTED
    val approverId: Long?,
    val approverName: String?,
    @Serializable(with = LocalDateSerializer::class)
    val approvedAt: LocalDate?,
    val workingDays: Int // Number of working days in the vacation period
) 