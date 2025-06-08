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

    lateinit var customerName: String

    var markdownHeader: String? = null
    var markdownFooter: String? = null
    var createdAt: LocalDateTime = LocalDateTime.now()
    var customerAddress: String? = null

    var invoiceNumber: String? = null
    @Serializable(with = LocalDateSerializer::class) var invoiceDate: LocalDate? = null
    @Serializable(with = LocalDateSerializer::class) var dueDate: LocalDate? = null
    @Enumerated(EnumType.STRING)
    var invoiceStatus: InvoiceStatus? = null
    var notes: String? = null
    var vatRate: Double? = null
    var projectId: Long? = null
    var projectName: String? = null

    @OneToMany(mappedBy = "document", cascade = [CascadeType.ALL], orphanRemoval = true)
    var entries: MutableList<DocumentEntryModel> = mutableListOf()
}
