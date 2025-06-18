package ch.baunex.controlreport.dto

import ch.baunex.controlreport.model.ContractorType
import ch.baunex.project.model.ProjectType
import ch.baunex.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

data class ControlReportCreateDto(
    var projectId: Long,
    val customerId: Long,
    val reportNumber: String,
    val pageCount: Int = 1,
    val currentPage: Int = 1,

    val contractorType: ContractorType,
    val contractorCompany: String,
    val contractorStreet: String,
    val contractorPostalCode: String,
    val contractorCity: String,

    val installationStreet: String,
    val installationPostalCode: String,
    val installationCity: String,
    val buildingType: ProjectType,
    val parcelNumber: String?,

    val controlDate: LocalDate,
    val controlScope: String,
    val controllerId: Long?,
    val controllerPhone: String,
    val hasDefects: Boolean,
    val deadlineNote: String?,

    val generalNotes: String?,

    val defectPositions: List<DefectPositionCreateDto> = emptyList(),

    val defectResolverNote: String?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val completionDate: LocalDateTime?,
    val companyStamp: String?,
    val completionSignature: String?
)
