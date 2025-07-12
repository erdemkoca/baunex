package ch.baunex.timetracking.dto

import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class HolidayDefinitionDTO(
    val id: Long? = null,
    val year: Int,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val name: String,
    val canton: String? = null,
    val isFixed: Boolean = true,
    val isEditable: Boolean = true,
    val active: Boolean = true,
    val isWorkFree: Boolean = true,
    val holidayType: String,
    val description: String? = null,
    @Serializable(with = LocalDateSerializer::class)
    val createdAt: LocalDate = LocalDate.now(),
    @Serializable(with = LocalDateSerializer::class)
    val updatedAt: LocalDate? = null
)

@Serializable
data class HolidayDefinitionListDTO(
    val holidays: List<HolidayDefinitionDTO>
) 