package ch.baunex.timetracking.dto

import kotlinx.serialization.Serializable

@Serializable
data class TimeEntryCostBreakdownDTO(
    val timeCost: Double,
    val nightSurcharge: Double?,
    val weekendSurcharge: Double?,
    val holidaySurcharge: Double?,
    val travelTimeCost: Double?,
    val waitingTimeCost: Double?,
    val disposalCost: Double?,
    val totalServiceCost: Double,
    val catalogItemsCost: Double,
    val grandTotal: Double,
    val totalSurcharges: Double = (nightSurcharge ?: 0.0) + (weekendSurcharge ?: 0.0) + (holidaySurcharge ?: 0.0),
    val totalAdditionalCosts: Double = (travelTimeCost ?: 0.0) + (waitingTimeCost ?: 0.0) + (disposalCost ?: 0.0)
)