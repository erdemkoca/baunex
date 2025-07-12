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
        println("DEBUG: Mapper called for entry ID: ${entry.id}")
        println("DEBUG: Entry employee: ${entry.employee}")
        println("DEBUG: Entry project: ${entry.project}")
        println("DEBUG: Entry employee ID: ${entry.employee?.id}")
        println("DEBUG: Entry project ID: ${entry.project?.id}")
        
        // 1) Compute the cost breakdown directly from the model
        println("DEBUG: About to calculate cost breakdown")
        val breakdown = try {
            timeEntryCostService.calculateCostBreakdown(entry)
        } catch (e: Exception) {
            println("DEBUG: Error in cost breakdown calculation: ${e.message}")
            e.printStackTrace()
            throw e
        }
        println("DEBUG: Cost breakdown calculated successfully")

        // 2) Build and return your single Response DTO:
        return TimeEntryDTO(
            // --- core time‐entry fields ---
            id                  = entry.id,
            employeeId          = entry.employee?.id ?: throw IllegalArgumentException("Employee is null for entry ${entry.id}"),
            projectId           = entry.project?.id ?: throw IllegalArgumentException("Project is null for entry ${entry.id}"),
            date                = entry.date,
            startTime = entry.startTime,
            endTime        = entry.endTime,
            hoursWorked         = entry.hoursWorked,
            title               = entry.title,
            notes               = try {
                println("DEBUG: About to map notes, count: ${entry.notes.size}")
                entry.notes.filter { it.id != null }.map { nm ->
                    println("DEBUG: Mapping note: ${nm.id}, createdBy: ${nm.createdBy}")
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
                        createdById  = nm.createdBy?.id ?: entry.employee?.id ?: throw IllegalArgumentException("Note createdBy is null and no fallback employee available"),
                        createdAt    = nm.createdAt,
                        updatedAt    = nm.updatedAt
                    )
                }
            } catch (e: Exception) {
                println("DEBUG: Error in notes mapping: ${e.message}")
                e.printStackTrace()
                throw e
            },
            hourlyRate          = entry.hourlyRate,
            billable            = entry.billable,
            invoiced            = entry.invoiced,
            catalogItems        = try {
                println("DEBUG: About to map catalog items, count: ${entry.usedCatalogItems.size}")
                entry.usedCatalogItems.map { ci ->
                    println("DEBUG: Mapping catalog item: ${ci.id}")
                    TimeEntryCatalogItemDTO(
                        id            = ci.id,
                        timeEntryId   = ci.timeEntry.id!!,
                        catalogItemId = ci.catalogItem.id!!,
                        quantity      = ci.quantity,
                        itemName      = ci.catalogItem.name,
                        unitPrice     = ci.unitPrice,
                        totalPrice    = ci.totalPrice
                    )
                }
            } catch (e: Exception) {
                println("DEBUG: Error in catalog items mapping: ${e.message}")
                e.printStackTrace()
                throw e
            },

            // --- input flags & costs ---
            hasNightSurcharge   = entry.hasNightSurcharge,
            hasWeekendSurcharge = entry.hasWeekendSurcharge,
            hasHolidaySurcharge = entry.hasHolidaySurcharge,
            travelTimeMinutes   = entry.travelTimeMinutes,
            waitingTimeMinutes  = entry.waitingTimeMinutes,
            disposalCost        = entry.disposalCost,

            // --- breaks (frontend-only, not stored in database) ---
            breaks              = try {
                if (entry.breaks.isNotEmpty() && entry.breaks != "[]") {
                    ch.baunex.serialization.SerializationUtils.json.decodeFromString(entry.breaks)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                println("DEBUG: Error parsing breaks JSON: ${e.message}")
                emptyList()
            },

            // --- the computed breakdown (all fields in one object) ---
            costBreakdown       = breakdown,

            // --- envelope‐only fields for the UI list view ---
            employeeEmail       = entry.employee.email,
            employeeFirstName   = entry.employee.person.firstName,
            employeeLastName    = entry.employee.person.lastName,
            projectName         = entry.project.name,
            cost                = breakdown.grandTotal,
            approval            = ApprovalDTO(
                approved     = entry.approvalStatus == ch.baunex.timetracking.model.ApprovalStatus.APPROVED,
                approverId   = entry.approvedBy?.id,
                approverName = entry.approvedBy
                    ?.person
                    ?.let { "${it.firstName} ${it.lastName}" }
                    .orEmpty(),
                approvedAt   = entry.approvedAt,
                status       = entry.approvalStatus.name
            )
        )
    }
}
