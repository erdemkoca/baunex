package ch.baunex.timetracking.dto

import kotlinx.serialization.Serializable

@Serializable
data class HolidayApprovalDTO(
    val approval: ApprovalDTO
)