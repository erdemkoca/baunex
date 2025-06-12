package ch.baunex.documentGenerator.pdf.invoice

import ch.baunex.documentGenerator.model.InvoiceDocumentModel
import ch.baunex.documentGenerator.pdf.core.HtmlToPdfConverter
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.util.data.MutableDataSet
import java.time.format.DateTimeFormatter
import org.jboss.logging.Logger

/**
 * Rendert ein InvoiceDocumentModel als PDF.
 */
object InvoiceHtmlBuilder {
    private val logger = Logger.getLogger(InvoiceHtmlBuilder::class.java)

    // Markdown → HTML
    private val flexmarkOptions = MutableDataSet().apply {
        set(HtmlRenderer.SOFT_BREAK, "<br/>")
        set(HtmlRenderer.ESCAPE_HTML, false)
        set(HtmlRenderer.HARD_BREAK, "<br/>")
    }
    private val parser   = Parser.builder(flexmarkOptions).build()
    private val renderer = HtmlRenderer.builder(flexmarkOptions).build()
    private val dateFmt  = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    /**
     * Public entry point: baut HTML & konvertiert zu PDF.
     */
    fun render(doc: InvoiceDocumentModel): ByteArray {
        val html = buildHtml(doc)
        logger.debug("Invoice HTML length=${html.length}")
        return HtmlToPdfConverter.convert(html)
    }

    private fun buildHtml(doc: InvoiceDocumentModel): String {
        logger.info("Building HTML for invoice ${doc.id}")

        // 1) Header / Footer
        val headerHtml = doc.headerMarkdown?.let { renderer.render(parser.parse(it)) } ?: ""
        val footerHtml = doc.footerMarkdown?.let { renderer.render(parser.parse(it)) } ?: ""

        // 2) Totals
        val net   = doc.totalNetto
        val vat   = doc.vatAmount
        val gross = doc.totalBrutto

        // 3) Logo path
        val logoPath = doc.companyLogo
            ?.let { if (it.startsWith("/")) it else "/$it" }
            ?.trimStart('/') ?: ""

        // 4) Build the Invoice Header HTML
        val invoiceHeader = """
          <div class="invoice-header">
            <h1>Rechnung ${doc.invoiceNumber.orEmpty()}</h1>
            <p>Datum: ${doc.invoiceDate?.format(dateFmt).orEmpty()}</p>
            <p>Fällig: ${doc.dueDate?.format(dateFmt).orEmpty()}</p>
          </div>
        """.trimIndent()

        // 5) Build line-items table
        val rows = doc.entries.joinToString("\n") { e ->
            val lineTotal = e.total ?: 0.0
            """
            <tr>
              <td>${e.description.orEmpty()}</td>
              <td class="text-right">${"%.2f".format(e.quantity ?: 0.0)}</td>
              <td class="text-right">${"%.2f".format(e.price ?: 0.0)} CHF</td>
              <td class="text-right">${"%.2f".format(lineTotal)} CHF</td>
            </tr>
            """.trimIndent()
        }
        val itemsTable = """
          <table>
            <thead>
              <tr>
                <th>Bezeichnung</th><th class="text-right">Menge</th>
                <th class="text-right">Preis</th><th class="text-right">Betrag</th>
              </tr>
            </thead>
            <tbody>
              $rows
            </tbody>
          </table>
        """.trimIndent()

        // 6) Totals section
        val totalsHtml = """
          <div class="totals">
            <p>Netto: ${"%.2f".format(net)} CHF</p>
            <p>MwSt: ${"%.2f".format(vat)} CHF</p>
            <p><strong>Brutto: ${"%.2f".format(gross)} CHF</strong></p>
          </div>
        """.trimIndent()

        // 7) Customer & Company info
        val sender = """
          <div class="sender">
            <p>${doc.companyName.orEmpty()}</p>
            <p>${doc.companyAddress.orEmpty()}</p>
            <p>${doc.companyZip.orEmpty()} ${doc.companyCity.orEmpty()}</p>
          </div>
        """.trimIndent()
        val recipient = """
          <div class="recipient">
            <p>${doc.customerName}</p>
            <p>${doc.customerAddress.orEmpty()}</p>
          </div>
        """.trimIndent()

        // 8) Assemble full HTML
        return """
          <!DOCTYPE html>
          <html><head><meta charset="UTF-8"/>
            <style>
              body { font-family: sans-serif; margin:20px; }
              .invoice-header, .sender, .recipient, .totals { margin-bottom:20px; }
              table { width:100%; border-collapse:collapse; }
              th, td { border:1px solid #ccc; padding:5px; }
              .text-right { text-align:right; }
            </style>
          </head>
          <body>
            $headerHtml
            $sender
            $recipient
            $invoiceHeader
            $itemsTable
            $totalsHtml
            $footerHtml
          </body>
          </html>
        """.trimIndent()
    }
}
