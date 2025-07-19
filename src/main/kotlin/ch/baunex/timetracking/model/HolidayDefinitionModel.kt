package ch.baunex.timetracking.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "holiday_definitions")
class HolidayDefinitionModel : PanacheEntity() {

    @Column(nullable = false)
    var year: Int = 0

    @Column(name = "holiday_date", nullable = false)
    lateinit var date: LocalDate

    @Column(nullable = false, length = 255)
    lateinit var name: String

    @Column(length = 50)
    var canton: String? = null

    @Column(name = "is_fixed", nullable = false)
    var isFixed: Boolean = true

    @Column(name = "is_editable", nullable = false)
    var isEditable: Boolean = true

    @Column(nullable = false)
    var active: Boolean = true

    @Column(name = "is_work_free", nullable = false)
    var isWorkFree: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holiday_type_id", nullable = false)
    lateinit var holidayType: HolidayTypeModel

    @Column(length = 500)
    var description: String? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDate = LocalDate.now()

    @Column(name = "updated_at")
    var updatedAt: LocalDate? = null
}

 