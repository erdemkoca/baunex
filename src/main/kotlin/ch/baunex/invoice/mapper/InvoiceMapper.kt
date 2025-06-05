package ch.baunex.invoice.mapper

import ch.baunex.company.facade.CompanyFacade
import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.dto.InvoiceItemDTO
import ch.baunex.invoice.model.InvoiceModel
import ch.baunex.invoice.model.InvoiceItemModel
import ch.baunex.notes.model.NoteModel
import ch.baunex.notes.dto.NoteDto
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.user.facade.CustomerFacade
import ch.baunex.user.service.EmployeeService
import ch.baunex.notes.mapper.toDto
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class InvoiceMapper @Inject constructor(
    private val customerFacade: CustomerFacade,
    private val projectFacade: ProjectFacade,
    private val companyFacade: CompanyFacade,
    private val employeeService: EmployeeService
) {

    fun toModel(dto: InvoiceDTO): InvoiceModel {
        return InvoiceModel().apply {
            id = dto.id
            invoiceNumber = dto.invoiceNumber
            invoiceDate = dto.invoiceDate
            dueDate = dto.dueDate
            customerId = dto.customerId
            projectId = dto.projectId
            invoiceStatus = dto.invoiceStatus
            val invoiceEntity = this

            this.notes = dto.notes.map { noteDto ->
                NoteModel().apply {
                    this.content = noteDto.content
                    this.title = noteDto.title
                    this.category = noteDto.category
                    this.tags = noteDto.tags
                    this.createdAt = noteDto.createdAt
                    this.createdBy = employeeService.findEmployeeById(noteDto.createdById)!!
                    this.invoice = invoiceEntity
                }
            }.toMutableList()

            totalNetto = dto.totalAmount
            vatAmount = dto.vatAmount
            totalBrutto = dto.grandTotal
        }
    }

    fun toDTO(model: InvoiceModel): InvoiceDTO {
        return InvoiceDTO(
            id = model.id,
            invoiceNumber = model.invoiceNumber ?: "",
            invoiceDate = model.invoiceDate ?: java.time.LocalDate.now(),
            dueDate = model.dueDate ?: java.time.LocalDate.now().plusDays(30),
            customerId = model.customerId ?: 0L,
            customerName = model.customerId?.let { getCustomerName(it) } ?: "",
            customerAddress = model.customerId?.let { getCustomerAddress(it) } ?: "",
            projectId = model.projectId ?: 0L,
            projectName = model.projectId?.let { getProjectName(it) } ?: "",
            projectDescription = model.projectId?.let { getProjectDescription(it) },
            invoiceStatus = model.invoiceStatus,
            totalAmount = model.totalNetto,
            vatAmount = model.vatAmount,
            grandTotal = model.totalBrutto,
            notes = model.notes.map { noteModel ->
                NoteDto(
                    id = noteModel.id!!,
                    projectId = noteModel.project?.id,
                    timeEntryId = noteModel.timeEntry?.id,
                    documentId = noteModel.document?.id,
                    createdById = noteModel.createdBy.id!!,
                    createdByName = "${noteModel.createdBy.person.firstName} ${noteModel.createdBy.person.lastName}",
                    createdAt = noteModel.createdAt,
                    updatedAt = noteModel.updatedAt,
                    title = noteModel.title,
                    content = noteModel.content,
                    category = noteModel.category,
                    tags = noteModel.tags,
                    attachments = noteModel.attachments.map { it.toDto() }
                )
            },
            vatRate = companyFacade.getCompany()?.defaultVatRate ?: 0.0,
            items = model.items.map { toItemDTO(it) }
        )
    }

    fun toItemDTO(model: InvoiceItemModel): InvoiceItemDTO {
        return InvoiceItemDTO(
            id = model.id,
            type = model.type,
            description = model.description,
            quantity = model.quantity,
            unitPrice = model.price,
            vatRate = 0.0,
            totalAmount = model.total,
            vatAmount = 0.0,
            grandTotal = model.total,
            order = 0,
            timeEntryId = model.timeEntryId,
            projectCatalogItemId = model.projectCatalogItemId,
            price = model.price
        )
    }

    fun toItemModel(dto: InvoiceItemDTO): InvoiceItemModel {
        return InvoiceItemModel().apply {
            description = dto.description
            type = dto.type
            quantity = dto.quantity
            price = dto.price // dto.price == unitPrice
            total = dto.quantity * dto.price
            timeEntryId = dto.timeEntryId
            projectCatalogItemId = dto.projectCatalogItemId
        }
    }

    private fun getCustomerName(customerId: Long): String {
        return customerFacade.getById(customerId)?.companyName ?: ""
    }

    private fun getCustomerAddress(customerId: Long): String {
        val customer = customerFacade.getById(customerId)
        return if (customer != null) {
            "${customer.street ?: ""}, ${customer.city ?: ""}"
        } else {
            ""
        }
    }

    private fun getProjectName(projectId: Long): String {
        return projectFacade.getProjectWithDetails(projectId)?.name ?: ""
    }

    private fun getProjectDescription(projectId: Long): String? {
        return projectFacade.getProjectWithDetails(projectId)?.description
    }
}
