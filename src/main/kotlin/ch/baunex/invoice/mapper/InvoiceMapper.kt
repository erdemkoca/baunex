package ch.baunex.invoice.mapper

import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.dto.InvoiceItemDTO
import ch.baunex.invoice.model.InvoiceModel
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
            status = ch.baunex.invoice.model.InvoiceStatus.valueOf(dto.status)
            notes = dto.notes
            totalAmount = dto.totalAmount
            vatAmount = dto.vatAmount
            grandTotal = dto.grandTotal
        }
    }

    fun toDTO(model: InvoiceModel): InvoiceDTO {
        return InvoiceDTO(
            id = model.id,
            invoiceNumber = model.invoiceNumber,
            invoiceDate = model.invoiceDate,
            dueDate = model.dueDate,
            customerId = model.customerId,
            customerName = model.customerId?.let { getCustomerName(it) } ?: "",
            customerAddress = model.customerId?.let { getCustomerAddress(it) } ?: "",
            projectId = model.projectId,
            projectName = model.projectId?.let { getProjectName(it) } ?: "",
            projectDescription = model.projectId?.let { getProjectDescription(it) },
            status = model.status.name,
            totalAmount = model.totalAmount,
            vatAmount = model.vatAmount,
            grandTotal = model.grandTotal,
            notes = model.notes,
            items = model.items.map { toItemDTO(it) }
        )
    }

    private fun toItemDTO(model: InvoiceModel.InvoiceItemModel): InvoiceItemDTO {
        return InvoiceItemDTO(
            id = model.id,
            type = model.type,
            description = model.description,
            quantity = model.quantity,
            unitPrice = model.unitPrice,
            vatRate = model.vatRate,
            totalAmount = model.totalAmount,
            vatAmount = model.vatAmount,
            grandTotal = model.grandTotal,
            order = model.itemOrder,
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