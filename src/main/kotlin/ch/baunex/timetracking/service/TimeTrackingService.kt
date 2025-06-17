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
            this.employee    = employee
            this.project     = project
            this.date        = dto.date
            this.hoursWorked = dto.hoursWorked
            this.hourlyRate  = employee.hourlyRate
            this.billable    = dto.billable
            this.invoiced    = dto.invoiced
            this.title       = dto.title

            // 2. Notizen verarbeiten
            val parent = this
            this.notes = dto.notes.map { noteCreateDto ->
                NoteModel().apply {
                    this.project   = parent.project   // ← ganz wichtig!
                    this.timeEntry = parent
                    this.createdBy = employee
                    this.createdAt = LocalDate.now()
                    this.updatedAt = LocalDate.now()

                    this.title    = noteCreateDto.title
                    this.content  = noteCreateDto.content
                    this.category = noteCreateDto.category
                    this.tags     = noteCreateDto.tags
                }
            }.toMutableList()
        }

        // 3. speichern (inkl. Cascade auf notes)
        timeEntryRepository.persist(timeEntry)

        // 4. Katalog-Items verknüpfen ...
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
            this.title    = dto.title
            existingEntry.notes.clear()
            val newNotes = dto.notes.map { noteCreateDto ->
                NoteModel().apply {
                    this.content = noteCreateDto.content
                    this.title = noteCreateDto.title
                    this.category = noteCreateDto.category
                    this.tags = noteCreateDto.tags
                    this.timeEntry = existingEntry
                    createdBy  = employee
                    createdAt  = LocalDate.now()
                    updatedAt  = LocalDate.now()
                }
            }
            existingEntry.notes.addAll(newNotes)

            this.hourlyRate = employee.hourlyRate
            this.billable = dto.billable
            this.invoiced = dto.invoiced
            
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
