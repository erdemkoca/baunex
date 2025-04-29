package ch.baunex.timetracking.service

import ch.baunex.project.service.ProjectService
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.mapper.toTimeEntryModel
import ch.baunex.timetracking.mapper.toTimeEntryResponseDTO
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.user.service.EmployeeService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class TimeTrackingService {

    @Inject
    lateinit var timeEntryRepository: TimeEntryRepository

    @Inject
    lateinit var employeeService: EmployeeService

    @Inject
    lateinit var projectService: ProjectService

    @Transactional
    fun logTime(dto: TimeEntryDTO): TimeEntryModel {
        val employee = employeeService.findEmployeeById(dto.employeeId)
            ?: throw IllegalArgumentException("Employee not found")
        val project = projectService.getProjectById(dto.projectId)
            ?: throw IllegalArgumentException("Project not found")

        val model = dto.toTimeEntryModel(employee, project)
        timeEntryRepository.persist(model)
        return model
    }

    fun getAllTimeEntries(): List<TimeEntryResponseDTO> =
        timeEntryRepository.listAll()
            .map { it.toTimeEntryResponseDTO() }

    fun getTimeEntryById(id: Long): TimeEntryModel? =
        timeEntryRepository.findById(id)

    @Transactional
    fun updateTimeEntry(id: Long, dto: TimeEntryDTO): TimeEntryModel? {
        val entry = timeEntryRepository.findById(id) ?: return null

        val employee = employeeService.findEmployeeById(dto.employeeId)
            ?: throw IllegalArgumentException("Employee not found")
        val project = projectService.getProjectById(dto.projectId)
            ?: throw IllegalArgumentException("Project not found")

        entry.employee = employee
        entry.project = project
        entry.date = dto.date
        entry.hoursWorked = dto.hoursWorked
        entry.note = dto.note
        entry.hourlyRate = dto.hourlyRate ?: employee.hourlyRate
        entry.billable = dto.billable
        entry.invoiced = dto.invoiced
        entry.catalogItemDescription = dto.catalogItemDescription
        entry.catalogItemPrice = dto.catalogItemPrice

        return entry
    }

    fun getEntriesForEmployee(employeeId: Long): List<TimeEntryModel> =
        timeEntryRepository.list("employee.id", employeeId)

    fun getEntriesForProject(projectId: Long): List<TimeEntryModel> =
        timeEntryRepository.list("project.id", projectId)

    @Transactional
    fun deleteEntry(id: Long): Boolean =
        timeEntryRepository.deleteById(id)
}
