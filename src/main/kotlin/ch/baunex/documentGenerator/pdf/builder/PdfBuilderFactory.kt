package ch.baunex.documentGenerator.pdf.builder

import ch.baunex.documentGenerator.model.DocumentModel
import ch.baunex.documentGenerator.model.DocumentType
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class PdfBuilderFactory @Inject constructor(
    private val invoiceBuilder: InvoicePdfBuilder,
    private val measurementBuilder:    MeasurementPdfBuilder
    /* … weitere Builder … */
) {
    fun getBuilder(type: DocumentType): PdfBuilder<out DocumentModel> = when (type) {
        DocumentType.INVOICE     -> invoiceBuilder
        DocumentType.MEASUREMENT -> measurementBuilder
        // ggf. weitere explizit:
        DocumentType.INVOICE_DRAFT,
        DocumentType.CONTROL,
        DocumentType.QUOTE,
        DocumentType.DELIVERY_NOTE -> throw IllegalArgumentException("Kein PdfBuilder für $type definiert")
    }
}