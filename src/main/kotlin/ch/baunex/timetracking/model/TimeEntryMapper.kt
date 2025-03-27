package ch.baunex.timetracking.model

import ch.baunex.timetracking.dto.TimeEntryResponseDTO

fun TimeEntryModel.toResponseDTO(): TimeEntryResponseDTO = TimeEntryResponseDTO(
    id = this.id,
    userId = this.user.id!!,
    projectId = this.project.id!!,
    date = this.date,
    hoursWorked = this.hoursWorked,
    notes = this.note
)

