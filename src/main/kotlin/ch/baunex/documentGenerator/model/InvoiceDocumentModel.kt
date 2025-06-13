package ch.baunex.documentGenerator.model

import ch.baunex.invoice.model.InvoiceStatus
import ch.baunex.serialization.LocalDateSerializer
import jakarta.persistence.*
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Entity
@Table(name = "invoice_documents")
class InvoiceDocumentModel : DocumentModel() {

    // — Kundendaten —
    @Column(length = 1000, nullable = false)
    lateinit var customerName: String

    @Column(length = 1000)
    var customerAddress: String? = null

    @Column(length = 100)
    var customerId: String? = null

    @Column(length = 100)
    var customerZip: String? = null

    @Column(length = 100)
    var customerCity: String? = null


    // — Rechnungs-Header —
    @Column(length = 100)
    var invoiceNumber: String? = null

    @Serializable(with = LocalDateSerializer::class)
    var invoiceDate: LocalDate? = null

    @Serializable(with = LocalDateSerializer::class)
    var dueDate: LocalDate? = null

    @Enumerated(EnumType.STRING)
    var invoiceStatus: InvoiceStatus? = null

    // Freitext / Notizen zur Rechnung
    @Column(columnDefinition = "TEXT")
    var notes: String? = null


    // — Projekt-Daten (optional) —
    var projectId: Long? = null

    @Column(length = 1000)
    var projectName: String? = null

    var projectNumber: Int? = null

    @Serializable(with = LocalDateSerializer::class)
    var projectStartDate: LocalDate? = null

    @Serializable(with = LocalDateSerializer::class)
    var projectEndDate: LocalDate? = null


    // — Beträge & Steuern —
    var vatRate: Double? = null

    var totalNetto: Double = 0.0

    var vatAmount: Double = 0.0

    var totalBrutto: Double = 0.0


    // — Firmen-Infos —
    @Column(length = 1000)
    var companyName: String? = null

    @Column(length = 1000)
    var companyAddress: String? = null

    @Column(length = 100)
    var companyZip: String? = null

    @Column(length = 100)
    var companyCity: String? = null

    @Column(length = 100)
    var companyPhone: String? = null

    @Column(length = 100)
    var companyEmail: String? = null

    @Column(length = 1000)
    var companyLogo: String? = null


    // — AGB / Fußzeile —
    @Column(columnDefinition = "TEXT")
    var terms: String? = null

    @Column(columnDefinition = "TEXT")
    var footer: String? = null
}

