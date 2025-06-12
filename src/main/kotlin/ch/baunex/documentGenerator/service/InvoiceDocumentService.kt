package ch.baunex.documentGenerator.service

import ch.baunex.documentGenerator.facade.InvoiceDocumentFacade
import ch.baunex.documentGenerator.mapper.InvoiceDocumentMapper
import ch.baunex.documentGenerator.model.InvoiceDocumentModel
import ch.baunex.documentGenerator.pdf.core.PdfGenerationService
import ch.baunex.invoice.repository.InvoiceRepository
import ch.baunex.project.repository.ProjectRepository
import ch.baunex.user.repository.CustomerRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException

@ApplicationScoped
class InvoiceDocumentService @Inject constructor(
    private val genericService: GenericDocumentService,
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val projectRepository: ProjectRepository,
    private val invoiceMapper: InvoiceDocumentMapper,
    private val pdfGenerationService: PdfGenerationService
) {
    /**
     * Baut ein InvoiceDocumentModel aus der Rechnung und speichert es.
     */
    @Transactional
    fun createFromInvoice(invoiceId: Long): InvoiceDocumentModel {
        val invoice  = invoiceRepository.findByIdWithItems(invoiceId)
            ?: throw NotFoundException("Invoice $invoiceId not found")
        val customer = customerRepository.findById(invoice.customerId)
        val project  = invoice.projectId?.let { projectRepository.findById(it) }

        val docModel = invoiceMapper.toModel(invoice, customer, project)
        // generisch speichern
        return genericService.save(docModel) as InvoiceDocumentModel
    }

    /**
     * Generiert das PDF für eine angelegte InvoiceDocumentModel.
     * Legt dabei bei jedem Aufruf ein neues Dokument an.
     */
    fun generatePdfBytesForInvoice(invoiceId: Long): ByteArray {
        // 1) Dokument neu erzeugen
        val doc = createFromInvoice(invoiceId)
        // 2) direkt rendern
        return pdfGenerationService.generatePdf(doc)
    }

    /**
     * Holt alle InvoiceDocumentModel-Einträge.
     */
    fun listInvoices(): List<InvoiceDocumentModel> =
        genericService.listAll()
            .filterIsInstance<InvoiceDocumentModel>()
}
