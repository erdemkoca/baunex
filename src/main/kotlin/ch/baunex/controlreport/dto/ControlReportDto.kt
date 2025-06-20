package ch.baunex.controlreport.dto

import ch.baunex.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ControlReportDto(
    val id: Long,
    val reportNumber: String? = null,
    val pageCount: Int,
    val currentPage: Int,
    val client: ClientDto,
    val contractor: ContractorDto,
    val installationLocation: InstallationLocationDto,
    val controlScope: String? = null,
    val controlData: ControlDataDto,
    val generalNotes: String? = null,
    val defectPositions: List<DefectPositionDto> = emptyList(),
    val defectResolverNote: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val completionDate: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)