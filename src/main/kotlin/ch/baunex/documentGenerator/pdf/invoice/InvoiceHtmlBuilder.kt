package ch.baunex.documentGenerator.pdf.invoice

import ch.baunex.documentGenerator.model.InvoiceDocumentModel
import ch.baunex.documentGenerator.pdf.core.HtmlToPdfConverter
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import java.math.BigDecimal
import java.time.format.DateTimeFormatter
import java.util.Base64
import net.codecrete.qrbill.generator.Address
import net.codecrete.qrbill.generator.Bill
import net.codecrete.qrbill.generator.BillFormat
import net.codecrete.qrbill.generator.GraphicsFormat
import net.codecrete.qrbill.generator.Language
import net.codecrete.qrbill.generator.OutputSize
import net.codecrete.qrbill.generator.QRBill
import org.jboss.logging.Logger

object InvoiceHtmlBuilder {
  private val logger = Logger.getLogger(InvoiceHtmlBuilder::class.java)

  private val flexmarkOptions =
          MutableDataSet().apply {
            set(HtmlRenderer.SOFT_BREAK, "<br/>")
            set(HtmlRenderer.ESCAPE_HTML, false)
            set(HtmlRenderer.HARD_BREAK, "<br/>")
          }
  private val parser = Parser.builder(flexmarkOptions).build()
  private val renderer = HtmlRenderer.builder(flexmarkOptions).build()
  private val dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")

  fun render(doc: InvoiceDocumentModel): ByteArray {
    val html = buildHtml(doc)
    logger.debug("Invoice HTML length=${html.length}")
    val pdf = HtmlToPdfConverter.convert(html)
    return pdf
  }

  fun generateQR(doc: InvoiceDocumentModel): ByteArray {

    val creditor =
            Address().apply {
              name = doc.companyName
              street = doc.companyAddress
              postalCode = doc.companyZip
              town = doc.companyCity
              countryCode = "CH"
            }

    // 2) Debtor (customer)
    val debtor =
            Address().apply {
              name = doc.customerName
              street = doc.customerAddress
              postalCode = doc.customerZip ?: "4053"
              town = doc.customerCity ?: "Basel"
              countryCode = "CH"
            }
    val format =
            BillFormat().apply {
              graphicsFormat = GraphicsFormat.PNG
              outputSize = OutputSize.QR_BILL_ONLY
              language = Language.DE
            }

    // 3) Fill Bill
    val bill =
            Bill().apply {
              this.creditor = creditor
              this.debtor = debtor
              this.amount = BigDecimal.valueOf(doc.totalBrutto)
              this.currency = "CHF"
              this.account = "CH9300762011623852957"
              this.unstructuredMessage = "Rechnung: ${doc.invoiceNumber}"
              this.format = format
            }

    return QRBill.generate(bill)
  }

  private fun buildHtml(doc: InvoiceDocumentModel): String {
    logger.info("Building HTML for invoice ${doc.id}")

    val headerHtml = doc.headerMarkdown?.let { renderer.render(parser.parse(it)) } ?: ""
    val footerHtml = doc.footerMarkdown?.let { renderer.render(parser.parse(it)) } ?: ""

    val logoPath =
            doc.companyLogo?.let { if (it.startsWith("/")) it else "/$it" }?.trimStart('/') ?: ""

    // Generate QR code and convert to base64
    val qrCodeBase64 =
            try {
              val qrBytes = generateQR(doc)
              val base64 = Base64.getEncoder().encodeToString(qrBytes)
              "data:image/svg;base64,$base64"
            } catch (e: Exception) {
              logger.warn("Failed to generate QR code: ${e.message}")
              ""
            }

    // Sender und Empfänger
    val sender =
            """
          <div class="sender">
            <p>${doc.companyName.orEmpty()}</p>
            <p>${doc.companyAddress.orEmpty()}</p>
            <p>${doc.companyZip.orEmpty()} ${doc.companyCity.orEmpty()}</p>
            <p>Tel: ${doc.companyPhone.orEmpty()}</p>
            <p>E-Mail: ${doc.companyEmail.orEmpty()}</p>
          </div>
        """.trimIndent()

    val recipient =
            """
          <div class="recipient">
            <p><strong>Rechnung an:</strong></p>
            <p>${doc.customerName}</p>
            <p>${doc.customerAddress.orEmpty()}</p>
            <p>${doc.customerZip.orEmpty()} ${doc.customerCity.orEmpty()}</p>
          </div>
        """.trimIndent()

    // Titel und Metadata
    val title = "<h2>Rechnung ${doc.invoiceNumber.orEmpty()}</h2>"
    val metadata =
            """
          <div class="metadata">
            <p>${doc.companyCity.orEmpty()}, ${doc.invoiceDate?.format(dateFmt).orEmpty()}</p>
            <p>Kunden-Nr.: ${doc.customerId.orEmpty()}</p>
            <p>Fällig: ${doc.dueDate?.format(dateFmt).orEmpty()}</p>
            <p>Projekt-Nr.: ${doc.projectNumber ?: ""}</p>
            <p>Zeitraum: ${doc.projectStartDate?.format(dateFmt).orEmpty()} – ${doc.projectEndDate?.format(dateFmt).orEmpty()}</p>
          </div>
        """.trimIndent()

    fun buildTable(type: String, title: String): String {
      val items = doc.entries.filter { it.type == type }
      if (items.isEmpty()) return ""
      val rows =
              items.joinToString("") { e ->
                val total = e.total ?: 0.0
                """
                <tr>
                  <td>${e.description.orEmpty()}</td>
                  <td class="text-right">${"%.2f".format(e.quantity ?: 0.0)}</td>
                  <td class="text-right">${"%.2f".format(e.price ?: 0.0)} CHF</td>
                  <td class="text-right">${"%.2f".format(total)} CHF</td>
                </tr>
                """
              }
      val sum = items.sumOf { it.total ?: 0.0 }
      return """
              <h4>$title</h4>
              <table class="items">
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
                    <td colspan="3" class="text-right"><strong>Total $title:</strong></td>
                    <td class="text-right"><strong>${"%.2f".format(sum)} CHF</strong></td>
                  </tr>
                </tfoot>
              </table>
            """.trimIndent()
    }

    val serviceTable = buildTable("VA", "Leistungen")
    val materialTable = buildTable("IC", "Material")

    // Summen-Tabelle (Brutto, MwSt, Netto-Total)
    val net = doc.totalNetto
    val vat = doc.vatAmount
    val gross = doc.totalBrutto
    val totalsTable =
            """
          <table class="totals">
            <tr><td>Brutto:</td><td class="text-right">${"%.2f".format(gross)} CHF</td></tr>
            <tr><td>MwSt (${doc.vatRate ?: 0.0}%):</td><td class="text-right">${"%.2f".format(vat)} CHF</td></tr>
            <tr class="highlight"><td><strong>Netto-Total:</strong></td><td class="text-right"><strong>${"%.2f".format(net)} CHF</strong></td></tr>
          </table>
        """.trimIndent()

    // AGB und Zahlungsinfo
    val termsHtml = doc.terms?.let { renderer.render(parser.parse(it)) } ?: ""
    val payHtml = doc.footer?.let { renderer.render(parser.parse(it)) } ?: ""
    val agbSection =
            if (termsHtml.isNotBlank())
                    """
          <div class="footer-block">
            <h4>AGB</h4>
            $termsHtml
          </div>
        """
            else ""
    val paySection =
            if (payHtml.isNotBlank())
                    """
          <div class="footer-block">
            <h4>Zahlungsinformationen</h4>
            $payHtml
          </div>
        """
            else ""

    val qrCodeHtml =
            if (qrCodeBase64.isNotEmpty())
                    """
        <div class="qr-container">
          <img class="qr-code-slip" src="$qrCodeBase64" alt="QR-Rechnung" />
        </div>
        """
            else ""

    val secondPageHtml =
            if (agbSection.isNotBlank() || paySection.isNotBlank() || qrCodeHtml.isNotBlank())
                    """
        <div class="second-page">
            $agbSection
            <hr class="footer-separator" />
            $paySection
            $qrCodeHtml
        </div>
    """
            else ""

    return """
          <!DOCTYPE html>
          <html xmlns="http://www.w3.org/1999/xhtml">
          <head>
            <meta charset="UTF-8"/>
            <style>
              body {
                font-family: sans-serif;
                margin: 20px;
                font-size: 11pt;
                line-height: 1.1;
              }
              h2 { font-size: 15pt; margin:4px 0; }
              h4 { font-size: 12pt; margin:6px 0 2px; }

              /* Generelle Abstände von Absätzen/P-Tags */
              p {
                margin: 0;
                padding: 0;
                line-height: 1.1;
                margin-bottom: 2px;
              }

              .header { overflow: hidden; margin-bottom: 6px; }
              .logo {
                float: right;
                max-width: 150px;
                max-height: 60px;
              }

              .sender, .recipient {
                width: 48%;
                display: inline-block;
                vertical-align: top;
                margin-bottom: 6px;
              }
              .sender { float: left; }
              .recipient { float: right; text-align: right; }

              .invoice-header { clear: both; overflow: hidden; margin-bottom: 8px; }
              .metadata {
                float: right;
                text-align: right;
                margin-bottom: 6px;
              }

              table.items {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 8px;
                font-size: 10pt;
              }
              table.items th, table.items td {
                border: 1px solid #ccc;
                padding: 4px;
              }
              table.items th { background: #f8f9fa; }

              table.totals {
                float: right;
                margin-top: 6px;
                border-collapse: collapse;
                font-size: 10pt;
              }
              table.totals td {
                padding: 4px 6px;
              }
              table.totals tr.highlight {
                background: #f8f9fa;
              }
              .text-right { text-align: right; }

              .notes { clear: both; margin-top: 10px; font-size: 10pt; }
              .notes h4 { margin-bottom: 4px; }

              .second-page {
                page-break-before: always;
              }
              .footer-block h4 {
                  font-weight: bold;
                  font-size: 14pt;
                  margin-top: 20px;
                  margin-bottom: 10px;
              }
              hr.footer-separator {
                  border: none;
                  height: 2px;
                  background-color: #333;
                  margin: 20px 0;
              }
              .qr-container {
                  margin-top: 20px;
              }
              .qr-code-slip {
                  width: 100%;
                  height: auto;
              }
            </style>
          </head>
          <body>
            <div class="header">
              <img class="logo" src="$logoPath" alt="Logo"/>
              $headerHtml
            </div>
            $sender
            $recipient
            <div class="invoice-header">
              $title
              $metadata
            </div>
            $serviceTable
            $materialTable
            $totalsTable
            $secondPageHtml
          </body>
          </html>
        """.trimIndent()
  }
}
