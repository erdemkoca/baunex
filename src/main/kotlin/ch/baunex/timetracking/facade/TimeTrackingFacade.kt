package ch.baunex.timetracking.facade

import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import ch.baunex.timetracking.dto.TimeEntryCatalogItemDTO
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.model.TimeEntryCatalogItemModel
import ch.baunex.timetracking.mapper.TimeEntryMapper
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.timetracking.service.TimeTrackingService
import ch.baunex.timetracking.service.TimeEntryCostService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate

@ApplicationScoped
class TimeTrackingFacade @Inject constructor(
    private val timeEntryRepository: TimeEntryRepository,
    private val timeTrackingService: TimeTrackingService,
    private val timeEntryCostService: TimeEntryCostService,
    private val timeEntryMapper: TimeEntryMapper
) {

    fun logTime(dto: TimeEntryDTO): TimeEntryResponseDTO {
        return timeEntryMapper.toTimeEntryResponseDTO(timeTrackingService.logTime(dto))
    }

    fun getAllTimeEntries(): List<TimeEntryResponseDTO> {
        return timeTrackingService.getAllTimeEntries().map { timeEntryMapper.toTimeEntryResponseDTO(it) }
    }

    fun getTimeEntryById(id: Long): TimeEntryResponseDTO? {
        return timeTrackingService.getTimeEntryById(id)?.let { timeEntryMapper.toTimeEntryResponseDTO(it) }
    }

    @Transactional
    fun updateTimeEntry(id: Long, dto: TimeEntryDTO): TimeEntryResponseDTO? {
        return timeTrackingService.updateTimeEntry(id, dto)?.let { timeEntryMapper.toTimeEntryResponseDTO(it) }
    }

    fun getTimeEntriesForEmployee(employeeId: Long): List<TimeEntryResponseDTO> {
        return timeEntryRepository.find("employee.id", employeeId).list<TimeEntryModel>().map {
            timeEntryMapper.toTimeEntryResponseDTO(it)
        }
    }

    fun getTimeEntriesForProject(projectId: Long): List<TimeEntryResponseDTO> {
        return timeEntryRepository.find("project.id", projectId).list<TimeEntryModel>().map {
            timeEntryMapper.toTimeEntryResponseDTO(it)
        }
    }

    fun getTimeEntriesByDateRange(start: LocalDate, end: LocalDate): List<TimeEntryResponseDTO> {
        return timeEntryRepository.find("date >= ?1 AND date <= ?2", start, end)
            .list<TimeEntryModel>().map {
                timeEntryMapper.toTimeEntryResponseDTO(it)
            }
    }

    @Transactional
    fun deleteTimeEntry(id: Long): Boolean {
        return timeEntryRepository.deleteById(id)
    }

    private fun mapToResponseDTO(entry: TimeEntryModel): TimeEntryResponseDTO {
        val dto = TimeEntryDTO(
            employeeId = entry.employee.id ?: 0L,
            projectId = entry.project.id ?: 0L,
            date = entry.date,
            hoursWorked = entry.hoursWorked,
            note = entry.note,
            hourlyRate = entry.hourlyRate,
            billable = entry.billable,
            invoiced = entry.invoiced,
            catalogItemDescription = entry.catalogItemDescription,
            catalogItemPrice = entry.catalogItemPrice,
            catalogItems = entry.usedCatalogItems.map { mapToCatalogItemDTO(it) },
            hasNightSurcharge = entry.hasNightSurcharge,
            hasWeekendSurcharge = entry.hasWeekendSurcharge,
            hasHolidaySurcharge = entry.hasHolidaySurcharge,
            travelTimeMinutes = entry.travelTimeMinutes,
            disposalCost = entry.disposalCost,
            hasWaitingTime = entry.hasWaitingTime,
            waitingTimeMinutes = entry.waitingTimeMinutes
        )

        return TimeEntryResponseDTO(
            id = entry.id,
            employeeId = entry.employee.id ?: 0L,
            employeeEmail = entry.employee.email,
            projectId = entry.project.id ?: 0L,
            projectName = entry.project.name,
            date = entry.date,
            hoursWorked = entry.hoursWorked,
            notes = entry.note,
            hourlyRate = entry.hourlyRate,
            cost = entry.hoursWorked * entry.hourlyRate,
            billable = entry.billable,
            invoiced = entry.invoiced,
            catalogItemDescription = entry.catalogItemDescription,
            catalogItemPrice = entry.catalogItemPrice,
            catalogItems = entry.usedCatalogItems.map { mapToCatalogItemDTO(it) },
            hasNightSurcharge = entry.hasNightSurcharge,
            hasWeekendSurcharge = entry.hasWeekendSurcharge,
            hasHolidaySurcharge = entry.hasHolidaySurcharge,
            travelTimeMinutes = entry.travelTimeMinutes,
            disposalCost = entry.disposalCost,
            hasWaitingTime = entry.hasWaitingTime,
            waitingTimeMinutes = entry.waitingTimeMinutes,
            costBreakdown = timeEntryCostService.calculateCostBreakdown(dto)
        )
    }

    private fun mapToCatalogItemDTO(item: TimeEntryCatalogItemModel): TimeEntryCatalogItemDTO {
        return TimeEntryCatalogItemDTO(
            catalogItemId = item.catalogItem.id ?: 0L,
            quantity = item.quantity,
            itemName = item.catalogItem.name,
            unitPrice = item.unitPrice,
            totalPrice = item.totalPrice
        )
    }
}
