package ch.baunex.invoice.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import ch.baunex.notes.model.NoteModel
import ch.baunex.serialization.LocalDateSerializer
import jakarta.persistence.*
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Entity
@Table(name = "invoices")
class InvoiceModel : PanacheEntity() {

    var invoiceNumber: String? = null

    @Serializable(with = LocalDateSerializer::class) var invoiceDate: LocalDate? = null
    @Serializable(with = LocalDateSerializer::class) var dueDate: LocalDate? = null

    var customerId: Long? = null
    var projectId: Long? = null

    @Enumerated(EnumType.STRING)
    var invoiceStatus: InvoiceStatus = InvoiceStatus.DRAFT

    var totalNetto: Double = 0.0
    var vatAmount: Double = 0.0
    var totalBrutto: Double = 0.0

    @OneToMany(mappedBy = "invoice", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var items: MutableList<InvoiceItemModel> = mutableListOf()

    @OneToMany(mappedBy = "invoice", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var notes: MutableList<NoteModel> = mutableListOf()
}