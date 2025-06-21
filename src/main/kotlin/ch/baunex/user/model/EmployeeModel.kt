package ch.baunex.user.model

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "employees")
@Serializable
class EmployeeModel : PanacheEntityBase() {

    @Id
    @Column(name = "person_id")
    var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "person_id")
    lateinit var person: PersonModel

    @Column(nullable = false, unique = true)
    lateinit var email: String

    @Column(nullable = false)
    lateinit var passwordHash: String

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    lateinit var role: Role

    @Column(nullable = false, unique = true)
    lateinit var ahvNumber: String

    @Column(nullable = true)
    var bankIban: String? = null

    @Column(nullable = false)
    var hourlyRate: Double = 150.0

    @Column(nullable = false)
    var plannedWeeklyHours: Double = 42.5

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