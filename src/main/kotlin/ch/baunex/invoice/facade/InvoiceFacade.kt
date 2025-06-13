package ch.baunex.invoice.facade

import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.dto.InvoiceNewDraftDTO
import ch.baunex.invoice.mapper.InvoiceMapper
import ch.baunex.invoice.model.InvoiceStatus
import ch.baunex.invoice.service.InvoiceService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

    @ApplicationScoped
class InvoiceFacade {

    @Inject
    lateinit var service: InvoiceService

    @Inject
    lateinit var mapper: InvoiceMapper

    fun getAll(): List<InvoiceDTO> {
        return service.getAll().map { mapper.toDTO(it) }
    }

    fun createDraftInvoice(dto: InvoiceDTO): InvoiceDTO {
        val invoice = service.createDraftInvoice(dto)
        return mapper.toDTO(invoice)
    }

    fun createIssuedInvoice(dto: InvoiceDTO): InvoiceDTO {
        val invoice = service.createIssuedInvoice(dto)
        return mapper.toDTO(invoice)
    }

//    fun createInvoice(dto: InvoiceNewDraftDTO): InvoiceDTO {
//        return createIssuedInvoice(dto)
//    }

    fun getById(id: Long): InvoiceDTO {
        return mapper.toDTO(service.getById(id))
    }

    fun getInvoiceByProjectId(projectId: Long): InvoiceDTO {
        return mapper.toDTO(service.getById(projectId))
    }

    fun markAsPaid(id: Long) {
        service.markAsPaid(id)
    }

    fun cancel(id: Long) {
        service.cancel(id)
    }

    fun generateInvoiceNumber(): String {
        return service.generateInvoiceNumber()
    }

    fun publishInvoice(id: Long) {
        val invoice = service.getById(id)
            ?: throw IllegalArgumentException("Rechnung nicht gefunden")

        if (invoice.invoiceStatus != InvoiceStatus.DRAFT) {
            throw IllegalStateException("Nur Entwürfe können veröffentlicht werden")
        }

        service.updateInvoiceStatus(id, InvoiceStatus.ISSUED)
    }

    fun createNewInvoice(dto: InvoiceNewDraftDTO): InvoiceDTO {
        val invoice = service.createNewInvoice(dto)
        return mapper.toDTO(invoice)
    }
}