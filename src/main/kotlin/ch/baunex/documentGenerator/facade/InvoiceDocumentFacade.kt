package ch.baunex.documentGenerator.facade

import ch.baunex.documentGenerator.dto.invoice.InvoiceDocumentResponseDTO
import ch.baunex.documentGenerator.mapper.InvoiceDocumentMapper
import ch.baunex.documentGenerator.service.InvoiceDocumentService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class InvoiceDocumentFacade @Inject constructor(
    private val service: InvoiceDocumentService,
    private val mapper:  InvoiceDocumentMapper
) {
    /** Create & persist a new Invoice-Document from an existing invoice ID */
    fun createFromInvoice(invoiceId: Long): InvoiceDocumentResponseDTO =
        service.createFromInvoice(invoiceId)
            .let(mapper::toResponseDTO)

    /** Return PDF bytes for download */
    fun generatePdf(invoiceDocId: Long): ByteArray =
        service.generatePdfBytesForInvoice(invoiceDocId)

    /** List only the invoice‐type documents */
    fun listAllInvoices(): List<InvoiceDocumentResponseDTO> =
        service.listInvoices().map(mapper::toResponseDTO)
}

