package ch.baunex.timetracking.facade

import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.mapper.TimeEntryMapper
import ch.baunex.timetracking.service.TimeTrackingService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.jboss.logging.Logger

@ApplicationScoped
class TimeTrackingFacade @Inject constructor(
    private val timeTrackingService: TimeTrackingService,
    private val timeEntryMapper: TimeEntryMapper
) {
    private val log = Logger.getLogger(TimeTrackingFacade::class.java)

    @Transactional
    fun logTime(dto: TimeEntryDTO): TimeEntryDTO {
        log.info("Logging time entry for employee ${dto.employeeId} on project ${dto.projectId}")
        val model = timeTrackingService.logTime(dto) // führt persist etc. aus
        return timeEntryMapper.toTimeEntryResponseDTO(model)
    }

    fun getAllTimeEntries(): List<TimeEntryDTO> {
        log.debug("Getting all time entries")
        return timeTrackingService.getAllTimeEntries().map { timeEntryMapper.toTimeEntryResponseDTO(it) }
    }

    fun getTimeEntryById(id: Long): TimeEntryDTO? {
        log.debug("Getting time entry by ID: $id")
        return timeTrackingService.getTimeEntryWithBreaks(id)
    }

    @Transactional
    fun updateTimeEntry(id: Long, dto: TimeEntryDTO): TimeEntryDTO? {
        log.info("Updating time entry with ID: $id for employee ${dto.employeeId}")
        val model = timeTrackingService.updateTimeEntry(id, dto)
        log.debug("Service returned model: ${model?.id}")
        
        return model?.let { 
            try {
                timeEntryMapper.toTimeEntryResponseDTO(it)
            } catch (e: Exception) {
                log.error("Error in mapper for time entry $id", e)
                throw e
            }
        }
    }

    fun approveEntry(entryId: Long, approverId: Long): Boolean {
        log.info("Approving time entry $entryId by approver $approverId")
        timeTrackingService.approveEntry(entryId, approverId)
        return true
    }

    fun approveWeeklyEntries(employeeId: Long, from: String, to: String, approverId: Long): Boolean {
        log.info("Approving weekly entries for employee $employeeId from $from to $to by approver $approverId")
        val fromDate = java.time.LocalDate.parse(from)
        val toDate = java.time.LocalDate.parse(to)
        return timeTrackingService.approveWeeklyEntries(employeeId, fromDate, toDate, approverId)
    }
}
