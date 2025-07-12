package ch.baunex.timetracking.facade

import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.mapper.TimeEntryMapper
import ch.baunex.timetracking.service.TimeTrackingService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class TimeTrackingFacade @Inject constructor(
    private val timeTrackingService: TimeTrackingService,
    private val timeEntryMapper: TimeEntryMapper
) {

    @Transactional
    fun logTime(dto: TimeEntryDTO): TimeEntryDTO {
        val model = timeTrackingService.logTime(dto) // führt persist etc. aus
        return timeEntryMapper.toTimeEntryResponseDTO(model)
    }

    fun getAllTimeEntries(): List<TimeEntryDTO> {
        return timeTrackingService.getAllTimeEntries().map { timeEntryMapper.toTimeEntryResponseDTO(it) }
    }

    fun getTimeEntryById(id: Long): TimeEntryDTO? {
        return timeTrackingService.getTimeEntryWithBreaks(id)
    }

    @Transactional
    fun updateTimeEntry(id: Long, dto: TimeEntryDTO): TimeEntryDTO? {
        println("DEBUG: Facade updateTimeEntry called with ID: $id")
        val model = timeTrackingService.updateTimeEntry(id, dto)
        println("DEBUG: Service returned model: ${model?.id}")
        println("DEBUG: Model employee: ${model?.employee?.id}")
        println("DEBUG: Model project: ${model?.project?.id}")
        
        return model?.let { 
            try {
                timeEntryMapper.toTimeEntryResponseDTO(it)
            } catch (e: Exception) {
                println("DEBUG: Error in mapper: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }

    fun approveEntry(entryId: Long, approverId: Long): Boolean {
        timeTrackingService.approveEntry(entryId, approverId)
        return true
    }

    fun approveWeeklyEntries(employeeId: Long, from: String, to: String, approverId: Long): Boolean {
        val fromDate = java.time.LocalDate.parse(from)
        val toDate = java.time.LocalDate.parse(to)
        return timeTrackingService.approveWeeklyEntries(employeeId, fromDate, toDate, approverId)
    }
}
