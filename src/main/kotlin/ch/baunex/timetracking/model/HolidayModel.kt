package ch.baunex.timetracking.model

import ch.baunex.user.model.EmployeeModel
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "holidays")
class HolidayModel : PanacheEntity() {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    lateinit var employee: EmployeeModel

    @Column(name = "start_date", nullable = false)
    lateinit var startDate: LocalDate

    @Column(name = "end_date", nullable = false)
    lateinit var endDate: LocalDate

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: HolidayType = HolidayType.PAID_VACATION

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    var approvalStatus: ApprovalStatus = ApprovalStatus.PENDING

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    var approvedBy: EmployeeModel? = null

    @Column(name = "approved_at")
    var approvedAt: LocalDate? = null

    @Column(length = 255)
    var reason: String? = null
}
