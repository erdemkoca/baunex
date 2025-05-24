package ch.baunex.invoice.model

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.*

@Entity
@Table(name = "invoice_draft_items")
class InvoiceDraftItemModel : PanacheEntityBase() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_draft_id", nullable = false)
    lateinit var invoiceDraft: InvoiceDraftModel

    @Column(nullable = false)
    var type: String = ""

    @Column(nullable = false)
    var description: String? = ""

    @Column(nullable = false)
    var quantity: Double = 0.0

    @Column(nullable = false)
    var unitPrice: Double = 0.0

    @Column(nullable = false)
    var vatRate: Double = 0.0

    @Column(nullable = false)
    var totalAmount: Double = 0.0

    @Column(nullable = false)
    var vatAmount: Double = 0.0

    @Column(nullable = false)
    var grandTotal: Double = 0.0

    @Column(name = "item_order")
    var itemOrder: Int = 0

    @Column
    var timeEntryId: Long? = null

    @Column
    var catalogItemId: Long? = null
} 