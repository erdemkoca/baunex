package ch.baunex.timetracking.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApprovalDTO(
    val approved: Boolean = false,
    val approverId: Long? = null,
    val approverName: String? = null
)
