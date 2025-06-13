package ch.baunex.documentGenerator.pdf.builder

import ch.baunex.documentGenerator.model.InvoiceDocumentModel
import ch.baunex.documentGenerator.pdf.invoice.InvoiceHtmlBuilder
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class MeasurementPdfBuilder : PdfBuilder<InvoiceDocumentModel> {
    override fun render(doc: InvoiceDocumentModel): ByteArray =
        InvoiceHtmlBuilder.render(doc)
}