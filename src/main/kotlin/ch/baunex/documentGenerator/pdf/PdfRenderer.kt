package ch.baunex.documentGenerator.pdf

import ch.baunex.documentGenerator.model.DocumentModel
import ch.baunex.documentGenerator.model.DocumentEntryModel
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.util.data.MutableDataSet
import java.time.format.DateTimeFormatter
import org.jboss.logging.Logger

/**
 * Rendert ein DocumentModel als PDF-Bytearray.
 * Die Klasse erzeugt validen XHTML-Content und nutzt OpenHTMLToPDF.
 */
object PdfRenderer {
    private val logger = Logger.getLogger(PdfRenderer::class.java)

    // Flexmark-Optionen für Markdown → HTML
    private val flexmarkOptions = MutableDataSet().apply {
        set(HtmlRenderer.SOFT_BREAK, "<br />")
        set(HtmlRenderer.ESCAPE_HTML, false)
        set(HtmlRenderer.HARD_BREAK, "<br />")
    }
    private val parser = Parser.builder(flexmarkOptions).build()
    private val renderer = HtmlRenderer.builder(flexmarkOptions).build()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    /**
     * Hauptmethode: baut HTML und konvertiert zu PDF.
     */
    fun render(doc: DocumentModel): ByteArray {
        try {
            logger.info("Rendere HTML fuer Dokument ${doc.id}")
            val html = buildHtml(doc)
            logger.debug("Generiertes HTML fuer Dokument ${doc.id}:\n$html")
            logger.info("Konvertiere HTML zu PDF fuer Dokument ${doc.id}")
            return HtmlToPdfConverter.convert(html)
        } catch (e: Exception) {
            logger.error("Fehler beim Rendern der PDF fuer Dokument ${doc.id}: ${e.message}", e)
            throw RuntimeException("Fehler beim Rendern der PDF: ${e.message}", e)
        }
    }

    /**
     * Baut validen XHTML-String aus Header, Tabelle und Footer
     */
    private fun buildHtml(doc: DocumentModel): String {
        val serviceItems = doc.entries.filter { it.type == "VA" }
        val materialItems = doc.entries.filter { it.type == "IC" }

        // Summen berechnen
        val serviceTotal = serviceItems.sumOf { (it.quantity ?: 0.0) * (it.price ?: 0.0) }
        val materialTotal = materialItems.sumOf { (it.quantity ?: 0.0) * (it.price ?: 0.0) }
        val totalNet = doc.totalNetto.takeIf { it > 0.0 } ?: (serviceTotal + materialTotal)
        val vatAmount = doc.vatAmount.takeIf { it > 0.0 } ?: (totalNet * (doc.vatRate ?: 0.0) / 100.0)
        val totalGross = doc.totalBrutto.takeIf { it > 0.0 } ?: (totalNet + vatAmount)

        // Header- und Footer-Markdown rendern
        val headerHtml = renderer.render(parser.parse(doc.markdownHeader ?: ""))
        val footerHtml = renderer.render(parser.parse(doc.markdownFooter ?: ""))

        return """
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta charset="UTF-8" />
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .company-info { float: right; text-align: right; }
                    .customer-info, .invoice-details { margin-bottom: 20px; }
                    table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                    th, td { border: 1px solid #ddd; padding: 8px; }
                    th { background: #f8f9fa; }
                    .text-right { text-align: right; }
                    .text-center { text-align: center; margin-top: 30px; }
                    .totals { width: 300px; float: right; }
                    .totals td { border: none; padding: 4px; text-align: right; }
                    .notes, .footer { clear: both; margin-top: 40px; }
                </style>
            </head>
            <body>
                <!-- Header (HTML aus Markdown) -->
                <div class="header">
                    $headerHtml
                </div>

                <!-- Firmenangaben -->
                <div class="company-info">
                    <h2>${doc.companyName.orEmpty()}</h2>
                    <p>${doc.companyAddress.orEmpty()}</p>
                    <p>${doc.companyZip.orEmpty()} ${doc.companyCity.orEmpty()}</p>
                    <p>Tel: ${doc.companyPhone.orEmpty()}</p>
                    <p>Email: ${doc.companyEmail.orEmpty()}</p>
                </div>

                <div style="clear: both;"></div>

                <!-- Kundendaten -->
                <div class="customer-info">
                    <h3>Rechnung an:</h3>
                    <p><strong>${doc.customerName}</strong></p>
                    <p>${doc.customerAddress.orEmpty()}</p>
                    <p>${doc.customerZip.orEmpty()} ${doc.customerCity.orEmpty()}</p>
                </div>

                <!-- Rechnungsdetails -->
                <div class="invoice-details">
                    <p><strong>Rechnungsnummer:</strong> ${doc.invoiceNumber.orEmpty()}</p>
                    <p><strong>Rechnungsdatum:</strong> ${doc.invoiceDate?.format(dateFormatter).orEmpty()}</p>
                    <p><strong>Fälligkeitsdatum:</strong> ${doc.dueDate?.format(dateFormatter).orEmpty()}</p>
                    <p><strong>Status:</strong> ${doc.invoiceStatus?.name.orEmpty()}</p>
                    <p><strong>Projekt:</strong> ${doc.projectName.orEmpty()}</p>
                </div>

                <!-- Leistungen -->
                ${if (serviceItems.isNotEmpty()) """
                <h4>Leistungen</h4>
                <table>
                    <thead>
                        <tr>
                            <th>Bezeichnung</th>
                            <th class="text-right">Menge</th>
                            <th class="text-right">Preis</th>
                            <th class="text-right">Betrag</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${serviceItems.joinToString("\n") { item ->
            val lineTotal = (item.quantity ?: 0.0) * (item.price ?: 0.0)
            """
                            <tr>
                                <td>${item.description.orEmpty()}</td>
                                <td class="text-right">${"%.2f".format(item.quantity ?: 0.0)}</td>
                                <td class="text-right">${"%.2f".format(item.price ?: 0.0)} CHF</td>
                                <td class="text-right">${"%.2f".format(lineTotal)} CHF</td>
                            </tr>
                            """
        }}
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="3" class="text-right"><strong>Total Leistungen:</strong></td>
                            <td class="text-right"><strong>${"%.2f".format(serviceTotal)} CHF</strong></td>
                        </tr>
                    </tfoot>
                </table>
                """ else ""}

                <!-- Material -->
                ${if (materialItems.isNotEmpty()) """
                <h4>Material</h4>
                <table>
                    <thead>
                        <tr>
                            <th>Bezeichnung</th>
                            <th class="text-right">Menge</th>
                            <th class="text-right">Preis</th>
                            <th class="text-right">Betrag</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${materialItems.joinToString("\n") { item ->
            val lineTotal = (item.quantity ?: 0.0) * (item.price ?: 0.0)
            """
                            <tr>
                                <td>${item.description.orEmpty()}</td>
                                <td class="text-right">${"%.2f".format(item.quantity ?: 0.0)}</td>
                                <td class="text-right">${"%.2f".format(item.price ?: 0.0)} CHF</td>
                                <td class="text-right">${"%.2f".format(lineTotal)} CHF</td>
                            </tr>
                            """
        }}
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="3" class="text-right"><strong>Total Material:</strong></td>
                            <td class="text-right"><strong>${"%.2f".format(materialTotal)} CHF</strong></td>
                        </tr>
                    </tfoot>
                </table>
                """ else ""}

                <!-- Gesamtsummen -->
                <div class="totals">
                    <table>
                        <tr><td>Netto:</td><td>${"%.2f".format(totalNet)} CHF</td></tr>
                        <tr><td>MWST (${doc.vatRate ?: 0.0}%):</td><td>${"%.2f".format(vatAmount)} CHF</td></tr>
                        <tr style="background:#f8f9fa;"><td><strong>Total:</strong></td><td><strong>${"%.2f".format(totalGross)} CHF</strong></td></tr>
                    </table>
                </div>

                <!-- Notizen -->
                ${doc.notes?.takeIf { it.isNotBlank() }?.let {
            "<div class=\"notes\"><h4>Notizen</h4><p>$it</p></div>"
        }.orEmpty()}

                <!-- Footer / AGB aus Markdown -->
                <div class="footer">
                    <h4>AGB</h4>
                    ${renderer.render(parser.parse(doc.terms.orEmpty()))}
                    <h4>Zahlungsinformationen</h4>
                    ${renderer.render(parser.parse(doc.footer.orEmpty()))}
                </div>

                <!-- optionaler Footer-HTML -->
                $footerHtml
            </body>
            </html>
        """.trimIndent()
    }
}
