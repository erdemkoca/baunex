package ch.baunex.timetracking.model

import ch.baunex.timetracking.dto.TimeEntryResponseDTO

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
    billable = this.billable,
    invoiced = this.invoiced,
    catalogItemDescription = this.catalogItemDescription,
    catalogItemPrice = this.catalogItemPrice
)