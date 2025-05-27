package ch.baunex.document.generator.model.templates

import ch.baunex.document.generator.model.*
import java.time.LocalDateTime
import java.util.UUID

class InvoiceDraftTemplate(
    override val id: UUID = UUID.randomUUID(),
    override val type: DocumentType = DocumentType.INVOICE_DRAFT,
    override val sections: List<DocumentSection>,
    override val metadata: DocumentMetadata,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val updatedAt: LocalDateTime = LocalDateTime.now()
) : DocumentTemplate {
    companion object {
        fun create(
            companyName: String,
            customerName: String,
            invoiceNumber: String,
            date: LocalDateTime,
            dueDate: LocalDateTime,
            items: List<InvoiceItem>,
            subtotal: Double,
            vatRate: Double,
            vatAmount: Double,
            total: Double
        ): InvoiceDraftTemplate {
            val sections = listOf(
                // Header
                HeaderSection(
                    position = 0,
                    content = SectionContent.MarkdownContent(
                        """
                        # Invoice Draft
                        **$companyName**
                        
                        To: $customerName
                        Invoice Number: $invoiceNumber
                        Date: ${date.toLocalDate()}
                        Due Date: ${dueDate.toLocalDate()}
                        """.trimIndent()
                    )
                ),
                
                // Items Table
                TableSection(
                    position = 1,
                    content = SectionContent.TableContent(
                        headers = listOf("Description", "Quantity", "Unit Price", "Total"),
                        rows = items.map { item ->
                            mapOf(
                                "Description" to item.description,
                                "Quantity" to item.quantity,
                                "Unit Price" to item.unitPrice,
                                "Total" to item.total
                            )
                        },
                        schema = InvoiceTableSchema()
                    )
                ),
                
                // Totals
                MarkdownSection(
                    position = 2,
                    content = SectionContent.MarkdownContent(
                        """
                        ---
                        
                        Subtotal: $subtotal CHF
                        VAT (${vatRate}%): $vatAmount CHF
                        **Total: $total CHF**
                        """.trimIndent()
                    )
                )
            )

            return InvoiceDraftTemplate(
                sections = sections,
                metadata = DocumentMetadata(
                    title = "Invoice Draft $invoiceNumber",
                    author = companyName,
                    company = companyName
                )
            )
        }
    }
}

data class InvoiceItem(
    val description: String,
    val quantity: Double,
    val unitPrice: Double,
    val total: Double
)

class InvoiceTableSchema : TableSchema {
    override val columns = listOf(
        ColumnDefinition("Description", ColumnType.TEXT, required = true),
        ColumnDefinition("Quantity", ColumnType.NUMBER, required = true),
        ColumnDefinition("Unit Price", ColumnType.CURRENCY, required = true),
        ColumnDefinition("Total", ColumnType.CURRENCY, required = true)
    )
} 