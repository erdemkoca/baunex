package ch.baunex.timetracking.service

import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.user.repository.EmployeeRepository
import ch.baunex.project.repository.ProjectRepository
import ch.baunex.catalog.service.CatalogService
import ch.baunex.notes.model.NoteModel
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.mapper.TimeEntryMapper
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
    private val timeEntryCatalogItemService: TimeEntryCatalogItemService,
    private val catalogService: CatalogService,
    private val employeeService: EmployeeService,
    private val timeEntryMapper: TimeEntryMapper
) {

    @Transactional
    fun logTime(dto: TimeEntryDTO): TimeEntryModel {
        val employee = employeeRepository.findById(dto.employeeId)
            ?: throw IllegalArgumentException("Employee not found with id: ${dto.employeeId}")
        val project = projectRepository.findById(dto.projectId)
            ?: throw IllegalArgumentException("Project not found with id: ${dto.projectId}")

        // Always create a single time entry, calculating hours worked from breaks
        return createSingleTimeEntry(dto, employee, project)
    }

    private fun createSingleTimeEntry(dto: TimeEntryDTO, employee: ch.baunex.user.model.EmployeeModel, project: ch.baunex.project.model.ProjectModel): TimeEntryModel {
        // Calculate hours worked by subtracting breaks from total duration
        val totalDurationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(dto.startTime, dto.endTime)
        val breakMinutes = dto.breaks.sumOf { breakItem ->
            java.time.temporal.ChronoUnit.MINUTES.between(breakItem.start, breakItem.end)
        }
        val workedMinutes = totalDurationMinutes - breakMinutes
        val calculatedHoursWorked = workedMinutes / 60.0
        
        println("DEBUG: Creating single time entry")
        println("DEBUG: Start time: ${dto.startTime}, End time: ${dto.endTime}")
        println("DEBUG: Total duration: ${totalDurationMinutes} minutes")
        println("DEBUG: Break minutes: ${breakMinutes}")
        println("DEBUG: Worked minutes: ${workedMinutes}")
        println("DEBUG: Calculated hours worked: ${calculatedHoursWorked}")
        
        val timeEntry = TimeEntryModel().apply {
            this.employee = employee
            this.project = project
            this.date = dto.date
            this.startTime = dto.startTime
            this.endTime = dto.endTime
            this.hoursWorked = calculatedHoursWorked
            this.hourlyRate = dto.hourlyRate ?: employee.hourlyRate
            this.billable = dto.billable
            this.invoiced = dto.invoiced
            this.title = dto.title.replace(Regex("\\s*\\(Teil\\s*\\d+\\)\\s*$"), "")

            // Surcharges
            this.hasNightSurcharge = dto.hasNightSurcharge
            this.hasWeekendSurcharge = dto.hasWeekendSurcharge
            this.hasHolidaySurcharge = dto.hasHolidaySurcharge
            
            // Additional Costs
            this.travelTimeMinutes = dto.travelTimeMinutes
            this.disposalCost = dto.disposalCost
            this.waitingTimeMinutes = dto.waitingTimeMinutes

            // Breaks - save as JSON
            this.breaks = ch.baunex.serialization.SerializationUtils.json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(ch.baunex.timetracking.dto.BreakDTO.serializer()),
                dto.breaks
            )

            // Notes
            this.notes = createNotes(dto.notes, this, employee)
        }

        timeEntryRepository.persist(timeEntry)

        // Catalog items
        dto.catalogItems.forEach { catalogItemDto ->
            val catalogItemId = catalogItemDto.catalogItemId
                ?: throw IllegalArgumentException("Catalog item ID cannot be null")
            val catalogItem = catalogService.getCatalogItemById(catalogItemId)
                ?: throw IllegalArgumentException("Catalog item not found with id: $catalogItemId")
            timeEntryCatalogItemService.addCatalogItemToTimeEntry(timeEntry, catalogItem, catalogItemDto.quantity)
        }

        return timeEntry
    }

    private fun createNotes(notes: List<ch.baunex.notes.dto.NoteDto>, timeEntry: TimeEntryModel, employee: ch.baunex.user.model.EmployeeModel): MutableList<ch.baunex.notes.model.NoteModel> {
        return notes.map { noteDto ->
            val noteCreator = if (noteDto.createdById != null) {
                employeeRepository.findById(noteDto.createdById)
                    ?: throw IllegalArgumentException("Note creator not found with id: ${noteDto.createdById}")
            } else {
                employee
            }
            
            ch.baunex.notes.model.NoteModel().apply {
                this.project = timeEntry.project
                this.timeEntry = timeEntry
                this.createdBy = noteCreator
                this.createdAt = noteDto.createdAt ?: LocalDate.now()
                this.updatedAt = noteDto.updatedAt ?: LocalDate.now()
                this.title = noteDto.title
                this.content = noteDto.content
                this.category = noteDto.category
                this.tags = noteDto.tags
            }
        }.toMutableList()
    }

    private fun calculateHoursWorked(start: java.time.LocalTime, end: java.time.LocalTime): Double {
        val startMinutes = start.hour * 60 + start.minute
        val endMinutes = end.hour * 60 + end.minute
        val diffMinutes = endMinutes - startMinutes
        return if (diffMinutes > 0) diffMinutes / 60.0 else 0.0
    }

    fun getAllTimeEntries(): List<TimeEntryModel> {
        return timeEntryRepository.listAll()
    }

    fun getTimeEntryById(id: Long): TimeEntryModel? {
        return timeEntryRepository.findById(id)
    }

    @Transactional
    fun updateTimeEntry(id: Long, dto: TimeEntryDTO): TimeEntryModel? {
        println("DEBUG: *** updateTimeEntry called with ID: $id ***")
        println("DEBUG: Breaks count: ${dto.breaks.size}")
        
        val existingEntry = timeEntryRepository.findById(id) ?: return null
        val employee = employeeRepository.findById(dto.employeeId)
            ?: throw IllegalArgumentException("Employee not found with id: ${dto.employeeId}")
        val project = projectRepository.findById(dto.projectId)
            ?: throw IllegalArgumentException("Project not found with id: ${dto.projectId}")

        // Always update the single entry, calculating hours worked from breaks
        println("DEBUG: Updating single entry with breaks")
        
        // Calculate hours worked by subtracting breaks from total duration
        val totalDurationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(dto.startTime, dto.endTime)
        val breakMinutes = dto.breaks.sumOf { breakItem ->
            java.time.temporal.ChronoUnit.MINUTES.between(breakItem.start, breakItem.end)
        }
        val workedMinutes = totalDurationMinutes - breakMinutes
        val calculatedHoursWorked = workedMinutes / 60.0
        
        println("DEBUG: Start time: ${dto.startTime}, End time: ${dto.endTime}")
        println("DEBUG: Total duration: ${totalDurationMinutes} minutes")
        println("DEBUG: Break minutes: ${breakMinutes}")
        println("DEBUG: Worked minutes: ${workedMinutes}")
        println("DEBUG: Calculated hours worked: ${calculatedHoursWorked}")
        
        // Update the existing entry
        existingEntry.apply {
            this.employee = employee
            this.project = project
            this.date = dto.date
            this.startTime = dto.startTime
            this.endTime = dto.endTime
            this.hoursWorked = calculatedHoursWorked
            this.title = dto.title.replace(Regex("\\s*\\(Teil\\s*\\d+\\)\\s*$"), "")
            this.hourlyRate = dto.hourlyRate ?: employee.hourlyRate
            this.billable = dto.billable
            this.invoiced = dto.invoiced
            
            // Surcharges
            this.hasNightSurcharge = dto.hasNightSurcharge
            this.hasWeekendSurcharge = dto.hasWeekendSurcharge
            this.hasHolidaySurcharge = dto.hasHolidaySurcharge
            
            // Additional Costs
            this.travelTimeMinutes = dto.travelTimeMinutes
            this.disposalCost = dto.disposalCost
            this.waitingTimeMinutes = dto.waitingTimeMinutes
            
            // Breaks - save as JSON
            this.breaks = ch.baunex.serialization.SerializationUtils.json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(ch.baunex.timetracking.dto.BreakDTO.serializer()),
                dto.breaks
            )
            
            // Update notes
            this.notes.clear()
            val newNotes = createNotes(dto.notes, this, employee)
            this.notes.addAll(newNotes)
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

    @Transactional
    fun approveEntry(entryId: Long, approverId: Long): Boolean {
        val entry = getTimeEntryById(entryId) ?: return false
        val approver = employeeService.findEmployeeById(approverId) ?: return false

        entry.approved = true
        entry.approvedBy = approver
        entry.approvedAt = LocalDate.now()
        return true
    }

    fun getTimeEntryWithBreaks(id: Long): TimeEntryDTO? {
        val timeEntry = timeEntryRepository.findById(id) ?: return null
        
        // With the new single-entry approach, we just return the entry as-is
        // The breaks are stored in the DTO, not in the database
        return timeEntryMapper.toTimeEntryResponseDTO(timeEntry)
    }

}
