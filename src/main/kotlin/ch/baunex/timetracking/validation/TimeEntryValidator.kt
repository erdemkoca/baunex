package ch.baunex.timetracking.validation

import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.exception.*
import ch.baunex.timetracking.service.WorkSummaryService
import ch.baunex.user.service.EmployeeService
import ch.baunex.project.service.ProjectService
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.timetracking.service.HolidayDefinitionService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.time.LocalDate
import java.time.LocalTime
import java.time.DayOfWeek
import java.time.temporal.ChronoUnit

/**
 * Validator for time entry data
 * Performs comprehensive validation of all business rules and data integrity
 */
@ApplicationScoped
class TimeEntryValidator @Inject constructor(
    private val employeeService: EmployeeService,
    private val projectService: ProjectService,
    private val workSummaryService: WorkSummaryService,
    private val timeEntryRepository: TimeEntryRepository,
    private val holidayDefinitionService: HolidayDefinitionService
) {
    
    private val log = Logger.getLogger(TimeEntryValidator::class.java)
    
    companion object {
        const val MAX_HOURS_PER_DAY = 24.0
        const val MAX_HOURS_PER_WEEK = 168.0 // 7 * 24
        const val MIN_BREAK_DURATION_MINUTES = 15
        const val MAX_BREAK_DURATION_MINUTES = 240 // 4 hours
        const val MAX_FUTURE_DAYS = 30
        const val MAX_PAST_DAYS = 365
    }
    
    /**
     * Validates a complete time entry DTO
     */
    fun validateTimeEntry(dto: TimeEntryDTO, isUpdate: Boolean = false) {
        log.debug("Validating time entry: employeeId=${dto.employeeId}, projectId=${dto.projectId}, date=${dto.date}")
        
        // Basic field validation
        validateRequiredFields(dto)
        
        // Entity existence validation
        validateEntities(dto)
        
        // Date and time validation
        validateDateAndTime(dto)
        
        // Business rule validation
        validateBusinessRules(dto, isUpdate)
        
        // Break validation
        validateBreaks(dto)
        
        // Hours validation
        validateHours(dto)
        
        log.debug("Time entry validation completed successfully")
    }
    
    /**
     * Validates required fields are present
     */
    private fun validateRequiredFields(dto: TimeEntryDTO) {
        requireNotNull(dto.employeeId) { "employeeId is required" }
        requireNotNull(dto.projectId) { "projectId is required" }
        requireNotNull(dto.date) { "date is required" }
        requireNotNull(dto.startTime) { "startTime is required" }
        requireNotNull(dto.endTime) { "endTime is required" }
        require(!dto.title.isNullOrBlank()) { "title is required" }
    }
    
    /**
     * Validates that referenced entities exist
     */
    private fun validateEntities(dto: TimeEntryDTO) {
        // Validate employee exists
        val employee = employeeService.findEmployeeById(dto.employeeId!!)
            ?: throw EmployeeNotFoundException(dto.employeeId!!)
        
        // Validate project exists
        val project = projectService.getProjectWithEntries(dto.projectId!!)
            ?: throw ProjectNotFoundException(dto.projectId!!)
        
        // Validate employee start date
        if (dto.date!!.isBefore(employee.startDate)) {
            throw InvalidDateException(
                dto.date!!,
                "Datum liegt vor dem Eintrittsdatum des Mitarbeiters (${employee.startDate})"
            )
        }
    }
    
    /**
     * Validates date and time constraints
     */
    private fun validateDateAndTime(dto: TimeEntryDTO) {
        val today = LocalDate.now()
        
        // Check if date is too far in the future
        if (dto.date!!.isAfter(today.plusDays(MAX_FUTURE_DAYS.toLong()))) {
            throw InvalidDateException(
                dto.date!!,
                "Datum liegt mehr als $MAX_FUTURE_DAYS Tage in der Zukunft"
            )
        }
        
        // Check if date is too far in the past
        if (dto.date!!.isBefore(today.minusDays(MAX_PAST_DAYS.toLong()))) {
            throw InvalidDateException(
                dto.date!!,
                "Datum liegt mehr als $MAX_PAST_DAYS Tage in der Vergangenheit"
            )
        }
        
        // Validate time range
        validateTimeRange(dto.startTime!!, dto.endTime!!)
    }
    
    /**
     * Validates time range logic
     */
    private fun validateTimeRange(startTime: LocalTime, endTime: LocalTime) {
        // Check if start time is before end time
        if (!startTime.isBefore(endTime)) {
            throw InvalidTimeRangeException(
                startTime,
                endTime,
                "Startzeit muss vor Endzeit liegen"
            )
        }
        
        // Check if duration is reasonable (not too short, not too long)
        val durationMinutes = ChronoUnit.MINUTES.between(startTime, endTime)
        
        if (durationMinutes < 15) {
            throw InvalidTimeRangeException(
                startTime,
                endTime,
                "Zeitbereich muss mindestens 15 Minuten betragen"
            )
        }
        
        if (durationMinutes > 1440) { // 24 hours
            throw InvalidTimeRangeException(
                startTime,
                endTime,
                "Zeitbereich darf maximal 24 Stunden betragen"
            )
        }
    }
    
    /**
     * Validates business rules
     */
    private fun validateBusinessRules(dto: TimeEntryDTO, isUpdate: Boolean) {
        // Check for overlapping time entries (only for new entries)
        if (!isUpdate) {
            validateNoOverlap(dto)
        }
        
        // Validate weekend work (if applicable)
        validateWeekendWork(dto)
        
        // Validate holiday work (if applicable)
        validateHolidayWork(dto)
    }
    
    /**
     * Validates that there are no overlapping time entries
     */
    private fun validateNoOverlap(dto: TimeEntryDTO) {
        log.debug("Checking for overlapping time entries for employee ${dto.employeeId} on ${dto.date}")
        
        // Find existing time entries for the same employee on the same date
        val existingEntries = timeEntryRepository.list(
            "employee.id = ?1 and date = ?2",
            dto.employeeId,
            dto.date
        )
        
        // Check for overlaps with existing entries
        existingEntries.forEach { existingEntry ->
            if (hasTimeOverlap(
                dto.startTime!!, dto.endTime!!,
                existingEntry.startTime, existingEntry.endTime
            )) {
                throw DuplicateTimeEntryException(
                    dto.employeeId!!,
                    dto.date!!,
                    dto.startTime!!,
                    dto.endTime!!
                )
            }
        }
    }
    
    /**
     * Checks if two time ranges overlap
     */
    private fun hasTimeOverlap(
        start1: LocalTime, end1: LocalTime,
        start2: LocalTime, end2: LocalTime
    ): Boolean {
        return start1 < end2 && start2 < end1
    }
    
    /**
     * Validates weekend work rules
     */
    private fun validateWeekendWork(dto: TimeEntryDTO) {
        val dayOfWeek = dto.date!!.dayOfWeek
        
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            log.info("Weekend work detected for employee ${dto.employeeId} on ${dto.date}")
            
            // Check if weekend surcharge is applied
            if (!dto.hasWeekendSurcharge) {
                log.warn("Weekend work without weekend surcharge for employee ${dto.employeeId}")
            }
        }
    }
    
    /**
     * Validates holiday work rules
     */
    private fun validateHolidayWork(dto: TimeEntryDTO) {
        // Check if the date is a public holiday
        val isHoliday = holidayDefinitionService.isHoliday(dto.date!!)
        
        if (isHoliday) {
            log.info("Holiday work detected for employee ${dto.employeeId} on ${dto.date}")
            
            // Check if holiday surcharge is applied
            if (!dto.hasHolidaySurcharge) {
                log.warn("Holiday work without holiday surcharge for employee ${dto.employeeId}")
            }
        }
    }
    
    /**
     * Validates break configuration
     */
    private fun validateBreaks(dto: TimeEntryDTO) {
        dto.breaks.forEach { breakItem ->
            validateBreak(breakItem, dto.startTime!!, dto.endTime!!)
        }
        
        // Validate total break time doesn't exceed work time
        val totalWorkMinutes = ChronoUnit.MINUTES.between(dto.startTime!!, dto.endTime!!)
        val totalBreakMinutes = dto.breaks.sumOf { breakItem ->
            ChronoUnit.MINUTES.between(breakItem.start, breakItem.end)
        }
        
        if (totalBreakMinutes >= totalWorkMinutes) {
            throw InvalidBreakException(
                "Total breaks: ${totalBreakMinutes}min",
                "Gesamte Pausenzeit darf nicht länger als Arbeitszeit sein"
            )
        }
    }
    
    /**
     * Validates individual break
     */
    private fun validateBreak(breakItem: ch.baunex.timetracking.dto.BreakDTO, workStart: LocalTime, workEnd: LocalTime) {
        // Validate break time range
        if (!breakItem.start.isBefore(breakItem.end)) {
            throw InvalidBreakException(
                "${breakItem.start}-${breakItem.end}",
                "Pausenstart muss vor Pausenende liegen"
            )
        }
        
        // Validate break is within work time
        if (breakItem.start.isBefore(workStart) || breakItem.end.isAfter(workEnd)) {
            throw InvalidBreakException(
                "${breakItem.start}-${breakItem.end}",
                "Pause muss innerhalb der Arbeitszeit liegen"
            )
        }
        
        // Validate break duration
        val breakDuration = ChronoUnit.MINUTES.between(breakItem.start, breakItem.end)
        
        if (breakDuration < MIN_BREAK_DURATION_MINUTES) {
            throw InvalidBreakException(
                "${breakItem.start}-${breakItem.end}",
                "Pause muss mindestens $MIN_BREAK_DURATION_MINUTES Minuten dauern"
            )
        }
        
        if (breakDuration > MAX_BREAK_DURATION_MINUTES) {
            throw InvalidBreakException(
                "${breakItem.start}-${breakItem.end}",
                "Pause darf maximal $MAX_BREAK_DURATION_MINUTES Minuten dauern"
            )
        }
    }
    
    /**
     * Validates calculated hours
     */
    private fun validateHours(dto: TimeEntryDTO) {
        val totalWorkMinutes = ChronoUnit.MINUTES.between(dto.startTime!!, dto.endTime!!)
        val totalBreakMinutes = dto.breaks.sumOf { breakItem ->
            ChronoUnit.MINUTES.between(breakItem.start, breakItem.end)
        }
        val calculatedHours = (totalWorkMinutes - totalBreakMinutes) / 60.0
        
        // Validate calculated hours are reasonable
        if (calculatedHours < 0) {
            throw InvalidHoursException(
                calculatedHours,
                "Berechnete Arbeitsstunden sind negativ"
            )
        }
        
        if (calculatedHours > MAX_HOURS_PER_DAY) {
            throw InvalidHoursException(
                calculatedHours,
                "Arbeitsstunden dürfen maximal $MAX_HOURS_PER_DAY Stunden pro Tag betragen"
            )
        }
        
        // Validate against manually entered hours if provided
        dto.hoursWorked?.let { manualHours ->
            val difference = kotlin.math.abs(calculatedHours - manualHours)
            if (difference > 0.5) { // Allow 30 minutes difference
                log.warn("Significant difference between calculated ($calculatedHours) and manual ($manualHours) hours for employee ${dto.employeeId}")
            }
        }
    }
} 