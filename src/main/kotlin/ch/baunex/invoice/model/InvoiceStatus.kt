package ch.baunex.invoice.model

enum class InvoiceStatus {
    DRAFT,      // Rechnungsentwurf
    CREATED,    // Rechnung erstellt
    CONVERTED,  // Entwurf wurde in Rechnung umgewandelt
    PAID,       // Rechnung bezahlt
    CANCELLED   // Rechnung storniert
} 