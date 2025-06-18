package ch.baunex.controlreport.dto

import ch.baunex.controlreport.model.ContractorType
import ch.baunex.serialization.LocalDateSerializer
import ch.baunex.serialization.LocalDateTimeSerializer
import ch.baunex.user.model.CustomerType
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime


data class ControlReportUpdateDto(
    // technische Metadaten
    val reportNumber: String? = null,
    val pageCount: Int,
    val currentPage: Int,

    // Auftraggeber
    val clientType: CustomerType? = null,
    val clientName: String? = null,
    val clientStreet: String? = null,
    val clientHouseNumber: String? = null,
    val clientPostalCode: String? = null,
    val clientCity: String? = null,

    // Auftragnehmer
    val contractorType: ContractorType? = null,
    val contractorCompany: String? = null,
    val contractorStreet: String? = null,
    val contractorHouseNumber: String? = null,
    val contractorPostalCode: String? = null,
    val contractorCity: String? = null,

    // Installationsort
    val installationStreet: String? = null,
    val installationHouseNumber: String? = null,
    val installationPostalCode: String? = null,
    val installationCity: String? = null,
    val buildingType: String? = null,
    val parcelNumber: String? = null,

    // Kontrolldaten
    @Serializable(with = LocalDateSerializer::class)
    val controlDate: LocalDate? = null,
    val controlScope: String? = null,
    val controllerId: Long?,
    val hasDefects: Boolean,
    val deadlineNote: String? = null,

    // Freie Notizen
    val generalNotes: String? = null,

    // Defekt‐Positionen: hier nur Liste mit Create‐ oder Update‐DTOs (müsste separat definiert werden)
    val defectPositions: List<DefectPositionUpdateDto>? = null,

    // Abschlussinfos
    val defectResolverNote: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val completionDate: LocalDateTime? = null,
    val completionConfirmation: LocalDateTime? = null
)