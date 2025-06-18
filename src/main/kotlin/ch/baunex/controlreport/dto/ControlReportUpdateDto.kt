package ch.baunex.controlreport.dto

import ch.baunex.controlreport.model.ContractorType
import ch.baunex.serialization.LocalDateSerializer
import ch.baunex.serialization.LocalDateTimeSerializer
import ch.baunex.user.model.CustomerType
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class ControlReportUpdateDto(
    // technische Metadaten
    val reportNumber: String? = null,
    val pageCount: Int,
    val currentPage: Int,

    // Client information
    val clientType: String? = null,
    val clientName: String? = null,
    val clientStreet: String? = null,
    val clientPostalCode: String? = null,
    val clientCity: String? = null,

    // Contractor information
    val contractorType: String? = null,
    val contractorCompany: String? = null,
    val contractorStreet: String? = null,
    val contractorPostalCode: String? = null,
    val contractorCity: String? = null,

    // Installation location
    val installationStreet: String? = null,
    val installationPostalCode: String? = null,
    val installationCity: String? = null,
    val buildingType: String? = null,
    val parcelNumber: String? = null,

    // Control data
    @Serializable(with = LocalDateSerializer::class)
    val controlDate: LocalDate? = null,
    val controlScope: String? = null,
    val controllerId: Long? = null,
    val hasDefects: Boolean = false,
    val deadlineNote: String? = null,

    // Freie Notizen
    val generalNotes: String? = null,

    // Defekt‐Positionen
    val defectPositions: List<DefectPositionUpdateDto>? = null,

    // Abschlussinfos
    val defectResolverNote: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val completionDate: LocalDateTime? = null
)