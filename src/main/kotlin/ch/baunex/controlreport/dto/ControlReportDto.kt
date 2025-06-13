package ch.baunex.controlreport.dto

import java.time.LocalDateTime

data class ControlReportDto(
    val id: Long,
    val reportNumber: String,
    val pageCount: Int,
    val currentPage: Int,
    val client: ClientDto,
    val contractor: ContractorDto,
    val installationLocation: InstallationLocationDto,
    val controlScope: String,
    val controlData: ControlDataDto,
    val generalNotes: String,
    val defectPositions: List<DefectPositionDto>,
    val defectResolverNote: String?,
    val completionConfirmation: CompletionConfirmationDto?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)