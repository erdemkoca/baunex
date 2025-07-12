package ch.baunex.timetracking.dto

import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ApprovalDTO(
    val approved: Boolean = false,
    val approverId: Long? = null,
    val approverName: String = "",
    @Serializable(with = LocalDateSerializer::class)
    val approvedAt: LocalDate? = null,
    val status: String = "PENDING" // PENDING, APPROVED, REJECTED
)
