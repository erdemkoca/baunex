package ch.baunex.invoice.model

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "invoice_drafts")
class InvoiceDraftModel : PanacheEntityBase() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var invoiceNumber: String = ""

    @Column(nullable = false)
    var invoiceDate: LocalDate = LocalDate.now()

    @Column(nullable = false)
    var dueDate: LocalDate = LocalDate.now().plusDays(30)

    @Column(nullable = false)
    var customerId: Long = 0

    @Column(nullable = false)
    var projectId: Long = 0

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: InvoiceStatus = InvoiceStatus.DRAFT

    @Column
    var notes: String? = null

    @OneToMany(mappedBy = "invoiceDraft", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<InvoiceDraftItemModel> = mutableListOf()

    @Column(nullable = false)
    var totalAmount: Double = 0.0

    @Column(nullable = false)
    var vatAmount: Double = 0.0

    @Column(nullable = false)
    var grandTotal: Double = 0.0
} 