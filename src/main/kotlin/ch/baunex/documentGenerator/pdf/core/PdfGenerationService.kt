package ch.baunex.documentGenerator.pdf.core

import ch.baunex.documentGenerator.model.DocumentModel
import ch.baunex.documentGenerator.model.InvoiceDocumentModel
import ch.baunex.documentGenerator.pdf.invoice.InvoiceHtmlBuilder
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger

@ApplicationScoped
class PdfGenerationService {
    private val logger = Logger.getLogger(PdfGenerationService::class.java)

    /**
     * Erzeugt ein PDF‐Bytearray je nach DocumentModel‐Untertyp.
     */
    fun generatePdf(doc: DocumentModel): ByteArray {
        try {
            logger.info("Generiere PDF für Dokument ${doc.id} vom Typ ${doc.type}")
            return when (doc) {
                is InvoiceDocumentModel -> InvoiceHtmlBuilder.render(doc)
                // hier weitere DocumentModel-Unterklassen abfangen, z.B.
                // is DeliveryNoteDocumentModel -> DeliveryNoteHtmlBuilder.render(doc)
                else -> throw IllegalArgumentException("Unsupported document type: ${doc.type}")
            }
        } catch (e: Exception) {
            logger.error("Fehler beim Generieren der PDF für Dokument ${doc.id}: ${e.message}", e)
            throw RuntimeException("Fehler beim Generieren der PDF: ${e.message}", e)
        }
    }

    /**
     * Generiert ein PDF aus purem HTML via den Core‐Converter.
     */
    fun generateFromHtml(html: String): ByteArray {
        logger.info("Generiere PDF aus HTML (Length=${html.length})")
        return HtmlToPdfConverter.convert(html)
    }
}