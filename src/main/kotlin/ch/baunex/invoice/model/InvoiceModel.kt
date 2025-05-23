package ch.baunex.invoice.model

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "invoices")
class InvoiceModel : PanacheEntityBase() {
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
    var status: InvoiceStatus = InvoiceStatus.CREATED

    @Column
    var notes: String? = null

    @OneToMany(mappedBy = "invoice", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<InvoiceItemModel> = mutableListOf()

    @Column(nullable = false)
    var totalAmount: Double = 0.0

    @Column(nullable = false)
    var vatAmount: Double = 0.0

    @Column(nullable = false)
    var grandTotal: Double = 0.0

    @Entity
    @Table(name = "invoice_items")
    class InvoiceItemModel : PanacheEntityBase() {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "invoice_id", nullable = false)
        lateinit var invoice: InvoiceModel

        @Column(nullable = false)
        var type: String = ""

        @Column(nullable = false)
        var description: String = ""

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
} 