package ch.baunex.timetracking.dto

import kotlinx.serialization.Serializable

/**
 * DTO for expected hours response
 */
@Serializable
data class ExpectedHoursDTO(
    val expectedHours: Double
) 