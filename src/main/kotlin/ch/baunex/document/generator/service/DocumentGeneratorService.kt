package ch.baunex.document.generator.service

import ch.baunex.document.generator.model.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.ByteArrayOutputStream

@ApplicationScoped
class DocumentGeneratorService @Inject constructor(
    private val markdownParser: Parser,
    private val htmlRenderer: HtmlRenderer
) {
    fun generateDocument(template: DocumentTemplate): ByteArray {
        val html = buildHtml(template)
        return convertToPdf(html)
    }

    fun buildHtml(template: DocumentTemplate): String {
        return buildString {
            append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        @page {
                            margin: 2cm;
                        }
                        body { 
                            font-family: Arial, sans-serif;
                            font-size: 12pt;
                            line-height: 1.5;
                        }
                        table { 
                            border-collapse: collapse; 
                            width: 100%;
                            margin: 1em 0;
                        }
                        th, td { 
                            border: 1px solid #ddd; 
                            padding: 8px;
                            text-align: left;
                        }
                        th { 
                            background-color: #f2f2f2;
                            font-weight: bold;
                        }
                        header {
                            margin-bottom: 2em;
                        }
                        footer {
                            margin-top: 2em;
                            border-top: 1px solid #ddd;
                            padding-top: 1em;
                        }
                        .total-section {
                            margin-top: 2em;
                            text-align: right;
                        }
                        .total-section p {
                            margin: 0.5em 0;
                        }
                        .total-section strong {
                            font-size: 1.2em;
                        }
                    </style>
                </head>
                <body>
            """.trimIndent())

            template.sections.sortedBy { it.position }.forEach { section ->
                when (section.type) {
                    is SectionType.Markdown -> append(renderMarkdown(section.content as SectionContent.MarkdownContent))
                    is SectionType.Table -> append(renderTable(section.content as SectionContent.TableContent))
                    is SectionType.Header -> append(renderHeader(section.content as SectionContent.MarkdownContent))
                    is SectionType.Footer -> append(renderFooter(section.content as SectionContent.MarkdownContent))
                    is SectionType.Image -> append(renderImage(section.content as SectionContent.ImageContent))
                }
            }

            append("""
                </body>
                </html>
            """.trimIndent())
        }
    }

    private fun renderMarkdown(content: SectionContent.MarkdownContent): String {
        val node = markdownParser.parse(content.markdown)
        return htmlRenderer.render(node)
    }

    private fun renderTable(content: SectionContent.TableContent): String {
        return buildString {
            append("<table>")
            
            // Headers
            append("<thead><tr>")
            content.headers.forEach { header ->
                append("<th>$header</th>")
            }
            append("</tr></thead>")
            
            // Rows
            append("<tbody>")
            content.rows.forEach { row ->
                append("<tr>")
                content.headers.forEach { header ->
                    append("<td>${row[header] ?: ""}</td>")
                }
                append("</tr>")
            }
            append("</tbody>")
            
            append("</table>")
        }
    }

    private fun renderHeader(content: SectionContent.MarkdownContent): String {
        return "<header>${renderMarkdown(content)}</header>"
    }

    private fun renderFooter(content: SectionContent.MarkdownContent): String {
        return "<footer>${renderMarkdown(content)}</footer>"
    }

    private fun renderImage(content: SectionContent.ImageContent): String {
        return buildString {
            append("<img src=\"${content.url}\"")
            content.alt?.let { append(" alt=\"$it\"") }
            content.width?.let { append(" width=\"$it\"") }
            content.height?.let { append(" height=\"$it\"") }
            append(">")
        }
    }

    private fun convertToPdf(html: String): ByteArray {
        val renderer = ITextRenderer()
        renderer.setDocumentFromString(html)
        renderer.layout()

        val outputStream = ByteArrayOutputStream()
        renderer.createPDF(outputStream, true)
        return outputStream.toByteArray()
    }
} 