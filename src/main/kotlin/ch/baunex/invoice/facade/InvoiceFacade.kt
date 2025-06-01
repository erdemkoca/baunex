package ch.baunex.invoice.facade

import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.dto.InvoiceDraftDTO
import ch.baunex.invoice.mapper.InvoiceMapper
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

    fun createInvoice(dto: InvoiceDraftDTO): InvoiceDTO {
        val invoice = service.createInvoice(dto)
        return mapper.toDTO(invoice)
    }


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
} 