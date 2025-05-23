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
) {
    companion object {
        fun calculate(entry: TimeEntryDTO): TimeEntryCostBreakdownDTO {
            val hourlyRate = entry.hourlyRate ?: 0.0
            val timeCost = entry.hoursWorked * hourlyRate
            
            // Calculate surcharges (25% each)
            val surchargeRate = 0.25
            val nightSurcharge = if (entry.hasNightSurcharge) timeCost * surchargeRate else null
            val weekendSurcharge = if (entry.hasWeekendSurcharge) timeCost * surchargeRate else null
            val holidaySurcharge = if (entry.hasHolidaySurcharge) timeCost * surchargeRate else null
            
            // Calculate additional time costs
            val travelTimeCost = if (entry.travelTimeMinutes > 0) 
                (entry.travelTimeMinutes / 60.0) * hourlyRate else null
            val waitingTimeCost = if (entry.hasWaitingTime && entry.waitingTimeMinutes > 0) 
                (entry.waitingTimeMinutes / 60.0) * hourlyRate else null
            val disposalCost = if (entry.disposalCost > 0) entry.disposalCost else null
            
            // Calculate total service cost
            val totalServiceCost = timeCost +
                (nightSurcharge ?: 0.0) +
                (weekendSurcharge ?: 0.0) +
                (holidaySurcharge ?: 0.0) +
                (travelTimeCost ?: 0.0) +
                (waitingTimeCost ?: 0.0) +
                (disposalCost ?: 0.0)
            
            // Calculate catalog items cost
            val catalogItemsCost = entry.catalogItems.sumOf { it.totalPrice }
            
            // Calculate grand total
            val grandTotal = totalServiceCost + catalogItemsCost
            
            return TimeEntryCostBreakdownDTO(
                timeCost = timeCost,
                nightSurcharge = nightSurcharge,
                weekendSurcharge = weekendSurcharge,
                holidaySurcharge = holidaySurcharge,
                travelTimeCost = travelTimeCost,
                waitingTimeCost = waitingTimeCost,
                disposalCost = disposalCost,
                totalServiceCost = totalServiceCost,
                catalogItemsCost = catalogItemsCost,
                grandTotal = grandTotal
            )
        }
    }
} 