package ch.baunex.timetracking.mapper

import ch.baunex.project.model.ProjectModel
import ch.baunex.timetracking.dto.ApprovalDTO
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import ch.baunex.timetracking.dto.TimeEntryCatalogItemDTO
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.user.model.EmployeeModel
import ch.baunex.timetracking.service.TimeEntryCostService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

// Extension functions at package level
fun TimeEntryModel.toTimeEntryResponseDTO(): TimeEntryResponseDTO {
    return TimeEntryResponseDTO(
        id = this.id,
        employeeId = this.employee.id!!,
        employeeEmail = this.employee.email,
        employeeFirstName = this.employee.person.firstName,
        employeeLastName = this.employee.person.lastName,
        projectId = this.project.id!!,
        projectName = this.project.name,
        date = this.date,
        hoursWorked = this.hoursWorked,
        notes = this.note,
        hourlyRate = this.hourlyRate,
        cost = this.hoursWorked * this.hourlyRate,
        billable = this.billable,
        invoiced = this.invoiced,
        catalogItemDescription = this.catalogItemDescription,
        catalogItemPrice = this.catalogItemPrice,
        catalogItems = this.usedCatalogItems.map { 
            TimeEntryCatalogItemDTO(
                id = it.id,
                timeEntryId = it.timeEntry.id,
                catalogItemId = it.catalogItem.id,
                quantity = it.quantity,
                itemName = it.catalogItem.name,
                unitPrice = it.unitPrice,
                totalPrice = it.totalPrice
            )
        },
        // Surcharges
        hasNightSurcharge = this.hasNightSurcharge,
        hasWeekendSurcharge = this.hasWeekendSurcharge,
        hasHolidaySurcharge = this.hasHolidaySurcharge,
        // Additional Costs
        travelTimeMinutes = this.travelTimeMinutes,
        disposalCost = this.disposalCost,
        hasWaitingTime = this.hasWaitingTime,
        waitingTimeMinutes = this.waitingTimeMinutes,
        costBreakdown = null, // We'll calculate this in the service layer

        approval = ApprovalDTO(
            approved = this.approvedBy != null,
            approverId = this.approvedBy?.id,
            approverName = this.approvedBy?.person?.firstName + " " + this.approvedBy?.person?.lastName
        )

    )
}

fun TimeEntryDTO.toTimeEntryModel(employee: EmployeeModel, project: ProjectModel): TimeEntryModel {
    return TimeEntryModel().apply {
        this.employee = employee
        this.project = project
        this.date = this@toTimeEntryModel.date
        this.hoursWorked = this@toTimeEntryModel.hoursWorked
        this.note = this@toTimeEntryModel.note
        this.hourlyRate = employee.hourlyRate
        this.billable = this@toTimeEntryModel.billable
        this.invoiced = this@toTimeEntryModel.invoiced
        this.catalogItemDescription = this@toTimeEntryModel.catalogItemDescription
        this.catalogItemPrice = this@toTimeEntryModel.catalogItemPrice
        
        // Surcharges
        this.hasNightSurcharge = this@toTimeEntryModel.hasNightSurcharge
        this.hasWeekendSurcharge = this@toTimeEntryModel.hasWeekendSurcharge
        this.hasHolidaySurcharge = this@toTimeEntryModel.hasHolidaySurcharge
        
        // Additional Costs
        this.travelTimeMinutes = this@toTimeEntryModel.travelTimeMinutes
        this.disposalCost = this@toTimeEntryModel.disposalCost
        this.hasWaitingTime = this@toTimeEntryModel.hasWaitingTime
        this.waitingTimeMinutes = this@toTimeEntryModel.waitingTimeMinutes
    }
}

// Class for dependency injection
@ApplicationScoped
class TimeEntryMapper @Inject constructor(
    private val timeEntryCostService: TimeEntryCostService
) {
    fun toTimeEntryResponseDTO(entry: TimeEntryModel): TimeEntryResponseDTO {
        val dto = entry.toTimeEntryResponseDTO()
        return dto.copy(
            costBreakdown = timeEntryCostService.calculateCostBreakdown(
                TimeEntryDTO(
                    employeeId = entry.employee.id!!,
                    projectId = entry.project.id!!,
                    date = entry.date,
                    hoursWorked = entry.hoursWorked,
                    note = entry.note,
                    hourlyRate = entry.hourlyRate,
                    billable = entry.billable,
                    invoiced = entry.invoiced,
                    catalogItemDescription = entry.catalogItemDescription,
                    catalogItemPrice = entry.catalogItemPrice,
                    catalogItems = entry.usedCatalogItems.map { 
                        TimeEntryCatalogItemDTO(
                            id = it.id,
                            timeEntryId = it.timeEntry.id,
                            catalogItemId = it.catalogItem.id,
                            quantity = it.quantity,
                            itemName = it.catalogItem.name,
                            unitPrice = it.unitPrice,
                            totalPrice = it.totalPrice
                        )
                    },
                    hasNightSurcharge = entry.hasNightSurcharge,
                    hasWeekendSurcharge = entry.hasWeekendSurcharge,
                    hasHolidaySurcharge = entry.hasHolidaySurcharge,
                    travelTimeMinutes = entry.travelTimeMinutes,
                    disposalCost = entry.disposalCost,
                    hasWaitingTime = entry.hasWaitingTime,
                    waitingTimeMinutes = entry.waitingTimeMinutes
                )
            )
        )
    }

    fun toTimeEntryModel(dto: TimeEntryDTO, employee: EmployeeModel, project: ProjectModel): TimeEntryModel {
        return dto.toTimeEntryModel(employee, project)
    }
}

