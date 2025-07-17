package ch.baunex.timetracking.service

import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.user.repository.EmployeeRepository
import ch.baunex.project.repository.ProjectRepository
import ch.baunex.catalog.service.CatalogService
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.mapper.TimeEntryMapper
import ch.baunex.timetracking.model.ApprovalStatus
import ch.baunex.user.service.EmployeeService
import ch.baunex.timetracking.validation.TimeEntryValidator
import ch.baunex.timetracking.exception.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate
import org.jboss.logging.Logger

@ApplicationScoped
class TimeTrackingService @Inject constructor(
    private val timeEntryRepository: TimeEntryRepository,
    private val employeeRepository: EmployeeRepository,
    private val projectRepository: ProjectRepository,
    private val timeEntryCatalogItemService: TimeEntryCatalogItemService,
    private val catalogService: CatalogService,
    private val employeeService: EmployeeService,
    private val timeEntryMapper: TimeEntryMapper,
    private val timeEntryValidator: TimeEntryValidator
) {
    
    private val log = Logger.getLogger(TimeTrackingService::class.java)

    @Transactional
    fun logTime(dto: TimeEntryDTO): TimeEntryModel {
        log.info("Creating new time entry for employee ${dto.employeeId} on project ${dto.projectId} for date ${dto.date}")
        
        // Validate the time entry before processing
        timeEntryValidator.validateTimeEntry(dto, isUpdate = false)
        
        val employee = employeeRepository.findById(dto.employeeId!!)
            ?: throw EmployeeNotFoundException(dto.employeeId!!)
        val project = projectRepository.findById(dto.projectId!!)
            ?: throw ProjectNotFoundException(dto.projectId!!)

        // Always create a single time entry, calculating hours worked from breaks
        val timeEntry = createSingleTimeEntry(dto, employee, project)
        
        log.info("Successfully created time entry with ID ${timeEntry.id}")
        return timeEntry
    }

    private fun createSingleTimeEntry(dto: TimeEntryDTO, employee: ch.baunex.user.model.EmployeeModel, project: ch.baunex.project.model.ProjectModel): TimeEntryModel {
        // Calculate hours worked by subtracting breaks from total duration
        val totalDurationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(dto.startTime, dto.endTime)
        val breakMinutes = dto.breaks.sumOf { breakItem ->
            java.time.temporal.ChronoUnit.MINUTES.between(breakItem.start, breakItem.end)
        }
        val workedMinutes = totalDurationMinutes - breakMinutes
        val calculatedHoursWorked = workedMinutes / 60.0
        
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
        log.debug("Fetching time entry with ID: $id")
        val entry = timeEntryRepository.findById(id)
        if (entry == null) {
            log.warn("Time entry with ID $id not found")
        }
        return entry
    }

    @Transactional
    fun updateTimeEntry(id: Long, dto: TimeEntryDTO): TimeEntryModel? {
        log.info("Updating time entry with ID: $id for employee ${dto.employeeId}")
        
        val existingEntry = timeEntryRepository.findById(id) 
            ?: throw TimeEntryNotFoundException(id)
        
        // Validate the time entry before processing
        timeEntryValidator.validateTimeEntry(dto, isUpdate = true)
        
        val employee = employeeRepository.findById(dto.employeeId!!)
            ?: throw EmployeeNotFoundException(dto.employeeId!!)
        val project = projectRepository.findById(dto.projectId!!)
            ?: throw ProjectNotFoundException(dto.projectId!!)

        // Always update the single entry, calculating hours worked from breaks
        log.debug("Updating single entry with ${dto.breaks.size} breaks")
        
        // Calculate hours worked by subtracting breaks from total duration
        val totalDurationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(dto.startTime!!, dto.endTime!!)
        val breakMinutes = dto.breaks.sumOf { breakItem ->
            java.time.temporal.ChronoUnit.MINUTES.between(breakItem.start, breakItem.end)
        }
        val workedMinutes = totalDurationMinutes - breakMinutes
        val calculatedHoursWorked = workedMinutes / 60.0
        
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
            val catalogItemId = catalogItemDto.catalogItemId 
                ?: throw MissingRequiredFieldException("catalogItemId")
            val catalogItem = catalogService.getCatalogItemById(catalogItemId)
                ?: throw IllegalArgumentException("Catalog item not found with id: $catalogItemId")
            timeEntryCatalogItemService.addCatalogItemToTimeEntry(existingEntry, catalogItem, catalogItemDto.quantity)
        }

        log.info("Successfully updated time entry with ID ${existingEntry.id}")
        return existingEntry
    }

    @Transactional
    fun approveEntry(entryId: Long, approverId: Long): Boolean {
        log.info("Approving time entry $entryId by approver $approverId")
        
        val entry = getTimeEntryById(entryId) 
            ?: throw TimeEntryNotFoundException(entryId)
        val approver = employeeService.findEmployeeById(approverId) 
            ?: throw EmployeeNotFoundException(approverId)

        entry.approvalStatus = ApprovalStatus.APPROVED
        entry.approvedBy = approver
        entry.approvedAt = LocalDate.now()
        
        log.info("Successfully approved time entry $entryId")
        return true
    }

    @Transactional
    fun approveWeeklyEntries(employeeId: Long, fromDate: LocalDate, toDate: LocalDate, approverId: Long): Boolean {
        log.info("Approving weekly entries for employee $employeeId from $fromDate to $toDate by approver $approverId")
        
        val approver = employeeService.findEmployeeById(approverId) 
            ?: throw EmployeeNotFoundException(approverId)
        
        // Find all time entries for the employee in the date range
        val entries = timeEntryRepository.list("employee.id = ?1 and date between ?2 and ?3", employeeId, fromDate, toDate)
        
        if (entries.isEmpty()) {
            log.warn("No time entries found for employee $employeeId in date range $fromDate to $toDate")
            return false
        }
        
        // Approve all entries
        entries.forEach { entry ->
            entry.approvalStatus = ApprovalStatus.APPROVED
            entry.approvedBy = approver
            entry.approvedAt = LocalDate.now()
        }
        
        log.info("Successfully approved ${entries.size} time entries for employee $employeeId")
        return true
    }

    fun getTimeEntryWithBreaks(id: Long): TimeEntryDTO? {
        val timeEntry = timeEntryRepository.findById(id) ?: return null
        
        // With the new single-entry approach, we just return the entry as-is
        // The breaks are stored in the DTO, not in the database
        return timeEntryMapper.toTimeEntryResponseDTO(timeEntry)
    }

}
