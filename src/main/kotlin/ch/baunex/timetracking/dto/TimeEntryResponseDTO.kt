package ch.baunex.timetracking.dto

import ch.baunex.timetracking.model.TimeEntryModel
import kotlinx.serialization.Serializable

@Serializable
data class TimeEntryResponseDTO(
    val id: Long?,
    val userId: Long,
    val userEmail: String,
    val projectId: Long,
    val projectName: String,
    val date: String,
    val hoursWorked: Double,
    val notes: String?
) {
    companion object {
        fun fromModel(model: TimeEntryModel): TimeEntryResponseDTO {
            return TimeEntryResponseDTO(
                id = model.id,
                userId = model.user.id!!,
                userEmail = model.user.email,
                projectId = model.project.id!!,
                projectName = model.project.name,
                date = model.date,
                hoursWorked = model.hoursWorked,
                notes = model.note
            )
        }
    }

    constructor(model: TimeEntryModel) : this(
        id = model.id,
        userId = model.user.id,
        userEmail = model.user.email,
        projectId = model.project.id,
        projectName = model.project.name,
        date = model.date,
        hoursWorked = model.hoursWorked,
        notes = model.note
    )
}
