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
    @Column(nullable = false)
    var status: ApprovalStatus = ApprovalStatus.PENDING

    @Column(length = 255)
    var reason: String? = null
}
