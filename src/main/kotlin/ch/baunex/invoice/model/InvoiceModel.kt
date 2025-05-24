package ch.baunex.invoice.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "invoices")
class InvoiceModel : PanacheEntity() {
    var invoiceNumber: String? = null
    var invoiceDate: LocalDate? = null
    var dueDate: LocalDate? = null
    var customerId: Long? = null
    var projectId: Long? = null
    var notes: String? = null
    var status: String = "CREATED"
    var totalNetto: Double = 0.0
    var vatAmount: Double = 0.0
    var totalBrutto: Double = 0.0

    @OneToMany(mappedBy = "invoice", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<InvoiceItemModel> = mutableListOf()
}

@Entity
@Table(name = "invoice_items")
class InvoiceItemModel : PanacheEntity() {
    @ManyToOne
    @JoinColumn(name = "invoice_id")
    lateinit var invoice: InvoiceModel

    var description: String? = ""
    var type: String = "" // VA (Verrechnete Arbeit) or IC (In Catalog)
    var quantity: Double = 0.0
    var price: Double = 0.0
    var total: Double = 0.0
    var timeEntryId: Long? = null
    var catalogItemId: Long? = null
} 