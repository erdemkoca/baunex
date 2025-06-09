package ch.baunex.documentGenerator.pdf

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import org.jboss.logging.Logger
import java.io.ByteArrayOutputStream

object HtmlToPdfConverter {
    private val logger = Logger.getLogger(HtmlToPdfConverter::class.java)

    fun convert(html: String): ByteArray {
        try {
            val outputStream = ByteArrayOutputStream()
            val builder = PdfRendererBuilder()
            
            builder.withHtmlContent(html, null)
            builder.toStream(outputStream)
            builder.run()

            return outputStream.toByteArray()
        } catch (e: Exception) {
            logger.error("Fehler beim Konvertieren von HTML zu PDF: ${e.message}", e)
            throw RuntimeException("Fehler beim Konvertieren von HTML zu PDF: ${e.message}", e)
        }
    }
}

