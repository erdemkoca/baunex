package ch.baunex.invoice.model

import kotlinx.serialization.Serializable

@Serializable
enum class InvoiceStatus {
    DRAFT,      // Rechnungsentwurf
    ISSUED,    // Rechnung erstellt
    PAID,       // Rechnung bezahlt
    CANCELLED   // Rechnung storniert
} 