package ch.baunex.timetracking.mapper

import ch.baunex.project.model.ProjectModel
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.user.model.EmployeeModel

fun TimeEntryModel.toTimeEntryResponseDTO(): TimeEntryResponseDTO = TimeEntryResponseDTO(
    id = this.id,
    employeeId = this.employee.id!!,
    employeeEmail = this.employee.email,
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

fun TimeEntryDTO.toTimeEntryModel(employee: EmployeeModel, project: ProjectModel): TimeEntryModel {
    return TimeEntryModel().apply {
        this.employee = employee
        this.project = project
        this.date = this@toTimeEntryModel.date
        this.hoursWorked = this@toTimeEntryModel.hoursWorked
        this.note = this@toTimeEntryModel.note
        this.hourlyRate = employee.hourlyRate ?: 0.0
        this.billable = this@toTimeEntryModel.billable
        this.invoiced = this@toTimeEntryModel.invoiced
        this.catalogItemDescription = this@toTimeEntryModel.catalogItemDescription
        this.catalogItemPrice = this@toTimeEntryModel.catalogItemPrice
    }
}

