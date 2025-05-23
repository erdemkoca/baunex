package ch.baunex.invoice.dto

import java.time.LocalDate

data class InvoiceDraftDTO(
    val id: Long? = null,
    val invoiceNumber: String = "",
    val invoiceDate: LocalDate = LocalDate.now(),
    val dueDate: LocalDate = LocalDate.now().plusDays(30),
    val customerId: Long = 0,
    val projectId: Long = 0,
    val status: String = "DRAFT",
    val notes: String? = null,
    val items: List<InvoiceDraftItemDTO> = emptyList(),
    val totalAmount: Double = 0.0,
    val vatAmount: Double = 0.0,
    val grandTotal: Double = 0.0
) 