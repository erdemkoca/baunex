//package ch.baunex.documentGenerator.service
//
//import ch.baunex.documentGenerator.model.DocumentModel
//import ch.baunex.documentGenerator.mapper.DocumentMapper
//import ch.baunex.documentGenerator.pdf.core.PdfGenerationService
//import ch.baunex.documentGenerator.repository.DocumentRepository
//import ch.baunex.invoice.repository.InvoiceRepository
//import ch.baunex.project.repository.ProjectRepository
//import ch.baunex.user.repository.CustomerRepository
//import jakarta.enterprise.context.ApplicationScoped
//import jakarta.inject.Inject
//import jakarta.transaction.Transactional
//import jakarta.ws.rs.NotFoundException
//
//@ApplicationScoped
//class DocumentService {
//
//    @Inject
//    lateinit var documentMapper: DocumentMapper
//
//    @Inject
//    lateinit var documentRepository: DocumentRepository
//
//    @Inject
//    lateinit var invoiceRepository: InvoiceRepository
//
//    @Inject
//    lateinit var customerRepository: CustomerRepository
//
//    @Inject
//    lateinit var projectRepository: ProjectRepository
//
//    @Inject
//    lateinit var pdfGenerator: PdfGenerationService
//
//
//    @Transactional
//    fun saveDocument(doc: DocumentModel): DocumentModel {
//        // First persist the document
//        doc.persist()
//
//        // Then persist each entry with the document reference
//        doc.entries.forEach { entry ->
//            entry.document = doc
//            entry.persist()
//        }
//
//        return doc
//    }
//
//    fun getDocumentById(id: Long): DocumentModel =
//        documentRepository.findByIdWithEntries(id) ?: throw NotFoundException("Document $id not found")
//
//    @Transactional
//    fun updateDocument(existing: DocumentModel, updated: DocumentModel): DocumentModel {
//        existing.customerName = updated.customerName
//        existing.markdownHeader = updated.markdownHeader
//        existing.markdownFooter = updated.markdownFooter
//        existing.type = updated.type
//
//        // Update entries by replacing
//        existing.entries.clear()
//        existing.entries.addAll(updated.entries.map {
//            it.document = existing
//            it
//        })
//
//        return existing
//    }
//
//    @Transactional
//    fun deleteDocument(id: Long) {
//        val doc = getDocumentById(id)
//        doc.delete()
//    }
//
//    @Transactional
//    fun createInvoiceDocument(invoiceId: Long): DocumentModel {
//        val invoice = invoiceRepository.findByIdWithItems(invoiceId)
//            ?: throw NotFoundException("Invoice $invoiceId not found")
//
//        println("Invoice items size: ${invoice.items.size}")
//        println("Invoice items: ${invoice.items}")
//
//        val customer = customerRepository.findById(invoice.customerId)
//        val project = invoice.projectId?.let { projectRepository.findById(it) }
//        val doc = documentMapper.toDocument(invoice, customer, project)
//
//        println("Document entries size: ${doc.entries.size}")
//        println("Document entries: ${doc.entries}")
//
//        return saveDocument(doc)
//    }
//
//    @Transactional
//    fun generatePdfBytes(doc: DocumentModel): ByteArray {
//        try {
//            return pdfGenerator.generatePdf(doc)
//        } catch (e: Exception) {
//            e.printStackTrace() // Für Debugging-Zwecke
//            throw RuntimeException("Fehler beim Generieren der PDF: ${e.message}", e)
//        }
//    }
//
//    fun getAllDocuments(): List<DocumentModel> =
//        documentRepository.listAll()
//
//
//}
