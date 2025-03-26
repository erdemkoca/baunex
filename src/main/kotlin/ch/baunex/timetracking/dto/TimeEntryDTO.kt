package ch.baunex.timetracking.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class TimeEntryDTO(
    val userId: Long,
    val projectId: Long,
    //@Contextual val date: LocalDate,
    //TODO didnt work with LocalDate, for now working with straight String
    val date: String,
    val hoursWorked: Double,
    val note: String? = null
)
