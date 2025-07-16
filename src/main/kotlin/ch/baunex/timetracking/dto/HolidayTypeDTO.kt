package ch.baunex.timetracking.dto

import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class HolidayTypeDTO(
    val id: Long? = null,
    val code: String,
    val displayName: String,
    val defaultExpectedHours: Double,
    val active: Boolean = true,
    val description: String? = null,
    val sortOrder: Int = 0,
    val isSystemType: Boolean = false,
    @Serializable(with = LocalDateSerializer::class) val createdAt: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class) val updatedAt: LocalDate? = null
)

@Serializable
data class HolidayTypeCreateDTO(
    val code: String,
    val displayName: String,
    val defaultExpectedHours: Double,
    val description: String? = null,
    val sortOrder: Int = 0
)

@Serializable
data class HolidayTypeUpdateDTO(
    val displayName: String? = null,
    val defaultExpectedHours: Double? = null,
    val active: Boolean? = null,
    val description: String? = null,
    val sortOrder: Int? = null
)

@Serializable
data class HolidayTypeListDTO(
    val holidayTypes: List<HolidayTypeDTO>,
    val totalCount: Long
) 