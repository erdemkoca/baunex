package ch.baunex.documentGenerator.dto.invoice

data class InvoiceLineItemDTO(
    val description: String,
    val quantity: Double,
    val unitPrice: Double
)