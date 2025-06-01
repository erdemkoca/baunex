package ch.baunex.timetracking.model

import ch.baunex.invoice.model.InvoiceModel
import ch.baunex.project.model.ProjectModel
import ch.baunex.user.model.EmployeeModel
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDate
//TODO Change from String to LocalDate

@Entity
@Table(name = "time_entries")
class TimeEntryModel : PanacheEntity() {

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    lateinit var employee: EmployeeModel

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    lateinit var project: ProjectModel

    @OneToMany(mappedBy = "timeEntry", cascade = [CascadeType.ALL], orphanRemoval = true)
    var usedCatalogItems: MutableList<TimeEntryCatalogItemModel> = mutableListOf()

    @Column(nullable = false)
    lateinit var date: LocalDate

    @Column(name = "hours_worked", nullable = false)
    var hoursWorked: Double = 0.0

    @Column(columnDefinition = "TEXT")
    var note: String? = null

    // --- Optional enhancements ---

    @Column(name = "hourly_rate")
    var hourlyRate: Double = 0.0

    @Column(name = "billable", nullable = false)
    var billable: Boolean = true

    @Column(name = "invoiced", nullable = false)
    var invoiced: Boolean = false  // track if this entry has been invoiced

    @Column(name = "catalog_item_description")
    var catalogItemDescription: String? = null  // e.g., "Electrical Socket Replacement"

    @Column(name = "catalog_item_price")
    var catalogItemPrice: Double? = null  // price from catalog per hour or fixed

    // --- Surcharges ---
    @Column(name = "has_night_surcharge", nullable = false)
    var hasNightSurcharge: Boolean = false

    @Column(name = "has_weekend_surcharge", nullable = false)
    var hasWeekendSurcharge: Boolean = false

    @Column(name = "has_holiday_surcharge", nullable = false)
    var hasHolidaySurcharge: Boolean = false

    // --- Additional Costs ---
    @Column(name = "travel_time_minutes")
    var travelTimeMinutes: Int = 0

    @Column(name = "disposal_cost")
    var disposalCost: Double = 0.0

    @Column(name = "has_waiting_time", nullable = false)
    var hasWaitingTime: Boolean = false

    @Column(name = "waiting_time_minutes")
    var waitingTimeMinutes: Int = 0

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    var invoice: InvoiceModel? = null

}
