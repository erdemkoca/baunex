package ch.baunex.timetracking.model

import ch.baunex.project.model.ProjectModel
import ch.baunex.user.model.UserModel
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDate
//TODO Change from String to LocalDate

@Entity
@Table(name = "time_entries")
class TimeEntryModel : PanacheEntity() {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    lateinit var user: UserModel

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    lateinit var project: ProjectModel

    @Column(nullable = false)
    lateinit var date: String

    @Column(name = "hours_worked", nullable = false)
    var hoursWorked: Double = 0.0

    @Column(columnDefinition = "TEXT")
    var note: String? = null

    // --- Optional enhancements ---

    @Column(name = "hourly_rate")
    var hourlyRate: Double? = null  // useful if user/project rate is different over time

    @Column(name = "billable", nullable = false)
    var billable: Boolean = true  // helps with reporting and invoice generation

    @Column(name = "invoiced", nullable = false)
    var invoiced: Boolean = false  // track if this entry has been invoiced

    @Column(name = "catalog_item_description")
    var catalogItemDescription: String? = null  // e.g., "Electrical Socket Replacement"

    @Column(name = "catalog_item_price")
    var catalogItemPrice: Double? = null  // price from catalog per hour or fixed
}
