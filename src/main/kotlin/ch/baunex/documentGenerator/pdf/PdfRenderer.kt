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
        var html = ""
        try {
            logger.info("Starting PDF generation for document ${doc.id}")
            html = buildHtml(doc)
            logger.info("Generated HTML length: ${html.length}")
            logger.debug("Generated HTML content:\n$html")
            return HtmlToPdfConverter.convert(html)
        } catch (e: Exception) {
            logger.error("=== START PROBLEMATIC HTML ===\n$html\n=== END PROBLEMATIC HTML ===")
            logger.error("Error rendering PDF for document ${doc.id}: ${e.message}", e)
            throw RuntimeException("Error rendering PDF: ${e.message}", e)
        }
    }

    /**
     * Baut validen XHTML-String aus Header, Tabellen und Footer
     */
    private fun buildHtml(doc: DocumentModel): String {
        logger.info("Building HTML for document ${doc.id}")

        // Einträge nach Typ gruppieren
        val serviceItems = doc.entries.filter { it.type == "VA" }
        val materialItems = doc.entries.filter { it.type == "IC" }

        // Summen berechnen
        val serviceTotal = serviceItems.sumOf { (it.quantity ?: 0.0) * (it.price ?: 0.0) }
        val materialTotal = materialItems.sumOf { (it.quantity ?: 0.0) * (it.price ?: 0.0) }
        val totalNet = doc.totalNetto.takeIf { it > 0.0 } ?: (serviceTotal + materialTotal)
        val vatAmount = doc.vatAmount.takeIf { it > 0.0 } ?: (totalNet * (doc.vatRate ?: 0.0) / 100.0)
        val totalGross = doc.totalBrutto.takeIf { it > 0.0 } ?: (totalNet + vatAmount)

        // Logo-Pfad
        val logoPath = doc.companyLogo
            ?.let { if (it.startsWith("/")) it else "/$it" }
            ?.trimStart('/') ?: ""

        // Header/Footer aus Markdown
        val headerHtml = renderer.render(parser.parse(doc.markdownHeader ?: ""))
        val footerHtml = renderer.render(parser.parse(doc.markdownFooter ?: ""))

        // Leistungen-Section
        val serviceSection = if (serviceItems.isNotEmpty()) {
            val rows = serviceItems.joinToString("") { item ->
                val lineTotal = (item.quantity ?: 0.0) * (item.price ?: 0.0)
                "<tr>" +
                        "<td>${item.description.orEmpty()}</td>" +
                        "<td class=\"text-right\">${"%.2f".format(item.quantity ?: 0.0)}</td>" +
                        "<td class=\"text-right\">${"%.2f".format(item.price ?: 0.0)} CHF</td>" +
                        "<td class=\"text-right\">${"%.2f".format(lineTotal)} CHF</td>" +
                        "</tr>"
            }
            """
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
                $rows
              </tbody>
              <tfoot>
                <tr>
                  <td colspan="3" class="text-right"><strong>Total Leistungen:</strong></td>
                  <td class="text-right"><strong>${"%.2f".format(serviceTotal)} CHF</strong></td>
                </tr>
              </tfoot>
            </table>
            """.trimIndent()
        } else ""

        // Material-Section
        val materialSection = if (materialItems.isNotEmpty()) {
            val rows = materialItems.joinToString("") { item ->
                val lineTotal = (item.quantity ?: 0.0) * (item.price ?: 0.0)
                "<tr>" +
                        "<td>${item.description.orEmpty()}</td>" +
                        "<td class=\"text-right\">${"%.2f".format(item.quantity ?: 0.0)}</td>" +
                        "<td class=\"text-right\">${"%.2f".format(item.price ?: 0.0)} CHF</td>" +
                        "<td class=\"text-right\">${"%.2f".format(lineTotal)} CHF</td>" +
                        "</tr>"
            }
            """
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
                $rows
              </tbody>
              <tfoot>
                <tr>
                  <td colspan="3" class="text-right"><strong>Total Material:</strong></td>
                  <td class="text-right"><strong>${"%.2f".format(materialTotal)} CHF</strong></td>
                </tr>
              </tfoot>
            </table>
            """.trimIndent()
        } else ""

        // Gesamtes HTML zusammenbauen
        val html = """
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
              <meta charset="UTF-8" />
              <style>
                body { font-family: sans-serif; margin: 20px; font-size: 11pt; line-height: 1.2; }
                .header { margin-bottom: 30px; }
                .sender-info { float: left; width: 40%; }
                .sender-info p { margin: 0; padding: 0; font-size: 11pt; }
                .logo-container { float: right; width: 40%; text-align: right; margin-bottom: 20px; }
                .logo-container img { max-width: 200px; max-height: 80px; object-fit: contain; }
                .customer-info { clear: both; float: right; width: 40%; margin-top: 20px; }
                .customer-info p { margin: 0; padding: 0; font-size: 11pt; }

                /* Neuer Invoice-Header */
                .invoice-header { clear: both; margin-top: 40px; margin-bottom: 20px; font-size: 11pt; }
                .invoice-header-row { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px; }
                .invoice-title { font-size: 14pt; font-weight: bold; margin: 0; }
                .invoice-header-info p, .invoice-header-left p, .invoice-header-right p { margin: 0; line-height: 1.2; }
                .invoice-header-info { text-align: right; }
                .invoice-header-left { width: 50%; }
                .invoice-header-right { width: 50%; text-align: right; }

                table { width: 100%; border-collapse: collapse; margin-bottom: 20px; font-size: 10pt; }
                th, td { border: 1px solid #ddd; padding: 6px; }
                th { background: #f8f9fa; }
                .text-right { text-align: right; }

                .totals { width: 300px; float: right; font-size: 10pt; }
                .totals td { border: none; padding: 2px; text-align: right; }

                .notes, .footer { clear: both; margin-top: 30px; font-size: 10pt; }
                .footer h4 { font-size: 11pt; margin: 10px 0 5px 0; }
              </style>
            </head>
            <body>
              <!-- Header -->
              <div class="header">
                <div class="sender-info">
                  <p>${doc.companyName.orEmpty()}</p>
                  <p>${doc.companyAddress.orEmpty()}</p>
                  <p>${doc.companyZip.orEmpty()} ${doc.companyCity.orEmpty()}</p>
                  <p>${doc.companyPhone.orEmpty()}</p>
                  <p>${doc.companyEmail.orEmpty()}</p>
                </div>
                <div class="logo-container">
                  ${if (logoPath.isNotEmpty()) "<img src=\"$logoPath\" alt=\"Company Logo\" />" else ""}
                </div>
                <div class="customer-info">
                  <p><strong>Rechnung an:</strong></p>
                  <p>${doc.customerName}</p>
                  <p>${doc.customerAddress.orEmpty()}</p>
                  <p>${doc.customerZip.orEmpty()} ${doc.customerCity.orEmpty()}</p>
                </div>
              </div>

              <!-- Neuer Invoice-Header -->
              <div class="invoice-header">
                <div class="invoice-header-row">
                  <div class="invoice-title">Rechnung ${doc.invoiceNumber.orEmpty()}</div>
                  <div class="invoice-header-info">
                    <p>${doc.companyCity.orEmpty()}, ${doc.invoiceDate?.format(dateFormatter).orEmpty()}</p>
                    <p><strong>Kunden-Nr:</strong> ${doc.customerId.orEmpty()}</p>
                    <p><strong>Fälligkeitsdatum:</strong> ${doc.dueDate?.format(dateFormatter).orEmpty()}</p>
                  </div>
                </div>
                <div class="invoice-header-row">
                  <div class="invoice-header-left">
                    <p>Projekt: ${doc.projectNumber ?: ""}</p>
                    <p>Zeitraum: ${doc.projectStartDate?.format(dateFormatter).orEmpty()} – ${doc.projectEndDate?.format(dateFormatter).orEmpty()}</p>
                  </div>
                </div>
              </div>

              <!-- Leistungen -->
              $serviceSection

              <!-- Material -->
              $materialSection

              <!-- Gesamtsummen -->
              <div class="totals">
                <table>
                  <tr><td>Brutto:</td><td>${"%.2f".format(totalNet)} CHF</td></tr>
                  <tr><td>MwSt (${doc.vatRate ?: 0.0}%):</td><td>${"%.2f".format(vatAmount)} CHF</td></tr>
                  <tr style="background:#f8f9fa;"><td><strong>Netto-Total:</strong></td><td><strong>${"%.2f".format(totalGross)} CHF</strong></td></tr>
                </table>
              </div>

              <!-- Notizen -->
              ${doc.notes?.takeIf { it.isNotBlank() }?.let { "<div class=\"notes\"><h4>Notizen</h4><p>$it</p></div>" } ?: ""}

              <!-- Footer / AGB -->
              <div class="footer">
                <h4>AGB</h4>
                ${renderer.render(parser.parse(doc.terms.orEmpty()))}
                <h4>Zahlungsinformationen</h4>
                ${renderer.render(parser.parse(doc.footer.orEmpty()))}
              </div>

              <!-- Optionaler Footer-HTML -->
              $footerHtml
            </body>
            </html>
        """.trimIndent()

        logger.info("HTML generation completed")
        return html
    }
}
