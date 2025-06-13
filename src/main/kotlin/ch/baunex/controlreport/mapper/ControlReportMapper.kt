// ch/baunex/controlreport/mapper/ControlReportMapper.kt
package ch.baunex.controlreport.mapper

import ch.baunex.controlreport.dto.*
import ch.baunex.controlreport.model.*
import ch.baunex.user.model.CustomerModel
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime

@ApplicationScoped
class ControlReportMapper {

    /** Entity → Read-DTO */
    fun toDto(m: ControlReportModel): ControlReportDto = ControlReportDto(
        id                    = m.id,
        reportNumber          = m.reportNumber.orEmpty(),
        pageCount             = m.pageCount,
        currentPage           = m.currentPage,
        client                = ClientDto(
            type       = m.clientType,
            name       = m.customer?.companyName.orEmpty(),
            street     = m.customer?.person?.details?.street.orEmpty(),
            postalCode = m.customer?.person?.details?.zipCode.orEmpty(),
            city       = m.customer?.person?.details?.city.orEmpty()
        ),
        contractor            = ContractorDto(
            type       = m.contractorType?.name.orEmpty(),
            company    = m.contractorCompany.orEmpty(),
            street     = m.contractorStreet.orEmpty(),
            postalCode = m.contractorPostalCode.orEmpty(),
            city       = m.contractorCity.orEmpty()
        ),
        installationLocation  = InstallationLocationDto(
            street     = m.installationStreet.orEmpty(),
            postalCode = m.installationPostalCode.orEmpty(),
            city       = m.installationCity.orEmpty(),
            buildingType = m.buildingType,
            parcelNumber = m.parcelNumber
        ),
        controlScope          = m.controlScope.orEmpty(),
        controlData           = ControlDataDto(
            controlDate    = m.controlDate ?: LocalDateTime.now(),
            controllerName = m.controllerName.orEmpty(),
            phoneNumber    = m.controllerPhone.orEmpty(),
            hasDefects     = m.hasDefects,
            deadlineNote   = m.deadlineNote
        ),
        generalNotes          = m.generalNotes.orEmpty(),
        defectPositions       = m.defectPositions.map { toDefectPositionDto(it) },
        defectResolverNote    = m.defectResolverNote,
        completionConfirmation= m.completionDate?.let {
            CompletionConfirmationDto(
                resolvedAt   = it,
                companyStamp = m.companyStamp.orEmpty(),
                signature    = m.completionSignature.orEmpty()
            )
        },
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
            contractorHouseNumber = dto.contractorHouseNumber
            contractorPostalCode  = dto.contractorPostalCode
            contractorCity        = dto.contractorCity
            installationStreet     = dto.installationStreet
            installationHouseNumber = dto.installationHouseNumber
            installationPostalCode  = dto.installationPostalCode
            installationCity        = dto.installationCity
            buildingType       = dto.buildingType
            parcelNumber       = dto.parcelNumber
            controlScope       = dto.controlScope
            controllerName     = dto.controllerName
            controllerPhone    = dto.controllerPhone
            hasDefects         = dto.hasDefects
            deadlineNote       = dto.deadlineNote
            generalNotes       = dto.generalNotes
        }
        dto.defectPositions.forEach { cp ->
            m.defectPositions.add(
                DefectPositionModel().apply {
                    positionNumber = cp.positionNumber
                    photoUrl       = cp.photoUrl
                    description    = cp.description
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
            controllerName  = dto.controllerName
            controllerPhone = dto.controllerPhone
            hasDefects      = dto.hasDefects
            deadlineNote    = dto.deadlineNote
            generalNotes    = dto.generalNotes
            updatedAt       = LocalDateTime.now()
        }
    }

    /** DefectPosition → DefectPositionDto */
    private fun toDefectPositionDto(p: DefectPositionModel) = DefectPositionDto(
        positionNumber = p.positionNumber,
        photoUrl       = p.photoUrl.orEmpty(),
        description    = p.description.orEmpty(),
        normReferences = p.normReferences.toList(),
        resolutionConfirmation = p.resolutionSignature?.let {
            ResolutionConfirmationDto(
                resolvedAt = p.resolvedAt ?: LocalDateTime.now(),
                stamp      = p.resolutionStamp.orEmpty(),
                signature  = p.resolutionSignature.orEmpty()
            )
        }
    )
}
