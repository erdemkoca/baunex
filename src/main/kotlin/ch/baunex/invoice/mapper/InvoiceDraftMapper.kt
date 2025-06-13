package ch.baunex.invoice.mapper

import ch.baunex.invoice.dto.InvoiceDraftDTO
import ch.baunex.invoice.dto.InvoiceEntryDTO
import ch.baunex.invoice.model.InvoiceDraftModel
import ch.baunex.invoice.model.InvoiceDraftItemModel
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import ch.baunex.user.facade.CustomerFacade
import ch.baunex.project.facade.ProjectFacade

@ApplicationScoped
class InvoiceDraftMapper {

    @Inject
    lateinit var itemMapper: InvoiceDraftItemMapper

    @Inject
    lateinit var customerFacade: CustomerFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    fun toModel(dto: InvoiceDraftDTO): InvoiceDraftModel {
        val draftModel = InvoiceDraftModel().apply {
            id = dto.id
            invoiceNumber = dto.invoiceNumber
            invoiceDate = dto.invoiceDate
            dueDate = dto.dueDate
            customerId = dto.customerId
            projectId = dto.projectId
            notes = dto.notes
            status = dto.status ?: "DRAFT"
            totalNetto = dto.totalNetto
            vatAmount = dto.vatAmount
            totalBrutto = dto.totalBrutto
        }

        // Convert entries to items
        dto.entries.forEach { entry ->
            val item = InvoiceDraftItemModel().apply {
                invoiceDraft = draftModel
                description = entry.description
                type = entry.type
                quantity = entry.quantity
                unitPrice = entry.price
                totalAmount = entry.total
            }
            draftModel.items.add(item)
        }

        return draftModel
    }

    fun toDTO(model: InvoiceDraftModel): InvoiceDraftDTO {
        return InvoiceDraftDTO(
            id = model.id,
            invoiceNumber = model.invoiceNumber,
            invoiceDate = model.invoiceDate,
            dueDate = model.dueDate,
            customerId = model.customerId,
            projectId = model.projectId,
            notes = model.notes,
            status = model.status,
            entries = model.items.map { item ->
                InvoiceEntryDTO(
                    id = item.id,
                    description = item.description,
                    type = item.type,
                    quantity = item.quantity,
                    price = item.unitPrice,
                    total = item.totalAmount
                )
            },
            totalNetto = model.totalNetto,
            vatAmount = model.vatAmount,
            totalBrutto = model.totalBrutto
        )
    }

    private fun getCustomerName(customerId: Long): String {
        return customerFacade.getById(customerId)?.companyName ?: ""
    }

    private fun getProjectName(projectId: Long): String {
        return projectFacade.getProjectWithDetails(projectId)?.name ?: ""
    }
} 