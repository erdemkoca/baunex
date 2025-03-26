package ch.baunex.timetracking.facade

import ch.baunex.project.service.ProjectService
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.timetracking.service.TimeTrackingService
import ch.baunex.user.service.UserService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate

@ApplicationScoped
class TimeTrackingFacade @Inject constructor(
    private val timeEntryRepository: TimeEntryRepository,
    private val userService: UserService,
    private val projectService: ProjectService,
    private val timeTrackingService: TimeTrackingService
) {

    @Transactional
    fun logTime(dto: TimeEntryDTO): TimeEntryModel {
        val user = userService.getUserById(dto.userId)
            ?: throw IllegalArgumentException("User not found")
        val project = projectService.getProjectById(dto.projectId)
            ?: throw IllegalArgumentException("Project not found")

        val entry = TimeEntryModel().apply {
            this.user = user
            this.project = project
            this.date = dto.date
            this.hoursWorked = dto.hoursWorked
            this.note = dto.note
        }

        timeEntryRepository.persist(entry)
        return entry
    }

    fun getAllTimeEntries(): List<TimeEntryResponseDTO> {
        return timeTrackingService.getAllTimeEntries()
    }

    fun getTimeEntryById(id: Long): TimeEntryResponseDTO? {
        return timeTrackingService.getTimeEntryById(id)?.let { TimeEntryResponseDTO.fromModel(it) }
    }

    fun updateTimeEntry(id: Long, dto: TimeEntryDTO): TimeEntryResponseDTO? {
        return timeTrackingService.updateTimeEntry(id, dto)?.let { TimeEntryResponseDTO.fromModel(it) }
    }


    fun getTimeEntriesForUser(userId: Long): List<TimeEntryResponseDTO> {
        return timeEntryRepository.find("user.id", userId).list<TimeEntryModel>().map {
            TimeEntryResponseDTO(it)
        }
    }

    fun getTimeEntriesForProject(projectId: Long): List<TimeEntryResponseDTO> {
        return timeEntryRepository.find("project.id", projectId).list<TimeEntryModel>().map {
            TimeEntryResponseDTO(it)
        }
    }

    fun getTimeEntriesByDateRange(start: LocalDate, end: LocalDate): List<TimeEntryResponseDTO> {
        return timeEntryRepository.find("date >= ?1 AND date <= ?2", start, end)
            .list<TimeEntryModel>().map {
                TimeEntryResponseDTO(it)
            }
    }

    @Transactional
    fun deleteTimeEntry(id: Long): Boolean {
        return timeEntryRepository.deleteById(id)
    }
}
