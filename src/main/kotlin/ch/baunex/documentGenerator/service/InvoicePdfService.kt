package ch.baunex.documentGenerator.service

import ch.baunex.documentGenerator.model.InvoiceDocumentModel
import ch.baunex.documentGenerator.pdf.core.PdfGenerationService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class InvoicePdfService @Inject constructor(
    private val pdfService: PdfGenerationService
) {
    /** Erzeugt das PDF für genau diesen Invoice‐Document‐Typ */
    fun generatePdf(inv: InvoiceDocumentModel): ByteArray =
        pdfService.generatePdf(inv)
}
