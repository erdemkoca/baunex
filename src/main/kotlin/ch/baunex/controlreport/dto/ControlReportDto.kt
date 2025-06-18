package ch.baunex.controlreport.dto

import ch.baunex.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
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
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)