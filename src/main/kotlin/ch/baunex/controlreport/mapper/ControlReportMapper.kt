// ch/baunex/controlreport/mapper/ControlReportMapper.kt
package ch.baunex.controlreport.mapper

import ch.baunex.controlreport.dto.*
import ch.baunex.controlreport.model.*
import ch.baunex.notes.mapper.toDto
import ch.baunex.user.model.CustomerModel
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime
import java.time.LocalDate

@ApplicationScoped
class ControlReportMapper {

    /** Entity → Read-DTO */
    fun toDto(m: ControlReportModel): ControlReportDto = ControlReportDto(
        id                    = m.id,
        reportNumber          = m.reportNumber.orEmpty(),
        pageCount             = m.pageCount,
        currentPage           = m.currentPage,
        client                = ClientDto(
            type       = m.project.customer.customerType,
            name       = m.customer?.companyName.orEmpty(),
            street     = m.customer?.person?.details?.street.orEmpty(),
            postalCode = m.customer?.person?.details?.zipCode.orEmpty(),
            city       = m.customer?.person?.details?.city.orEmpty()
        ),
        contractor            = ContractorDto(
            type       = m.contractorType.name,
            company    = m.contractorCompany.orEmpty(),
            street     = m.contractorStreet.orEmpty(),
            postalCode = m.contractorPostalCode.orEmpty(),
            city       = m.contractorCity.orEmpty()
        ),
        installationLocation  = InstallationLocationDto(
            street     = m.installationStreet.orEmpty(),
            postalCode = m.installationPostalCode.orEmpty(),
            city       = m.installationCity.orEmpty(),
            buildingType = m.project.buildingType,
            parcelNumber = m.parcelNumber
        ),
        controlScope          = m.controlScope.orEmpty(),
        controlData           = ControlDataDto(
            controlDate = m.controlDate ?: LocalDate.now(),
            controllerId = m.employee?.id,
            controllerFirstName = m.employee?.person?.firstName,
            controllerLastName = m.employee?.person?.lastName,
            phoneNumber = m.employee?.person?.details?.phone.orEmpty(),
            hasDefects = m.hasDefects,
            deadlineNote = m.deadlineNote
        ),
        generalNotes          = m.generalNotes.orEmpty(),
        defectPositions       = m.defectPositions.map { toDefectPositionDto(it) },
        defectResolverNote    = m.defectResolverNote,
        completionConfirmation= m.completionDate,
        createdAt             = m.createdAt,
        updatedAt             = m.updatedAt
    )

    /** Create-DTO → neues Entity */
    fun toModel(dto: ControlReportCreateDto): ControlReportModel {
        val m = ControlReportModel().apply {
            reportNumber       = dto.reportNumber
            controlDate        = dto.controlDate
            pageCount          = dto.pageCount
            currentPage        = dto.currentPage
            customer           = CustomerModel().apply { id = dto.customerId }
            contractorType     = dto.contractorType
            contractorCompany  = dto.contractorCompany
            contractorStreet   = dto.contractorStreet
            contractorPostalCode  = dto.contractorPostalCode
            contractorCity        = dto.contractorCity
            installationStreet     = dto.installationStreet
            installationPostalCode  = dto.installationPostalCode
            installationCity        = dto.installationCity
            project.buildingType       = dto.buildingType
            parcelNumber       = dto.parcelNumber
            controlScope       = dto.controlScope
            hasDefects         = dto.hasDefects
            deadlineNote       = dto.deadlineNote
            generalNotes       = dto.generalNotes
        }
        dto.defectPositions.forEach { cp ->
            m.defectPositions.add(
                DefectPositionModel().apply {
                    positionNumber = cp.positionNumber
                    normReferences += cp.normReferences
                    controlReport  = m
                }
            )
        }
        return m
    }

    /** Update-DTO → bestehendes Entity aktualisieren */
    fun applyUpdate(m: ControlReportModel, dto: ControlReportUpdateDto): ControlReportModel {
        return m.apply {
            reportNumber    = dto.reportNumber
            controlDate     = dto.controlDate
            pageCount       = dto.pageCount
            currentPage     = dto.currentPage
            controlScope    = dto.controlScope
            hasDefects      = dto.hasDefects
            deadlineNote    = dto.deadlineNote
            generalNotes    = dto.generalNotes
            updatedAt       = LocalDateTime.now()
        }
    }

    /** DefectPosition → DefectPositionDto */
    private fun toDefectPositionDto(p: DefectPositionModel) = DefectPositionDto(
        positionNumber = p.positionNumber,
        photoUrl       = p.note.attachments.first().toDto(),
        description    = p.note.content,
        normReferences = p.normReferences.toList()
    )
}
