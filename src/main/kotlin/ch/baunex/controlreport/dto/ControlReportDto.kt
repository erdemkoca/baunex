package ch.baunex.controlreport.dto

import ch.baunex.serialization.LocalDateSerializer
import ch.baunex.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class ControlReportDto(
    val id: Long,
    val reportNumber: String,
    val pageCount: Int,
    val currentPage: Int,

    // client + contractor + installation
    val client: ClientDto,
    val contractor: ContractorDto,
    val installationLocation: InstallationLocationDto,

    // ** add controlDate here **
    @Serializable(with = LocalDateSerializer::class)
    val controlDate: LocalDate,

    val controlScope: String,

    // ** expand ControlDataDto if necessary **
    val controlData: ControlDataDto,

    val generalNotes: String,
    val defectPositions: List<DefectPositionDto>,
    val defectResolverNote: String?,

    // metadata
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)
