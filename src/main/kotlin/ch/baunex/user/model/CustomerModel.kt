package ch.baunex.user.model

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.time.LocalDateTime

@Entity
@Table(name = "customers")
@Serializable
class CustomerModel : PanacheEntityBase() {

    @Id
    @Column(name = "person_id")
    var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "person_id")
    lateinit var person: PersonModel

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var customerType: CustomerType = CustomerType.OWNER

    @OneToMany(
        mappedBy = "customer",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var contacts: MutableList<CustomerContact> = mutableListOf()

    @Column(nullable = false, unique = true)
    var customerNumber: Int = 0

    @Column(nullable = true)
    var companyName: String? = null

    @Column(nullable = true)
    var paymentTerms: String? = null

    @Contextual
    @Column(nullable = true)
    var creditLimit: Double? = null

    @Column(nullable = true)
    var industry: String? = null

    @Column(nullable = true)
    var discountRate: Double? = null

    @Column(nullable = true)
    var preferredLanguage: String? = null

    @Column(nullable = false)
    var marketingConsent: Boolean = false

    @Column(nullable = true)
    var taxId: String? = null

    @Contextual
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Contextual
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @PrePersist
    fun onCreate() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}