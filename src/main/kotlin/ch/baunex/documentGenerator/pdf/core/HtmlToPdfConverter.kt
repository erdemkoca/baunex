package ch.baunex.documentGenerator.pdf.core

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import org.jboss.logging.Logger
import java.io.ByteArrayOutputStream
import java.io.File

object HtmlToPdfConverter {
    private val logger = Logger.getLogger(HtmlToPdfConverter::class.java)

    fun convert(html: String): ByteArray {
        logger.info("Starting HTML to PDF conversion")
        logger.info("HTML content length: ${html.length}")
        
        try {
            val outputStream = ByteArrayOutputStream()
            
            // Use a simple file-based URI for now
            val baseUri = File("src/main/resources/META-INF/resources").toURI().toString()
            logger.info("Base URI for resources: $baseUri")

            // Simplified configuration
            PdfRendererBuilder()
                .withHtmlContent(html, baseUri)
                .toStream(outputStream)
                .run()

            val pdfBytes = outputStream.toByteArray()
            logger.info("PDF generation completed. PDF size: ${pdfBytes.size} bytes")
            return pdfBytes
        } catch (e: Exception) {
            logger.error("Error converting HTML to PDF: ${e.message}", e)
            logger.error("Stack trace: ${e.stackTraceToString()}")
            throw RuntimeException("Error converting HTML to PDF: ${e.message}", e)
        }
    }
}

