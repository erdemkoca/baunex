package ch.baunex.documentGenerator.pdf

import ch.baunex.documentGenerator.model.DocumentModel
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.html.HtmlRenderer

object PdfRenderer {
    private val parser = Parser.builder().build()
    private val renderer = HtmlRenderer.builder().build()

    fun render(doc: DocumentModel): ByteArray {
        val html = buildHtml(doc)
        return HtmlToPdfConverter.convert(html)
    }

    private fun buildHtml(doc: DocumentModel): String {
        val header = renderer.render(parser.parse(doc.markdownHeader ?: ""))
        val footer = renderer.render(parser.parse(doc.markdownFooter ?: ""))
        val tableRows = doc.entries.joinToString("") {
            "<tr><td>${it.description}</td><td>${it.quantity}</td><td>${it.price}</td></tr>"
        }
        return """
            <html>
            <body>
                $header
                <table border="1" cellpadding="5" cellspacing="0">
                    <tr><th>Beschreibung</th><th>Menge</th><th>Preis</th></tr>
                    $tableRows
                </table>
                $footer
            </body>
            </html>
        """.trimIndent()
    }
}
