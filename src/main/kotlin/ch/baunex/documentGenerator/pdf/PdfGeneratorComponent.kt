package ch.baunex.documentGenerator.pdf

import ch.baunex.documentGenerator.model.DocumentModel
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PdfGeneratorComponent {
    fun generatePdf(doc: DocumentModel): ByteArray =
        PdfRenderer.render(doc)
}
