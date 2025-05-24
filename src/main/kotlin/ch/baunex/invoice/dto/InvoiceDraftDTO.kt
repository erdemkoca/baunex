package ch.baunex.invoice.dto

import java.time.LocalDate
import java.text.NumberFormat
import java.util.Locale

data class InvoiceDraftDTO(
    val id: Long? = null,
    val invoiceNumber: String? = null,
    val invoiceDate: LocalDate? = null,
    val dueDate: LocalDate? = null,
    val projectId: Long? = null,
    val projectName: String? = null,
    val customerId: Long? = null,
    val customerName: String? = null,
    val customerAddress: String? = null,
    val serviceStartDate: LocalDate? = null,
    val serviceEndDate: LocalDate? = null,
    val notes: String? = null,
    val status: String? = null,
    val entries: List<InvoiceEntryDTO> = emptyList(),
    val totalNetto: Double = 0.0,
    val vatAmount: Double = 0.0,
    val totalBrutto: Double = 0.0
) {
    val formattedGrandTotal: String
        get() = NumberFormat.getCurrencyInstance(Locale.GERMANY).format(totalBrutto)
}

data class InvoiceEntryDTO(
    val id: Long? = null,
    val description: String? = null,
    val type: String, // VA (Verrechnete Arbeit) or IC (In Catalog)
    val quantity: Double,
    val price: Double,
    val total: Double
) 