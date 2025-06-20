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
        return timeTrackingService.getTimeEntryById(id)?.let { timeEntryMapper.toTimeEntryResponseDTO(it) }
    }

    fun approveEntry(entryId: Long, approverId: Long): Boolean {
        timeTrackingService.approveEntry(entryId, approverId)
        return true
    }
}
