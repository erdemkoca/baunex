package ch.baunex.invoice.mapper

import ch.baunex.invoice.dto.InvoiceDraftDTO
import ch.baunex.invoice.dto.InvoiceDraftItemDTO
import ch.baunex.invoice.model.InvoiceDraftModel
import ch.baunex.invoice.model.InvoiceDraftItemModel
import ch.baunex.invoice.model.InvoiceStatus
import ch.baunex.invoice.model.InvoiceDraftItemType
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate
import jakarta.inject.Inject

@ApplicationScoped
class InvoiceDraftMapper {

    @Inject
    lateinit var itemMapper: InvoiceDraftItemMapper

    fun toModel(dto: InvoiceDraftDTO): InvoiceDraftModel {
        val model = InvoiceDraftModel().apply {
            id = dto.id
            invoiceNumber = dto.invoiceNumber
            invoiceDate = dto.invoiceDate
            dueDate = dto.dueDate
            customerId = dto.customerId
            projectId = dto.projectId
            status = InvoiceStatus.valueOf(dto.status)
            notes = dto.notes
            totalAmount = dto.totalAmount
            vatAmount = dto.vatAmount
            grandTotal = dto.grandTotal
        }

        // Map items after model is created to avoid circular dependency
        dto.items.forEach { itemDto ->
            model.items.add(itemMapper.toModel(itemDto, model))
        }

        return model
    }

    fun toDTO(model: InvoiceDraftModel): InvoiceDraftDTO {
        return InvoiceDraftDTO(
            id = model.id,
            invoiceNumber = model.invoiceNumber,
            invoiceDate = model.invoiceDate,
            dueDate = model.dueDate,
            customerId = model.customerId,
            projectId = model.projectId,
            status = model.status.name,
            notes = model.notes,
            items = model.items.map { itemMapper.toDTO(it) },
            totalAmount = model.totalAmount,
            vatAmount = model.vatAmount,
            grandTotal = model.grandTotal
        )
    }
} 