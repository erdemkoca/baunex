package ch.baunex.invoice.mapper

import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.dto.InvoiceItemDTO
import ch.baunex.invoice.model.InvoiceModel
import ch.baunex.invoice.model.InvoiceItemModel
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.user.facade.CustomerFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class InvoiceMapper @Inject constructor(
    private val customerFacade: CustomerFacade,
    private val projectFacade: ProjectFacade
) {

    fun toModel(dto: InvoiceDTO): InvoiceModel {
        return InvoiceModel().apply {
            id = dto.id
            invoiceNumber = dto.invoiceNumber
            invoiceDate = dto.invoiceDate
            dueDate = dto.dueDate
            customerId = dto.customerId
            projectId = dto.projectId
            status = dto.status
            notes = dto.notes
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
            status = model.status,
            totalAmount = model.totalNetto,
            vatAmount = model.vatAmount,
            grandTotal = model.totalBrutto,
            notes = model.notes,
            items = model.items.map { toItemDTO(it) }
        )
    }

    private fun toItemDTO(model: InvoiceItemModel): InvoiceItemDTO {
        return InvoiceItemDTO(
            id = model.id,
            type = model.type,
            description = model.description,
            quantity = model.quantity,
            unitPrice = model.price,
            vatRate = 0.0, // Default value since it's not in the model
            totalAmount = model.total,
            vatAmount = 0.0, // Default value since it's not in the model
            grandTotal = model.total,
            order = 0, // Default value since it's not in the model
            timeEntryId = model.timeEntryId,
            catalogItemId = model.catalogItemId
        )
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