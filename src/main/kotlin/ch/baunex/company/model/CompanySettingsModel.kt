package ch.baunex.company.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "company_settings")
class CompanySettingsModel : PanacheEntity() {

    @Column(name = "planned_weekly_hours", nullable = false)
    var plannedWeeklyHours: Double = 40.0

    @Column(name = "default_workdays_per_week", nullable = false)
    var defaultWorkdaysPerWeek: Int = 5

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDate = LocalDate.now()

    @Column(name = "updated_at")
    var updatedAt: LocalDate? = null

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDate.now()
    }
} 