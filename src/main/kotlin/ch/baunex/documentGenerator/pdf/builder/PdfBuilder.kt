package ch.baunex.documentGenerator.pdf.builder

import ch.baunex.documentGenerator.model.DocumentModel

interface PdfBuilder<T: DocumentModel> {
    fun render(doc: T): ByteArray
}