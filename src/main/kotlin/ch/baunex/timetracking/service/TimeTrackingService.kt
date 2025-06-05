package ch.baunex.timetracking.service

import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.mapper.TimeEntryMapper
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.user.repository.EmployeeRepository
import ch.baunex.project.repository.ProjectRepository
import ch.baunex.catalog.service.CatalogService
import ch.baunex.notes.model.NoteModel
import ch.baunex.user.service.EmployeeService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate

@ApplicationScoped
class TimeTrackingService @Inject constructor(
    private val timeEntryRepository: TimeEntryRepository,
    private val employeeRepository: EmployeeRepository,
    private val projectRepository: ProjectRepository,
    private val timeEntryMapper: TimeEntryMapper,
    private val timeEntryCatalogItemService: TimeEntryCatalogItemService,
    private val catalogService: CatalogService,
    private val employeeService: EmployeeService
) {

    @Transactional
    fun logTime(dto: TimeEntryDTO): TimeEntryModel {
        val employee = employeeRepository.findById(dto.employeeId)
            ?: throw IllegalArgumentException("Employee not found with id: ${dto.employeeId}")
        val project = projectRepository.findById(dto.projectId)
            ?: throw IllegalArgumentException("Project not found with id: ${dto.projectId}")

        // 1. TimeEntryModel neu anlegen
        val timeEntry = TimeEntryModel().apply {
            this.employee = employee
            this.project = project
            this.date = dto.date
            this.hoursWorked = dto.hoursWorked
            this.hourlyRate = employee.hourlyRate
            this.billable = dto.billable
            this.invoiced = dto.invoiced
            this.catalogItemDescription = dto.catalogItemDescription
            this.catalogItemPrice = dto.catalogItemPrice

            // 2. Notizen verarbeiten
            val parent = this
            this.notes = dto.notes.map { noteDto ->
                NoteModel().apply {
                    content    = noteDto.content
                    title      = noteDto.title
                    category   = noteDto.category
                    tags       = noteDto.tags
                    createdAt  = noteDto.createdAt
                    updatedAt  = noteDto.updatedAt
                    createdBy  = employeeService.findEmployeeById(noteDto.createdById)!!
                    timeEntry  = parent
                }
            }.toMutableList()
        }

        // 3. speichern
        timeEntryRepository.persist(timeEntry)

        // 4. Katalog-Items verknüpfen
        dto.catalogItems.forEach { catalogItemDto ->
            val catalogItemId = catalogItemDto.catalogItemId
                ?: throw IllegalArgumentException("Catalog item ID cannot be null")
            val catalogItem = catalogService.getCatalogItemById(catalogItemId)
                ?: throw IllegalArgumentException("Catalog item not found with id: $catalogItemId")
            timeEntryCatalogItemService.addCatalogItemToTimeEntry(timeEntry, catalogItem, catalogItemDto.quantity)
        }

        return timeEntry
    }

    fun getAllTimeEntries(): List<TimeEntryModel> {
        return timeEntryRepository.listAll()
    }

    fun getTimeEntryById(id: Long): TimeEntryModel? {
        return timeEntryRepository.findById(id)
    }

    @Transactional
    fun updateTimeEntry(id: Long, dto: TimeEntryDTO): TimeEntryModel? {
        val existingEntry = timeEntryRepository.findById(id) ?: return null
        val employee = employeeRepository.findById(dto.employeeId)
            ?: throw IllegalArgumentException("Employee not found with id: ${dto.employeeId}")
        val project = projectRepository.findById(dto.projectId)
            ?: throw IllegalArgumentException("Project not found with id: ${dto.projectId}")

        // Update the existing entry instead of creating a new one
        existingEntry.apply {
            this.employee = employee
            this.project = project
            this.date = dto.date
            this.hoursWorked = dto.hoursWorked
            existingEntry.notes.clear()
            val newNotes = dto.notes.map { noteDto ->
                NoteModel().apply {
                    this.content = noteDto.content
                    this.title = noteDto.title
                    this.category = noteDto.category
                    this.tags = noteDto.tags
                    this.createdAt = noteDto.createdAt
                    createdBy  = employeeService.findEmployeeById(noteDto.createdById)!!
                    this.timeEntry = existingEntry
                }
            }
            existingEntry.notes.addAll(newNotes)

            this.hourlyRate = employee.hourlyRate
            this.billable = dto.billable
            this.invoiced = dto.invoiced
            this.catalogItemDescription = dto.catalogItemDescription
            this.catalogItemPrice = dto.catalogItemPrice
            
            // Surcharges
            this.hasNightSurcharge = dto.hasNightSurcharge
            this.hasWeekendSurcharge = dto.hasWeekendSurcharge
            this.hasHolidaySurcharge = dto.hasHolidaySurcharge
            
            // Additional Costs
            this.travelTimeMinutes = dto.travelTimeMinutes
            this.disposalCost = dto.disposalCost
            this.hasWaitingTime = dto.hasWaitingTime
            this.waitingTimeMinutes = dto.waitingTimeMinutes
        }

        // Handle catalog items
        // First delete existing catalog items
        timeEntryCatalogItemService.deleteByTimeEntryId(id)
        // Then add new ones
        dto.catalogItems.forEach { catalogItemDto ->
            val catalogItemId = catalogItemDto.catalogItemId ?: throw IllegalArgumentException("Catalog item ID cannot be null")
            val catalogItem = catalogService.getCatalogItemById(catalogItemId)
                ?: throw IllegalArgumentException("Catalog item not found with id: $catalogItemId")
            timeEntryCatalogItemService.addCatalogItemToTimeEntry(existingEntry, catalogItem, catalogItemDto.quantity)
        }

        return existingEntry
    }

    fun getEntriesForEmployee(employeeId: Long): List<TimeEntryModel> =
        timeEntryRepository.list("employee.id", employeeId)

    fun getEntriesForProject(projectId: Long): List<TimeEntryModel> =
        timeEntryRepository.list("project.id", projectId)

    @Transactional
    fun deleteEntry(id: Long): Boolean {
        // First delete associated catalog items
        timeEntryCatalogItemService.deleteByTimeEntryId(id)
        // Then delete the time entry
        return timeEntryRepository.deleteById(id)
    }

    @Transactional
    fun approveEntry(entryId: Long, approverId: Long): Boolean {
        val entry = getTimeEntryById(entryId) ?: return false
        val approver = employeeService.findEmployeeById(approverId) ?: return false

        entry.approved = true
        entry.approvedBy = approver
        entry.approvedAt = LocalDate.now()
        return true
    }

}
