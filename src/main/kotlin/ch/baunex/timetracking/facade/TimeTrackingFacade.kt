package ch.baunex.timetracking.facade

import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.mapper.TimeEntryMapper
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.timetracking.service.TimeTrackingService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class TimeTrackingFacade @Inject constructor(
    private val timeEntryRepository: TimeEntryRepository,
    private val timeTrackingService: TimeTrackingService,
    private val timeEntryMapper: TimeEntryMapper
) {

    @Transactional
    fun logTime(dto: TimeEntryDTO): TimeEntryDTO {
        val model = timeTrackingService.logTime(dto) // führt persist etc. aus
        return timeEntryMapper.toTimeEntryResponseDTO(model)
    }

    @Transactional
    fun updateTimeEntry(id: Long, dto: TimeEntryDTO): TimeEntryDTO? {
        val model = timeTrackingService.updateTimeEntry(id, dto) ?: return null
        return timeEntryMapper.toTimeEntryResponseDTO(model)
    }

    fun getAllTimeEntries(): List<TimeEntryDTO> {
        return timeTrackingService.getAllTimeEntries().map { timeEntryMapper.toTimeEntryResponseDTO(it) }
    }

    fun getTimeEntryById(id: Long): TimeEntryDTO? {
        return timeTrackingService.getTimeEntryById(id)?.let { timeEntryMapper.toTimeEntryResponseDTO(it) }
    }

    @Transactional
    fun deleteTimeEntry(id: Long): Boolean {
        return timeEntryRepository.deleteById(id)
    }

     fun approveEntry(entryId: Long, approverId: Long): Boolean {
        timeTrackingService.approveEntry(entryId, approverId)
        return true
    }
}
