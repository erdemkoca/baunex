package ch.baunex.invoice.dto

import ch.baunex.invoice.model.InvoiceStatus
import java.time.LocalDate
import java.text.NumberFormat
import java.util.Locale

data class InvoiceDTO(
    val id: Long? = null,
    val invoiceNumber: String,
    val invoiceDate: LocalDate,
    val dueDate: LocalDate,
    val customerId: Long,
    val customerName: String,
    val customerAddress: String,
    val projectId: Long,
    val projectName: String,
    val projectDescription: String?,
    val status: String,
    val totalAmount: Double,
    val vatAmount: Double,
    val grandTotal: Double,
    val formattedGrandTotal: String = NumberFormat.getCurrencyInstance(Locale.GERMANY).format(grandTotal),
    val notes: String?,
    val items: List<InvoiceItemDTO> = emptyList()
)

data class InvoiceItemDTO(
    val id: Long? = null,
    val type: String,
    val description: String? = null,
    val quantity: Double,
    val unitPrice: Double,
    val vatRate: Double,
    val totalAmount: Double,
    val vatAmount: Double,
    val grandTotal: Double,
    val order: Int = 0,
    val timeEntryId: Long? = null,
    val catalogItemId: Long? = null
) 