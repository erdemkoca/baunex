package ch.baunex.timetracking.dto

import ch.baunex.timetracking.model.TimeEntryModel
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class TimeEntryResponseDTO(
    val id: Long?,
    val userId: Long,
    val projectId: Long,
    val date: String,
    val hoursWorked: Double,
    val notes: String? // ✅ keep this consistent everywhere
) {
    companion object {
        fun fromModel(model: TimeEntryModel): TimeEntryResponseDTO {
            return TimeEntryResponseDTO(
                id = model.id,
                userId = model.user.id!!,
                projectId = model.project.id!!,
                date = model.date,
                hoursWorked = model.hoursWorked,
                notes = model.note
            )
        }
    }

    constructor(model: TimeEntryModel) : this(
        id = model.id,
        userId = model.user.id,
        projectId = model.project.id,
        date = model.date,
        hoursWorked = model.hoursWorked,
        notes = model.note
    )
}
