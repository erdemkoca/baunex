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
            logger.info("Starting PDF generation for document ${doc.id}")
            logger.info("Document details:")
            logger.info("Company: ${doc.companyName}")
            logger.info("Customer: ${doc.customerName}")
            logger.info("Invoice Number: ${doc.invoiceNumber}")
            logger.info("Entries count: ${doc.entries.size}")
            
            val html = buildHtml(doc)
            logger.info("Generated HTML length: ${html.length}")
            logger.debug("Generated HTML content:\n$html")
            
            return HtmlToPdfConverter.convert(html)
        } catch (e: Exception) {
            logger.error("Error rendering PDF for document ${doc.id}: ${e.message}", e)
            logger.error("Stack trace: ${e.stackTraceToString()}")
            throw RuntimeException("Error rendering PDF: ${e.message}", e)
        }
    }

    /**
     * Baut validen XHTML-String aus Header, Tabelle und Footer
     */
    private fun buildHtml(doc: DocumentModel): String {
        try {
            logger.info("Building HTML for document ${doc.id}")
            
            val serviceItems = doc.entries.filter { it.type == "VA" }
            val materialItems = doc.entries.filter { it.type == "IC" }
            
            logger.info("Service items count: ${serviceItems.size}")
            logger.info("Material items count: ${materialItems.size}")

            // Summen berechnen
            val serviceTotal = serviceItems.sumOf { (it.quantity ?: 0.0) * (it.price ?: 0.0) }
            val materialTotal = materialItems.sumOf { (it.quantity ?: 0.0) * (it.price ?: 0.0) }
            val totalNet = doc.totalNetto.takeIf { it > 0.0 } ?: (serviceTotal + materialTotal)
            val vatAmount = doc.vatAmount.takeIf { it > 0.0 } ?: (totalNet * (doc.vatRate ?: 0.0) / 100.0)
            val totalGross = doc.totalBrutto.takeIf { it > 0.0 } ?: (totalNet + vatAmount)

            logger.info("Calculated totals:")
            logger.info("Service total: $serviceTotal")
            logger.info("Material total: $materialTotal")
            logger.info("Total net: $totalNet")
            logger.info("VAT amount: $vatAmount")
            logger.info("Total gross: $totalGross")

            // Ensure logo path is absolute and uses the correct format for classpath resources
            val logoPath = doc.companyLogo?.let {
                if (it.startsWith("/")) it else "/$it"
            }?.let {
                // Remove any leading slashes to make it relative to the base URI
                it.trimStart('/')
            } ?: ""
            logger.info("Logo path: $logoPath")

            // Header- und Footer-Markdown rendern
            val headerHtml = renderer.render(parser.parse(doc.markdownHeader ?: ""))
            val footerHtml = renderer.render(parser.parse(doc.markdownFooter ?: ""))
            
            logger.info("Header HTML length: ${headerHtml.length}")
            logger.info("Footer HTML length: ${footerHtml.length}")

            val html = """
                <!DOCTYPE html>
                <html xmlns="http://www.w3.org/1999/xhtml">
                <head>
                    <meta charset="UTF-8" />
                    <style>
                        body { 
                            font-family: sans-serif; 
                            margin: 20px;
                            font-size: 11pt;
                            line-height: 1.2;
                        }
                        .header {
                            margin-bottom: 30px;
                        }
                        .sender-info {
                            float: left;
                            width: 40%;
                        }
                        .sender-info p {
                            margin: 0;
                            padding: 0;
                            font-size: 11pt;
                        }
                        .logo-container {
                            float: right;
                            width: 40%;
                            text-align: right;
                            margin-bottom: 20px;
                        }
                        .logo-container img {
                            max-width: 200px;
                            max-height: 80px;
                            object-fit: contain;
                        }
                        .customer-info {
                            clear: both;
                            float: right;
                            width: 40%;
                            margin-top: 20px;
                        }
                        .customer-info p {
                            margin: 0;
                            padding: 0;
                            font-size: 11pt;
                        }
                        .invoice-header {
                            clear: both;
                            margin-top: 40px;
                            margin-bottom: 20px;
                        }
                        .invoice-title {
                            font-size: 14pt;
                            font-weight: bold;
                            margin-bottom: 10px;
                        }
                        .invoice-meta {
                            display: flex;
                            justify-content: space-between;
                            margin-bottom: 20px;
                        }
                        .invoice-meta-left, .invoice-meta-right {
                            width: 45%;
                        }
                        .invoice-meta p {
                            margin: 2px 0;
                            font-size: 11pt;
                        }
                        table { 
                            width: 100%; 
                            border-collapse: collapse; 
                            margin-bottom: 20px;
                            font-size: 10pt;
                        }
                        th, td { 
                            border: 1px solid #ddd; 
                            padding: 6px; 
                        }
                        th { 
                            background: #f8f9fa; 
                        }
                        .text-right { 
                            text-align: right; 
                        }
                        .totals { 
                            width: 300px; 
                            float: right;
                            font-size: 10pt;
                        }
                        .totals td { 
                            border: none; 
                            padding: 2px; 
                            text-align: right; 
                        }
                        .notes, .footer { 
                            clear: both; 
                            margin-top: 30px;
                            font-size: 10pt;
                        }
                        .footer h4 {
                            font-size: 11pt;
                            margin: 10px 0 5px 0;
                        }
                    </style>
                </head>
                <body>
                    <!-- Header -->
                    <div class="header">
                        <!-- Sender Info (Left) -->
                        <div class="sender-info">
                            <p>${doc.companyName.orEmpty()}</p>
                            <p>${doc.companyAddress.orEmpty()}</p>
                            <p>${doc.companyZip.orEmpty()} ${doc.companyCity.orEmpty()}</p>
                            <p>${doc.companyPhone.orEmpty()}</p>
                            <p>${doc.companyEmail.orEmpty()}</p>
                        </div>

                        <!-- Logo (Right) -->
                        <div class="logo-container">
                            ${if (logoPath.isNotEmpty()) "<img src=\"$logoPath\" alt=\"Company Logo\" />" else ""}
                        </div>

                        <!-- Customer Info (Right, below logo) -->
                        <div class="customer-info">
                            <p><strong>Rechnung an:</strong></p>
                            <p>${doc.customerName}</p>
                            <p>${doc.customerAddress.orEmpty()}</p>
                            <p>${doc.customerZip.orEmpty()} ${doc.customerCity.orEmpty()}</p>
                        </div>
                    </div>

                    <!-- Invoice Header -->
                    <div class="invoice-header">
                        <div class="invoice-title">Rechnung ${doc.invoiceNumber.orEmpty()}</div>
                        <div class="invoice-meta">
                            <div class="invoice-meta-left">
                                <p><strong>Projekt:</strong> ${doc.projectName.orEmpty()}</p>
                                <p><strong>Kundennummer:</strong> ${doc.customerId.orEmpty()}</p>
                                <p><strong>Zeitraum:</strong> ${doc.projectStartDate?.format(dateFormatter).orEmpty()} - ${doc.projectEndDate?.format(dateFormatter).orEmpty()}</p>
                            </div>
                            <div class="invoice-meta-right">
                                <p>${doc.companyCity.orEmpty()}, ${doc.invoiceDate?.format(dateFormatter).orEmpty()}</p>
                                <p><strong>Fälligkeitsdatum:</strong> ${doc.dueDate?.format(dateFormatter).orEmpty()}</p>                              
                            </div>
                        </div>
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
                            <tr><td>Brutto:</td><td>${"%.2f".format(totalNet)} CHF</td></tr>
                            <tr><td>MwSt (${doc.vatRate ?: 0.0}%):</td><td>${"%.2f".format(vatAmount)} CHF</td></tr>
                            <tr style="background:#f8f9fa;"><td><strong>Netto-Total:</strong></td><td><strong>${"%.2f".format(totalGross)} CHF</strong></td></tr>
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
            
            logger.info("HTML generation completed")
            return html
        } catch (e: Exception) {
            logger.error("Error building HTML for document ${doc.id}: ${e.message}", e)
            logger.error("Stack trace: ${e.stackTraceToString()}")
            throw RuntimeException("Error building HTML: ${e.message}", e)
        }
    }
}
