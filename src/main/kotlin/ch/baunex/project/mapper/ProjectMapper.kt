package ch.baunex.project.mapper

import ch.baunex.catalog.mapper.toProjectCatalogItemDTO
import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.mapper.toDto
import ch.baunex.notes.model.NoteModel
import ch.baunex.project.dto.*
import ch.baunex.project.model.ProjectModel
import ch.baunex.project.model.ProjectStatus
import ch.baunex.timetracking.mapper.TimeEntryMapper
import ch.baunex.user.mapper.CustomerMapper
import ch.baunex.user.model.CustomerModel
import ch.baunex.user.service.EmployeeService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class ProjectMapper @Inject constructor(
    private val timeEntryMapper: TimeEntryMapper,
    private val customerMapper: CustomerMapper,
    private val employeeService: EmployeeService
) {
    fun toListDTO(model: ProjectModel): ProjectListDTO {
        return ProjectListDTO(
            id = model.id ?: throw IllegalStateException("Project ID cannot be null"),
            name = model.name,
            customerId = model.customer.id ?: throw IllegalStateException("Customer ID cannot be null"),
            customerName = model.customer.companyName ?: "",
            status = model.status.name,
            budget = model.budget,
            startDate = model.startDate,
            endDate = model.endDate,
            projectNumberFormatted = "PR-" + model.projectNumber.toString().padStart(4, '0')
        )
    }

    fun toDetailDTO(model: ProjectModel) = ProjectDetailDTO(
        id           = model.id!!,
        name         = model.name,
        customerId   = model.customer.id!!,
        customerName = model.customer.companyName ?: "",
        budget       = model.budget,
        customer     = customerMapper.toDTO(model.customer),
        startDate    = model.startDate,
        endDate      = model.endDate,
        description  = model.description,
        status       = model.status,
        street       = model.street,
        city         = model.city,
        timeEntries  = model.timeEntries.sortedBy { it.date }.map { timeEntryMapper.toTimeEntryResponseDTO(it) },
        catalogItems = model.usedItems.map { it.toProjectCatalogItemDTO() },
        contacts     = model.customer.contacts.map { customerMapper.toContactDTO(it) },
        projectNumberFormatted = "PR-" + model.projectNumber.toString().padStart(4, '0'),
        notes        = model.notes.map { note ->
            NoteDto(
                id = note.id!!,
                projectId = note.project?.id,
                timeEntryId = note.timeEntry?.id,
                documentId = note.document?.id,
                createdById = note.createdBy.id!!,
                createdByName = "${note.createdBy.person.firstName} ${note.createdBy.person.lastName}",
                createdAt = note.createdAt,
                updatedAt = note.updatedAt,
                title = note.title,
                content = note.content,
                category = note.category,
                tags = note.tags,
                attachments = note.attachments.map { it.toDto() }
            )
        }
    )

    fun toDTO(model: ProjectModel): ProjectDTO {
        return ProjectDTO(
            id = model.id ?: throw IllegalStateException("Project ID cannot be null"),
            name = model.name,
            client = model.customer.companyName ?: "",
            budget = model.budget,
            contact = model.customer.person.firstName + " " + model.customer.person.lastName,
            startDate = model.startDate,
            endDate = model.endDate,
            description = model.description,
            status = model.status,
            street = model.street,
            city = model.city,
            timeEntries = model.timeEntries.sortedBy { it.date }.map { timeEntryMapper.toTimeEntryResponseDTO(it) },
            catalogItems = model.usedItems.map { it.toProjectCatalogItemDTO() },
            customerId = model.customer.id ?: throw IllegalStateException("Customer ID cannot be null"),
            customerName = model.customer.companyName ?: "",
            projectNumber = model.projectNumber,
            projectNumberFormatted = "PR-" + model.projectNumber.toString().padStart(4, '0')
        )
    }

    fun toEntity(dto: ProjectDTO): ProjectModel {
        return ProjectModel().apply {
            id = dto.id
            name = dto.name
            budget = dto.budget
            description = dto.description
            status = ProjectStatus.valueOf(dto.status.name)
            street = dto.street
            city = dto.city
        }
    }

    fun createModel(dto: ProjectCreateDTO, customer: CustomerModel): ProjectModel {
        return ProjectModel().apply {
            name        = dto.name
            this.customer = customer
            budget      = dto.budget
            startDate   = dto.startDate
            endDate     = dto.endDate
            description = dto.description
            status      = dto.status
            street      = dto.street
            city        = dto.city

            val projectEntity = this
            notes = dto.initialNotes.map { noteDto ->
                NoteModel().apply {
                    content    = noteDto.content
                    title      = noteDto.title
                    category   = noteDto.category
                    tags       = noteDto.tags
                    createdAt  = noteDto.createdAt
                    updatedAt  = noteDto.updatedAt
                    createdBy  = employeeService.findEmployeeById(noteDto.createdById)!!
                    project    = projectEntity
                }
            }.toMutableList()
        }
    }

    fun updateModel(model: ProjectModel, dto: ProjectUpdateDTO) {
        dto.name       ?.let { model.name        = it }
        dto.budget     ?.let { model.budget      = it }
        dto.startDate  ?.let { model.startDate   = it }
        dto.endDate    ?.let { model.endDate     = it }
        dto.description?.let { model.description = it }
        dto.status     ?.let { model.status      = it }
        dto.street     ?.let { model.street      = it }
        dto.city       ?.let { model.city        = it }

        if (dto.updatedNotes.isNotEmpty()) {
            model.notes.clear()
            val projectEntity = model
            val newNotes = dto.updatedNotes.map { noteDto ->
                NoteModel().apply {
                    content    = noteDto.content
                    title      = noteDto.title
                    category   = noteDto.category
                    tags       = noteDto.tags
                    createdAt  = noteDto.createdAt
                    updatedAt  = noteDto.updatedAt
                    createdBy  = employeeService.findEmployeeById(noteDto.createdById)!!
                    project    = projectEntity
                }
            }
            model.notes.addAll(newNotes)
        }
    }
}

//fun ProjectCreateDTO.toModel(customer: CustomerModel) = ProjectModel().apply {
//    name        = this@toModel.name
//    this.customer = customer
//    budget      = this@toModel.budget
//    startDate   = this@toModel.startDate
//    endDate     = this@toModel.endDate
//    description = this@toModel.description
//    status      = this@toModel.status
//    street      = this@toModel.street
//    city        = this@toModel.city
//
//    val projectEntity = this
//    notes = this@toModel.initialNotes.map { noteDto ->
//        NoteModel().apply {
//            content    = noteDto.content
//            title      = noteDto.title
//            category   = noteDto.category
//            tags       = noteDto.tags
//            createdAt  = noteDto.createdAt
//            updatedAt  = noteDto.updatedAt
//            createdBy  = employeeService.findEmployeeById(noteDto.createdById)!!
//            project    = projectEntity
//        }
//    }.toMutableList()
//
//}
//
//fun ProjectUpdateDTO.applyTo(model: ProjectModel) {
//    name        ?.let { model.name        = it }
//    budget      ?.let { model.budget      = it }
//    startDate   ?.let { model.startDate   = it }
//    endDate     ?.let { model.endDate     = it }
//    description ?.let { model.description = it }
//    status      ?.let { model.status      = it }
//    street      ?.let { model.street      = it }
//    city        ?.let { model.city        = it }
//    if (updatedNotes.isNotEmpty()) {
//        model.notes.clear()
//        val projectEntity = model
//        val newNotes = updatedNotes.map { noteDto ->
//            NoteModel().apply {
//                content    = noteDto.content
//                title      = noteDto.title
//                category   = noteDto.category
//                tags       = noteDto.tags
//                createdAt  = noteDto.createdAt
//                updatedAt  = noteDto.updatedAt
//                createdBy  = employeeService.findEmployeeById(noteDto.createdById)!!
//                project    = projectEntity
//            }
//        }
//        model.notes.addAll(newNotes)
//    }
//}
