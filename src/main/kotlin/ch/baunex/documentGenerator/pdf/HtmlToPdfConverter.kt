package ch.baunex.documentGenerator.pdf

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import java.io.ByteArrayOutputStream

object HtmlToPdfConverter {
    fun convert(html: String): ByteArray {
        val outputStream = ByteArrayOutputStream()
        PdfRendererBuilder()
            .useFastMode()
            .withHtmlContent(html, null)
            .toStream(outputStream)
            .run()
        return outputStream.toByteArray()
    }
}
