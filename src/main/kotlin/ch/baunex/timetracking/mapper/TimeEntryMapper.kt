package ch.baunex.timetracking.mapper

import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.mapper.toDto
import ch.baunex.timetracking.dto.*
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.service.TimeEntryCostService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class TimeEntryMapper @Inject constructor(
    private val timeEntryCostService: TimeEntryCostService
) {

    fun toTimeEntryResponseDTO(entry: TimeEntryModel): TimeEntryDTO {
        // 1) Compute the cost breakdown directly from the model
        val breakdown = timeEntryCostService.calculateCostBreakdown(entry)

        // 2) Build and return your single Response DTO:
        return TimeEntryDTO(
            // --- core time‐entry fields ---
            id                  = entry.id,
            employeeId          = entry.employee.id!!,
            projectId           = entry.project.id!!,
            date                = entry.date,
            startTime = entry.startTime,
            hoursWorked         = entry.hoursWorked,
            title               = entry.title,
            notes               = entry.notes.map { nm ->
                NoteDto(
                    id           = nm.id!!,
                    projectId    = nm.project?.id,
                    timeEntryId  = nm.timeEntry?.id,
                    documentId   = nm.document?.id,
                    title        = nm.title,
                    content      = nm.content,
                    category     = nm.category,
                    tags         = nm.tags,
                    attachments  = nm.attachments.map { it.toDto() },
                    createdById  = nm.createdBy.id!!,
                    createdAt    = nm.createdAt,
                    updatedAt    = nm.updatedAt
                )
            },
            hourlyRate          = entry.hourlyRate,
            billable            = entry.billable,
            invoiced            = entry.invoiced,
            catalogItems        = entry.usedCatalogItems.map { ci ->
                TimeEntryCatalogItemDTO(
                    id            = ci.id,
                    timeEntryId   = ci.timeEntry.id!!,
                    catalogItemId = ci.catalogItem.id!!,
                    quantity      = ci.quantity,
                    itemName      = ci.catalogItem.name,
                    unitPrice     = ci.unitPrice,
                    totalPrice    = ci.totalPrice
                )
            },

            // --- input flags & costs ---
            hasNightSurcharge   = entry.hasNightSurcharge,
            hasWeekendSurcharge = entry.hasWeekendSurcharge,
            hasHolidaySurcharge = entry.hasHolidaySurcharge,
            travelTimeMinutes   = entry.travelTimeMinutes,
            waitingTimeMinutes  = entry.waitingTimeMinutes,
            disposalCost        = entry.disposalCost,

            // --- the computed breakdown (all fields in one object) ---
            costBreakdown       = breakdown,

            // --- envelope‐only fields for the UI list view ---
            employeeEmail       = entry.employee.email,
            employeeFirstName   = entry.employee.person.firstName,
            employeeLastName    = entry.employee.person.lastName,
            projectName         = entry.project.name,
            cost                = breakdown.grandTotal,
            approval            = ApprovalDTO(
                approved     = entry.approvedBy != null,
                approverId   = entry.approvedBy?.id,
                approverName = entry.approvedBy
                    ?.person
                    ?.let { "${it.firstName} ${it.lastName}" }
                    .orEmpty()
            )
        )
    }
}
