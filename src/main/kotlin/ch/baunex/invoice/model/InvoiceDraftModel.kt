package ch.baunex.invoice.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "invoice_drafts")
class InvoiceDraftModel : PanacheEntity() {
    var invoiceNumber: String? = null
    var invoiceDate: LocalDate? = null
    var dueDate: LocalDate? = null
    var customerId: Long? = null
    var projectId: Long? = null
    var notes: String? = null
    var status: String = "DRAFT"
    var totalNetto: Double = 0.0
    var vatAmount: Double = 0.0
    var totalBrutto: Double = 0.0

    @OneToMany(mappedBy = "invoiceDraft", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<InvoiceDraftItemModel> = mutableListOf()
}