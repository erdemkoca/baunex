package ch.baunex.invoice.model

enum class InvoiceStatus {
    DRAFT,      // Rechnungsentwurf
    ISSUED,    // Rechnung erstellt
    PAID,       // Rechnung bezahlt
    CANCELLED   // Rechnung storniert
} 