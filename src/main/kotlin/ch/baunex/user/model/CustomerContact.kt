package ch.baunex.user.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.time.LocalDateTime

@Entity
@Table(name = "customer_contacts")
@Serializable
class CustomerContact : PanacheEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    lateinit var customer: CustomerModel

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_person_id", nullable = false)
    lateinit var contactPerson: PersonModel

    @Column(nullable = true)
    var role: String? = null

    @Column(nullable = false)
    var isPrimary: Boolean = false

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