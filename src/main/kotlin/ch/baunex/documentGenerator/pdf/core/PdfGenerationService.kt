package ch.baunex.documentGenerator.pdf.core

import ch.baunex.documentGenerator.model.DocumentModel
import ch.baunex.documentGenerator.pdf.builder.PdfBuilder
import ch.baunex.documentGenerator.pdf.builder.PdfBuilderFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger

@ApplicationScoped
class PdfGenerationService @Inject constructor(
    private val factory: PdfBuilderFactory
) {
    private val logger = Logger.getLogger(PdfGenerationService::class.java)

    /**
     * Erzeugt ein PDF‐Bytearray je nach DocumentModel‐Untertyp.
     */
    fun generatePdf(doc: DocumentModel): ByteArray {
        val builder = factory.getBuilder(doc.type)
        @Suppress("UNCHECKED_CAST")
        return (builder as PdfBuilder<DocumentModel>).render(doc)
    }

    /**
     * Generiert ein PDF aus purem HTML via den Core‐Converter.
     */
    fun generateFromHtml(html: String): ByteArray {
        logger.info("Generiere PDF aus HTML (Length=${html.length})")
        return HtmlToPdfConverter.convert(html)
    }
}
