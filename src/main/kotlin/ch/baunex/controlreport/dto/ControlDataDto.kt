package ch.baunex.controlreport.dto

import java.time.LocalDateTime

data class ControlDataDto(
    val controlDate: LocalDateTime,
    val controllerName: String,
    val phoneNumber: String,
    val hasDefects: Boolean,
    val deadlineNote: String?
)