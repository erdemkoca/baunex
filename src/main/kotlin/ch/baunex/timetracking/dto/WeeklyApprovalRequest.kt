package ch.baunex.timetracking.dto

data class WeeklyApprovalRequest(
    val employeeId: Long,
    val from: String, // YYYY-MM-DD format
    val to: String    // YYYY-MM-DD format
) 