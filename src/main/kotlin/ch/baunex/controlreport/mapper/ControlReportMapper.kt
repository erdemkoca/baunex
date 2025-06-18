package ch.baunex.controlreport.mapper

import ch.baunex.controlreport.dto.*
import ch.baunex.controlreport.model.ControlReportModel
import ch.baunex.controlreport.model.DefectPositionModel
import ch.baunex.notes.mapper.toDto
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate
import java.time.LocalDateTime

@ApplicationScoped
class ControlReportMapper {

    /** Entity → Read-DTO */
    fun toDto(m: ControlReportModel): ControlReportDto = ControlReportDto(
        id                    = m.id,
        reportNumber          = m.reportNumber.orEmpty(),
        pageCount             = m.pageCount,
        currentPage           = m.currentPage,
        client               = ClientDto(
            type       = m.project.customer.customerType,
            name       = m.customer?.companyName.orEmpty(),
            street     = m.customer?.person?.details?.street.orEmpty(),
            postalCode = m.customer?.person?.details?.zipCode.orEmpty(),
            city       = m.customer?.person?.details?.city.orEmpty()
        ),
        contractor           = ContractorDto(
            type       = m.contractorType.name,
            company    = m.contractorCompany.orEmpty(),
            street     = m.contractorStreet.orEmpty(),
            postalCode = m.contractorPostalCode.orEmpty(),
            city       = m.contractorCity.orEmpty()
        ),
        installationLocation = InstallationLocationDto(
            street       = m.installationStreet.orEmpty(),
            postalCode   = m.installationPostalCode.orEmpty(),
            city         = m.installationCity.orEmpty(),
            buildingType = m.project.buildingType,
            parcelNumber = m.parcelNumber
        ),
        controlScope          = m.controlScope.orEmpty(),
        controlData          = ControlDataDto(
            controlDate         = m.controlDate ?: LocalDate.now(),
            controllerId        = m.employee?.id,
            controllerFirstName = m.employee?.person?.firstName,
            controllerLastName  = m.employee?.person?.lastName,
            phoneNumber         = m.employee?.person?.details?.phone.orEmpty(),
            hasDefects          = m.hasDefects,
            deadlineNote        = m.deadlineNote
        ),
        generalNotes          = m.generalNotes.orEmpty(),
        defectPositions       = m.defectPositions.map { toDefectPositionDto(it) },
        defectResolverNote    = m.defectResolverNote,
        createdAt             = m.createdAt,
        updatedAt             = m.updatedAt,
        controlDate           = m.controlDate ?: LocalDate.now()
    )

    /** Update-DTO → Report (ohne Note/Defect-Logik) */
    fun applyUpdate(m: ControlReportModel, dto: ControlReportUpdateDto): ControlReportModel =
        m.apply {
            updatedAt       = LocalDateTime.now()

            // core fields
            reportNumber    = dto.reportNumber
            pageCount       = dto.pageCount
            currentPage     = dto.currentPage

            // contractor
            dto.contractorType?.let { contractorType = it }
            contractorCompany    = dto.contractorCompany
            contractorStreet     = dto.contractorStreet
            contractorPostalCode = dto.contractorPostalCode
            contractorCity       = dto.contractorCity

            // installation
            installationStreet     = dto.installationStreet
            installationPostalCode = dto.installationPostalCode
            installationCity       = dto.installationCity
            parcelNumber           = dto.parcelNumber

            // control data
            controlDate    = dto.controlDate
            controlScope   = dto.controlScope
            hasDefects     = dto.hasDefects
            deadlineNote   = dto.deadlineNote

            // free text
            generalNotes   = dto.generalNotes

            // completion info
            defectResolverNote = dto.defectResolverNote
            completionDate     = dto.completionDate
        }

    private fun toDefectPositionDto(p: DefectPositionModel) = DefectPositionDto(
        positionNumber = p.positionNumber,
        photoUrl       = p.note.attachments.firstOrNull()?.toDto(),
        description    = p.note.content,
        normReferences = p.normReferences.toList()
    )
}
