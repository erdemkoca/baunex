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

    @Enumerated(EnumType.STRING)
    @Column(name = "holiday_type", nullable = false)
    var holidayType: HolidayDefinitionType = HolidayDefinitionType.PUBLIC_HOLIDAY

    @Column(length = 500)
    var description: String? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDate = LocalDate.now()

    @Column(name = "updated_at")
    var updatedAt: LocalDate? = null
}

enum class HolidayDefinitionType(val displayName: String) {
    PUBLIC_HOLIDAY("Öffentlicher Feiertag"),
    CANTONAL_HOLIDAY("Kantonaler Feiertag"),
    COMPANY_HOLIDAY("Betriebsfeiertag"),
    CUSTOM_HOLIDAY("Benutzerdefinierter Feiertag");

    companion object {
        fun fromDisplayNameOrDefault(name: String?): HolidayDefinitionType {
            if (name == null) return PUBLIC_HOLIDAY
            values().firstOrNull { it.displayName == name }?.let { return it }
            return PUBLIC_HOLIDAY
        }
    }
} 