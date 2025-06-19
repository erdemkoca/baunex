package ch.baunex.controlreport.mapper

import ch.baunex.controlreport.dto.*
import ch.baunex.controlreport.model.ControlReportModel
import ch.baunex.controlreport.model.DefectPositionModel
import ch.baunex.notes.mapper.toDto
import ch.baunex.notes.mapper.toDtoList
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime

@ApplicationScoped
class ControlReportMapper {

    fun toDto(m: ControlReportModel): ControlReportDto = ControlReportDto(
        id              = m.id,
        reportNumber    = "CR-${m.id + 1000}",
        pageCount       = m.pageCount,
        currentPage     = m.currentPage,
        client = ClientDto(
            type       = m.clientType,
            name       = m.clientName.orEmpty(),
            street     = m.clientStreet.orEmpty(),
            postalCode = m.clientPostalCode.orEmpty(),
            city       = m.clientCity.orEmpty()
        ),
        contractor = ContractorDto(
            type       = m.contractorType,
            company    = m.contractorCompany.orEmpty(),
            street     = m.contractorStreet.orEmpty(),
            postalCode = m.contractorPostalCode.orEmpty(),
            city       = m.contractorCity.orEmpty()
        ),
        installationLocation = InstallationLocationDto(
            street       = m.installationStreet.orEmpty(),
            postalCode   = m.installationPostalCode.orEmpty(),
            city         = m.installationCity.orEmpty(),
            buildingType = m.buildingType,
            parcelNumber = m.parcelNumber
        ),
        controlScope = m.controlScope,
        controlData = ControlDataDto(
            controlDate         = m.controlDate!!,
            controllerId        = m.employee?.id,
            controllerFirstName = m.employee?.person?.firstName,
            controllerLastName  = m.employee?.person?.lastName,
            phoneNumber         = m.employee?.person?.details?.phone,
            hasDefects          = m.hasDefects,
            deadlineNote        = m.deadlineNote
        ),
        generalNotes        = m.generalNotes,
        defectPositions     = m.defectPositions.map { toDefectPositionDto(it) },
        defectResolverNote  = m.defectResolverNote,
        completionDate      = m.completionDate,
        createdAt           = m.createdAt,
        updatedAt           = m.updatedAt
    )

    fun applyUpdate(m: ControlReportModel, dto: ControlReportDto): ControlReportModel = m.apply {
        updatedAt       = LocalDateTime.now()

        reportNumber    = dto.reportNumber?.removePrefix("CR-")?.toInt()
        pageCount       = dto.pageCount
        currentPage     = dto.currentPage

        // client
        clientType      = dto.client.type
        clientName      = dto.client.name
        clientStreet    = dto.client.street
        clientPostalCode = dto.client.postalCode
        clientCity      = dto.client.city

        // contractor
        contractorType      = dto.contractor.type
        contractorCompany   = dto.contractor.company
        contractorStreet    = dto.contractor.street
        contractorPostalCode= dto.contractor.postalCode
        contractorCity      = dto.contractor.city

        // installation
        installationStreet     = dto.installationLocation.street
        installationPostalCode = dto.installationLocation.postalCode
        installationCity       = dto.installationLocation.city
        buildingType           = dto.installationLocation.buildingType
        parcelNumber           = dto.installationLocation.parcelNumber

        // control
        controlScope   = dto.controlScope
        controlDate    = dto.controlData.controlDate
        hasDefects     = dto.controlData.hasDefects
        deadlineNote   = dto.controlData.deadlineNote

        // misc
        generalNotes        = dto.generalNotes
        defectResolverNote  = dto.defectResolverNote
        completionDate      = dto.completionDate
    }

    private fun toDefectPositionDto(p: DefectPositionModel) = DefectPositionDto(
        id              = p.id,
        positionNumber  = p.positionNumber,
        description     = p.note.content,                      // wird beim Erstellen kopiert
        buildingLocation = p.buildingLocation,                 // eigenes Feld im Model (falls du es dort ergänzt hast)
        noteId          = p.note.id,
        noteContent     = p.note.content,
        photoUrls = p.note.attachments.toDtoList()
    )
}
