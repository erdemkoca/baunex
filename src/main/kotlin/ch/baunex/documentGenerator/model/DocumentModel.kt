package ch.baunex.documentGenerator.model

import ch.baunex.invoice.model.InvoiceStatus
import ch.baunex.serialization.LocalDateSerializer
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "documents")
class DocumentModel : PanacheEntity() {

    @Enumerated(EnumType.STRING)
    lateinit var type: DocumentType

    @Column(length = 1000)
    lateinit var customerName: String

    @Column(columnDefinition = "TEXT")
    var markdownHeader: String? = null

    @Column(columnDefinition = "TEXT")
    var markdownFooter: String? = null

    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(length = 1000)
    var customerAddress: String? = null

    @Column(length = 100)
    var invoiceNumber: String? = null

    @Serializable(with = LocalDateSerializer::class) 
    var invoiceDate: LocalDate? = null

    @Serializable(with = LocalDateSerializer::class) 
    var dueDate: LocalDate? = null

    @Enumerated(EnumType.STRING)
    var invoiceStatus: InvoiceStatus? = null

    @Column(columnDefinition = "TEXT")
    var notes: String? = null

    var vatRate: Double? = null
    var projectId: Long? = null

    @Column(length = 1000)
    var projectName: String? = null

    var projectNumber: Int? = null

    var totalNetto: Double = 0.0
    var vatAmount: Double = 0.0
    var totalBrutto: Double = 0.0

    // Company information
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

    // Customer information
    @Column(length = 100)
    var customerId: String? = null

    @Column(length = 100)
    var customerZip: String? = null

    @Column(length = 100)
    var customerCity: String? = null

    // Project information
    @Serializable(with = LocalDateSerializer::class)
    var projectStartDate: LocalDate? = null

    @Serializable(with = LocalDateSerializer::class)
    var projectEndDate: LocalDate? = null

    // Document information
    @Column(length = 100)
    var documentNumber: String? = null

    @Column(length = 100)
    var documentDate: String? = null

    // Terms and footer
    @Column(columnDefinition = "TEXT")
    var terms: String? = null

    @Column(columnDefinition = "TEXT")
    var footer: String? = null

    @OneToMany(mappedBy = "document", cascade = [CascadeType.ALL], orphanRemoval = true)
    var entries: MutableList<DocumentEntryModel> = mutableListOf()
}
