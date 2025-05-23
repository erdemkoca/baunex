package ch.baunex.invoice.mapper

import ch.baunex.invoice.dto.InvoiceDraftItemDTO
import ch.baunex.invoice.model.InvoiceDraftItemModel
import ch.baunex.invoice.model.InvoiceDraftModel
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class InvoiceDraftItemMapper {

    fun toModel(dto: InvoiceDraftItemDTO, invoiceDraft: InvoiceDraftModel): InvoiceDraftItemModel {
        return InvoiceDraftItemModel().apply {
            id = dto.id
            this.invoiceDraft = invoiceDraft
            type = dto.type
            description = dto.description
            quantity = dto.quantity
            unitPrice = dto.unitPrice
            vatRate = dto.vatRate
            totalAmount = dto.totalAmount
            vatAmount = dto.vatAmount
            grandTotal = dto.grandTotal
            itemOrder = dto.order
            timeEntryId = dto.timeEntryId
            catalogItemId = dto.catalogItemId
        }
    }

    fun toDTO(model: InvoiceDraftItemModel): InvoiceDraftItemDTO {
        return InvoiceDraftItemDTO(
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
} 