package ch.baunex.timetracking.service

import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.mapper.TimeEntryMapper
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.user.repository.EmployeeRepository
import ch.baunex.project.repository.ProjectRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class TimeTrackingService @Inject constructor(
    private val timeEntryRepository: TimeEntryRepository,
    private val employeeRepository: EmployeeRepository,
    private val projectRepository: ProjectRepository,
    private val timeEntryMapper: TimeEntryMapper,
    private val timeEntryCatalogItemService: TimeEntryCatalogItemService
) {

    @Transactional
    fun logTime(dto: TimeEntryDTO): TimeEntryModel {
        val employee = employeeRepository.findById(dto.employeeId)
            ?: throw IllegalArgumentException("Employee not found with id: ${dto.employeeId}")
        val project = projectRepository.findById(dto.projectId)
            ?: throw IllegalArgumentException("Project not found with id: ${dto.projectId}")

        val timeEntry = timeEntryMapper.toTimeEntryModel(dto, employee, project)
        timeEntryRepository.persist(timeEntry)
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

        val updatedEntry = timeEntryMapper.toTimeEntryModel(dto, employee, project)
        updatedEntry.id = existingEntry.id
        timeEntryRepository.persist(updatedEntry)
        return updatedEntry
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
}
