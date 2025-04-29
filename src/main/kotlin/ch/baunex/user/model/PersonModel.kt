package ch.baunex.user.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.time.LocalDateTime

@Entity
@Table(name = "persons")
@Serializable
class PersonModel : PanacheEntity() {

    @Column(nullable = false)
    lateinit var firstName: String

    @Column(nullable = false)
    lateinit var lastName: String

    @Column(nullable = true)
    var email: String? = null

    @Embedded
    lateinit var details: PersonDetails

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
