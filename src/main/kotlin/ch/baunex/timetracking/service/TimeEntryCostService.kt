package ch.baunex.timetracking.service

import ch.baunex.timetracking.dto.TimeEntryCostBreakdownDTO
import ch.baunex.timetracking.model.TimeEntryModel
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger

@ApplicationScoped
class TimeEntryCostService {
    private val log = Logger.getLogger(TimeEntryCostService::class.java)
    fun calculateCostBreakdown(entry: TimeEntryModel): TimeEntryCostBreakdownDTO {
        log.info("Calculating cost breakdown for time entry ${entry.id}")
        return try {
            val hourlyRate = entry.hourlyRate ?: 0.0
            val timeCost = entry.hoursWorked * hourlyRate
            val surchargeRate = 0.25
            val nightSurcharge   = if (entry.hasNightSurcharge) timeCost * surchargeRate else null
            val weekendSurcharge = if (entry.hasWeekendSurcharge) timeCost * surchargeRate else null
            val holidaySurcharge = if (entry.hasHolidaySurcharge) timeCost * surchargeRate else null
            val travelTimeCost  = if (entry.travelTimeMinutes > 0)
                (entry.travelTimeMinutes / 60.0) * hourlyRate else null
            val waitingTimeCost = if (entry.waitingTimeMinutes > 0)
                (entry.waitingTimeMinutes / 60.0) * hourlyRate else null
            val disposalCost    = if (entry.disposalCost > 0) entry.disposalCost else null
            val totalServiceCost = timeCost +
                    listOf(nightSurcharge, weekendSurcharge, holidaySurcharge,
                        travelTimeCost, waitingTimeCost, disposalCost)
                        .map { it ?: 0.0 }
                        .sum()
            val catalogItemsCost = entry.usedCatalogItems.sumOf { it.totalPrice }
            val grandTotal       = totalServiceCost + catalogItemsCost
            val result = TimeEntryCostBreakdownDTO(
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
            log.info("Calculated cost breakdown for time entry ${entry.id}")
            result
        } catch (e: Exception) {
            log.error("Failed to calculate cost breakdown for time entry ${entry.id}", e)
            throw e
        }
    }
}
