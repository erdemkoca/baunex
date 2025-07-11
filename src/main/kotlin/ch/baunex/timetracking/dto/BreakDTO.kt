package ch.baunex.timetracking.dto

import kotlinx.serialization.Serializable


@Serializable
data class BreakDTO(
    @Serializable(with = ch.baunex.serialization.LocalTimeSerializer::class)
    val start: java.time.LocalTime,
    @Serializable(with = ch.baunex.serialization.LocalTimeSerializer::class)
    val end: java.time.LocalTime
)