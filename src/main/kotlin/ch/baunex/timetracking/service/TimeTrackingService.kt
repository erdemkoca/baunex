// TimeTrackingService.kt
package ch.baunex.timetracking.service

import ch.baunex.project.model.ProjectModel
import ch.baunex.project.service.ProjectService
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.model.toModel
import ch.baunex.timetracking.model.toResponseDTO
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.user.model.UserModel
import ch.baunex.user.service.UserService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate

@ApplicationScoped
class TimeTrackingService {

    @Inject
    lateinit var timeEntryRepository: TimeEntryRepository

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var projectService: ProjectService


    @Transactional
    fun logTime(dto: TimeEntryDTO): TimeEntryModel {
        val user = userService.getUserById(dto.userId)
            ?: throw IllegalArgumentException("User not found")
        val project = projectService.getProjectById(dto.projectId)
            ?: throw IllegalArgumentException("Project not found")

        val model = dto.toModel(user, project)
        timeEntryRepository.persist(model)
        return model
    }


    fun getAllTimeEntries(): List<TimeEntryResponseDTO> {
        return timeEntryRepository.listAll()
            .map { it.toResponseDTO() }
    }

    fun getTimeEntryById(id: Long): TimeEntryModel? {
        return timeEntryRepository.findById(id)
    }

    @Transactional
    fun updateTimeEntry(id: Long, dto: TimeEntryDTO): TimeEntryModel? {
        val entry = timeEntryRepository.findById(id) ?: return null

        val user = userService.getUserById(dto.userId) ?: throw IllegalArgumentException("User not found")
        val project =
            projectService.getProjectById(dto.projectId) ?: throw IllegalArgumentException("Project not found")

        entry.user = user
        entry.project = project
        entry.date = dto.date
        entry.hoursWorked = dto.hoursWorked
        entry.note = dto.note
        entry.hourlyRate = dto.hourlyRate
        entry.billable = dto.billable
        entry.invoiced = dto.invoiced
        entry.catalogItemDescription = dto.catalogItemDescription
        entry.catalogItemPrice = dto.catalogItemPrice

        return entry
    }


    fun getEntriesForUser(userId: Long): List<TimeEntryModel> {
        return timeEntryRepository.list("user.id", userId)
    }

    fun getEntriesForProject(projectId: Long): List<TimeEntryModel> {
        return timeEntryRepository.list("project.id", projectId)
    }

    @Transactional
    fun deleteEntry(id: Long): Boolean {
        return timeEntryRepository.deleteById(id)
    }
}
