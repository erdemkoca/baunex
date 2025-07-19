package ch.baunex.timetracking.dto

import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class HolidayTypeDTO(
    val id: Long? = null,
    val code: String,
    val displayName: String,
    val factor: Double = 1.0,
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
    val factor: Double = 1.0,
    val description: String? = null,
    val sortOrder: Int = 0
)

@Serializable
data class HolidayTypeUpdateDTO(
    val displayName: String? = null,
    val factor: Double? = null,
    val description: String? = null,
    val sortOrder: Int? = null,
    val active: Boolean? = null
)

data class HolidayTypeListDTO(
    val holidayTypes: List<HolidayTypeDTO>,
    val totalCount: Long
)

data class DefaultWorkdayHoursDTO(
    val defaultWorkdayHours: Double
) 