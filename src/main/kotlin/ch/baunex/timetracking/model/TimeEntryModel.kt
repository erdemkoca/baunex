package ch.baunex.timetracking.model

import ch.baunex.invoice.model.InvoiceModel
import ch.baunex.notes.model.NoteModel
import ch.baunex.project.model.ProjectModel
import ch.baunex.user.model.EmployeeModel
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime

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

    @Column(nullable = false)
    lateinit var startTime: LocalTime

    @Column(nullable = false)
    lateinit var endTime: LocalTime

    @Column(name = "hours_worked", nullable = false)
    var hoursWorked: Double = 0.0

    @Column(name = "title", length = 255)
    var title: String = ""

    @OneToMany(mappedBy = "timeEntry", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var notes: MutableList<NoteModel> = mutableListOf()

    @Column(name = "hourly_rate")
    var hourlyRate: Double = 0.0

    @Column(name = "billable", nullable = false)
    var billable: Boolean = true

    @Column(name = "invoiced", nullable = false)
    var invoiced: Boolean = false  // track if this entry has been invoiced

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

    @Column(name = "waiting_time_minutes")
    var waitingTimeMinutes: Int = 0

    // --- Breaks ---
    @Column(name = "breaks", columnDefinition = "TEXT")
    var breaks: String = "[]"  // JSON string of breaks

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    var invoice: InvoiceModel? = null

    @Column(name = "approved", nullable = false)
    var approvalStatus: ApprovalStatus = ApprovalStatus.PENDING

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    var approvedBy: EmployeeModel? = null

    @Column(name = "approved_at")
    var approvedAt: LocalDate? = null
}
