package ch.baunex.timetracking.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "holiday_types")
class HolidayTypeModel : PanacheEntity() {

    @Column(nullable = false, unique = true, length = 50)
    lateinit var code: String

    @Column(nullable = false, length = 100)
    lateinit var displayName: String

    @Column(name = "default_expected_hours", nullable = false)
    var defaultExpectedHours: Double = 0.0

    @Column(nullable = false)
    var active: Boolean = true

    @Column(length = 500)
    var description: String? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDate = LocalDate.now()

    @Column(name = "updated_at")
    var updatedAt: LocalDate? = null

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0

    @Column(name = "is_system_type", nullable = false)
    var isSystemType: Boolean = false
} 