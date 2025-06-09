package ch.baunex.documentGenerator.pdf

import ch.baunex.documentGenerator.model.DocumentModel
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger

@ApplicationScoped
class PdfGeneratorComponent {
    private val logger = Logger.getLogger(PdfGeneratorComponent::class.java)

    fun generatePdf(doc: DocumentModel): ByteArray {
        try {
            logger.info("Generiere PDF für Dokument ${doc.id}")
            return PdfRenderer.render(doc)
        } catch (e: Exception) {
            logger.error("Fehler beim Generieren der PDF für Dokument ${doc.id}: ${e.message}", e)
            throw RuntimeException("Fehler beim Generieren der PDF: ${e.message}", e)
        }
    }
}
