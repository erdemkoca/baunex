package ch.baunex.timetracking.model

import ch.baunex.project.model.ProjectModel
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import ch.baunex.user.model.UserModel

fun TimeEntryModel.toResponseDTO(): TimeEntryResponseDTO = TimeEntryResponseDTO(
    id = this.id,
    userId = this.user.id!!,
    userEmail = this.user.email,
    projectId = this.project.id!!,
    projectName = this.project.name,
    date = this.date,
    hoursWorked = this.hoursWorked,
    notes = this.note,
    hourlyRate = this.hourlyRate,
    cost = this.hoursWorked * this.hourlyRate,
    billable = this.billable,
    invoiced = this.invoiced,
    catalogItemDescription = this.catalogItemDescription,
    catalogItemPrice = this.catalogItemPrice
)

fun TimeEntryDTO.toModel(user: UserModel, project: ProjectModel): TimeEntryModel {
    return TimeEntryModel().apply {
        this.user = user
        this.project = project
        this.date = this@toModel.date
        this.hoursWorked = this@toModel.hoursWorked
        this.note = this@toModel.note
        this.hourlyRate = user.hourlyRate ?: 0.0
        this.billable = this@toModel.billable
        this.invoiced = this@toModel.invoiced
        this.catalogItemDescription = this@toModel.catalogItemDescription
        this.catalogItemPrice = this@toModel.catalogItemPrice
    }
}

