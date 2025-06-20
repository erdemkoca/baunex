package ch.baunex.controlreport.mapper

import ch.baunex.controlreport.dto.*
import ch.baunex.controlreport.model.ControlReportModel
import ch.baunex.controlreport.model.DefectPositionModel
import ch.baunex.notes.mapper.toDto
import ch.baunex.notes.mapper.toDtoList
import ch.baunex.project.model.ProjectType
import ch.baunex.user.model.CustomerType
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
            type       = m.project.customer.customerType,
            firstName       = m.project.customer.person.firstName,
            lastName = m.project.customer.person.lastName,
            street     = m.project.customer.person.details.street,
            postalCode = m.project.customer.person.details.zipCode,
            city       = m.project.customer.person.details.city
        ),
        contractor = ContractorDto(
            type       = m.contractorType,
            company    = m.contractorCompany.orEmpty(),
            street     = m.contractorStreet.orEmpty(),
            postalCode = m.contractorPostalCode.orEmpty(),
            city       = m.contractorCity.orEmpty()
        ),
        installationLocation = InstallationLocationDto(
            street       = m.project.street,
            postalCode   = m.project.zipCode,
            city         = m.project.city,
            buildingType = m.project.buildingType,
            parcelNumber = m.project.parcelNumber
        ),
        controlScope = m.controlScope,
        controlData = ControlDataDto(
            controlDate         = m.controlDate,
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
        m.project.customer.customerType      = dto.client.type
        m.project.customer.person.firstName      = dto.client.firstName
        m.project.customer.person.lastName = dto.client.lastName
        m.project.customer.person.details.street    = dto.client.street
        m.project.customer.person.details.zipCode = dto.client.postalCode
        m.project.customer.person.details.city      = dto.client.city

        // contractor
        contractorType      = dto.contractor.type
        contractorCompany   = dto.contractor.company
        contractorStreet    = dto.contractor.street
        contractorPostalCode= dto.contractor.postalCode
        contractorCity      = dto.contractor.city

        // installation
        m.project.street     = dto.installationLocation.street
        m.project.zipCode = dto.installationLocation.postalCode
        m.project.city       = dto.installationLocation.city
        m.project.buildingType           = dto.installationLocation.buildingType
        m.project.parcelNumber           = dto.installationLocation.parcelNumber

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
        description     = p.description,
        buildingLocation = p.buildingLocation,
        noteId          = p.note.id,
        noteContent     = p.note.content,
        photoUrls = p.note.attachments.toDtoList(),
        normReferences = p.normReferences?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    )
}
