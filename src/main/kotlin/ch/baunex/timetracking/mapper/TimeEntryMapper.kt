package ch.baunex.timetracking.mapper

import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.mapper.toDto
import ch.baunex.notes.model.NoteModel
import ch.baunex.project.model.ProjectModel
import ch.baunex.timetracking.dto.ApprovalDTO
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import ch.baunex.timetracking.dto.TimeEntryCatalogItemDTO
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.service.TimeEntryCostService
import ch.baunex.user.model.EmployeeModel
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.LocalDate

@ApplicationScoped
class TimeEntryMapper @Inject constructor(
    private val timeEntryCostService: TimeEntryCostService
) {

    fun toTimeEntryResponseDTO(entry: TimeEntryModel): TimeEntryResponseDTO {
        // 1. Basis-DTO ohne notes
        val base = TimeEntryResponseDTO(
            id = entry.id,
            employeeId = entry.employee.id!!,
            employeeEmail = entry.employee.email,
            employeeFirstName = entry.employee.person.firstName,
            employeeLastName = entry.employee.person.lastName,
            projectId = entry.project.id!!,
            projectName = entry.project.name,
            date = entry.date,
            hoursWorked = entry.hoursWorked,
            title = entry.title,
            notes = emptyList(), // wird weiter unten überschrieben
            hourlyRate = entry.hourlyRate,
            cost = entry.hoursWorked * entry.hourlyRate,
            billable = entry.billable,
            invoiced = entry.invoiced,
            catalogItemDescription = entry.catalogItemDescription,
            catalogItemPrice = entry.catalogItemPrice,
            catalogItems = entry.usedCatalogItems.map { item ->
                TimeEntryCatalogItemDTO(
                    id = item.id,
                    timeEntryId = item.timeEntry.id!!,
                    catalogItemId = item.catalogItem.id!!,
                    quantity = item.quantity,
                    itemName = item.catalogItem.name,
                    unitPrice = item.unitPrice,
                    totalPrice = item.totalPrice
                )
            },
            hasNightSurcharge = entry.hasNightSurcharge,
            hasWeekendSurcharge = entry.hasWeekendSurcharge,
            hasHolidaySurcharge = entry.hasHolidaySurcharge,
            travelTimeMinutes = entry.travelTimeMinutes,
            disposalCost = entry.disposalCost,
            hasWaitingTime = entry.hasWaitingTime,
            waitingTimeMinutes = entry.waitingTimeMinutes,
            costBreakdown = null,
            approval = ApprovalDTO(
                approved = entry.approvedBy != null,
                approverId = entry.approvedBy?.id,
                approverName = entry.approvedBy?.person?.let { "${it.firstName} ${it.lastName}" } ?: ""
            )
        )

        // 2. Kopiere mit costBreakdown und mappe notes
        return base.copy(
            costBreakdown = timeEntryCostService.calculateCostBreakdown(
                TimeEntryDTO(
                    employeeId = entry.employee.id!!,
                    projectId = entry.project.id!!,
                    date = entry.date,
                    hoursWorked = entry.hoursWorked,
                    title = entry.title,
                    notes = entry.notes.map { noteModel ->
                        NoteDto(
                            id = noteModel.id!!,
                            projectId = noteModel.project?.id,
                            timeEntryId = noteModel.timeEntry?.id,
                            documentId = noteModel.document?.id,
                            title = noteModel.title,
                            content = noteModel.content,
                            category = noteModel.category,
                            tags = noteModel.tags,
                            attachments = noteModel.attachments.map { it.toDto() },
                            createdById = noteModel.createdBy.id!!,
                            createdAt = noteModel.createdAt,
                            updatedAt = noteModel.updatedAt
                        )
                    },
                    hourlyRate = entry.hourlyRate,
                    billable = entry.billable,
                    invoiced = entry.invoiced,
                    catalogItems = entry.usedCatalogItems.map { item ->
                        TimeEntryCatalogItemDTO(
                            id = item.id,
                            timeEntryId = item.timeEntry.id!!,
                            catalogItemId = item.catalogItem.id!!,
                            quantity = item.quantity,
                            itemName = item.catalogItem.name,
                            unitPrice = item.unitPrice,
                            totalPrice = item.totalPrice
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
            ),
            notes = entry.notes.map { noteModel ->
                NoteDto(
                    id = noteModel.id!!,
                    projectId = noteModel.project?.id,
                    timeEntryId = noteModel.timeEntry?.id,
                    documentId = noteModel.document?.id,
                    createdById = noteModel.createdBy.id!!,
                    createdAt = noteModel.createdAt,
                    updatedAt = noteModel.updatedAt,
                    title = noteModel.title,
                    content = noteModel.content,
                    category = noteModel.category,
                    tags = noteModel.tags,
                    attachments = noteModel.attachments.map { it.toDto() }
                )
            }
        )
    }

    fun toTimeEntryModel(dto: TimeEntryDTO, employee: EmployeeModel, project: ProjectModel): TimeEntryModel {
        return TimeEntryModel().apply {
            this.employee = employee
            this.project = project
            this.date = dto.date
            this.hoursWorked = dto.hoursWorked
            this.title = dto.title

            // Marke Dir eine Referenz auf das TimeEntryModel
            val timeEntryEntity = this

            this.notes = dto.notes.map { noteDto ->
                NoteModel().apply {
                    content      = noteDto.content
                    title        = noteDto.title
                    category     = noteDto.category
                    tags         = noteDto.tags
                    timeEntry    = timeEntryEntity
                    createdBy    = employee     // the EmployeeModel passed into toTimeEntryModel
                    createdAt    = noteDto.createdAt ?: LocalDate.now()
                    updatedAt    = noteDto.updatedAt ?: LocalDate.now()
                }
            }.toMutableList()

            this.hourlyRate = dto.hourlyRate ?: employee.hourlyRate
            this.billable = dto.billable
            this.invoiced = dto.invoiced

            this.hasNightSurcharge = dto.hasNightSurcharge
            this.hasWeekendSurcharge = dto.hasWeekendSurcharge
            this.hasHolidaySurcharge = dto.hasHolidaySurcharge

            this.travelTimeMinutes = dto.travelTimeMinutes
            this.disposalCost = dto.disposalCost
            this.hasWaitingTime = dto.hasWaitingTime
            this.waitingTimeMinutes = dto.waitingTimeMinutes
        }
    }
}
