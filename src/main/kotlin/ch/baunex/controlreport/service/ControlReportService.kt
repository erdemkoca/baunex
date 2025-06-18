package ch.baunex.controlreport.service

import ch.baunex.company.repository.CompanyRepository
import ch.baunex.controlreport.dto.*
import ch.baunex.controlreport.mapper.ControlReportMapper
import ch.baunex.controlreport.model.ContractorType
import ch.baunex.controlreport.model.ControlReportModel
import ch.baunex.controlreport.model.DefectPositionModel
import ch.baunex.controlreport.repository.ControlReportRepository
import ch.baunex.project.repository.ProjectRepository
import ch.baunex.user.model.CustomerType
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException
import java.time.LocalDate

@ApplicationScoped
class ControlReportService(
    private val mapper: ControlReportMapper
) {
    @Inject lateinit var repository: ControlReportRepository
    @Inject lateinit var projectRepository: ProjectRepository
    @Inject lateinit var companyRepository: CompanyRepository

    /**
     * Exposed endpoint: returns a DTO, creating the report if needed.
     */
    @Transactional
    fun getOrInitializeByProjectId(projectId: Long): ControlReportDto {
        val model = getOrInitializeModel(projectId)
        return mapper.toDto(model)
    }

    /**
     * Internal helper: find or create the ControlReportModel.
     */
    @Transactional
    fun getOrInitializeModel(projectId: Long): ControlReportModel {
        repository.findByProjectId(projectId).firstOrNull()?.let { return it }

        val project = projectRepository.findById(projectId)
            ?: throw NotFoundException("Project $projectId")
        val company = companyRepository.findFirst()
            ?: throw IllegalStateException("No company configured")

        // prepare the same pieces you already have…
        val clientDto = ClientDto(
            type       = CustomerType.OWNER,
            name       = project.customer.person.firstName + " " + project.customer.person.lastName,
            street     = project.customer.person.details.street.orEmpty(),
            postalCode = project.customer.person.details.zipCode.orEmpty(),
            city       = project.customer.person.details.city.orEmpty()
        )
        val contractorDto = ContractorDto(
            type    = "", //TODO make a default inspector field in company. One Employee is the default inspector companyRepository.defaultInspectorType,
            company = company?.name ?: "",
            street  = company?.street ?: "",
            postalCode    = company?.zipCode ?: "",
            city          = company?.city ?: ""
        )
        val installDto = InstallationLocationDto(
            street       = project.customer.person.details.street.orEmpty(),
            postalCode   = project.customer.person.details.zipCode.orEmpty(),
            city         = project.customer.person.details.city.orEmpty(),
            buildingType = project.buildingType,
            parcelNumber = "" // TODO maybe needed idk project.parcelNumber
        )
        val controlDataDto = ControlDataDto(
            controlDate = LocalDate.now(),
            controllerId = 0,
            controllerFirstName = "",
            controllerLastName = "",
            phoneNumber = "",
            hasDefects = false,
            deadlineNote = null
        )

        // **HERE**: build a CreateDto, not a ControlReportDto
        val createDto = ControlReportCreateDto(
            projectId            = projectId,
            customerId           = project.customer.id!!,
            reportNumber         = "",
            pageCount            = 1,
            currentPage          = 1,

            // fill in your contractor fields—here I'm faking a type
            contractorType       = ContractorType.ELECTRICIAN,
            contractorCompany    = company.name,
            contractorStreet     = company.street,
            contractorPostalCode = company.zipCode,
            contractorCity       = company.city,

            installationStreet   = project.customer.person.details.street.orEmpty(),
            installationPostalCode  = project.customer.person.details.zipCode.orEmpty(),
            installationCity        = project.customer.person.details.city.orEmpty(),
            buildingType            = project.buildingType,
            parcelNumber            = project.parcelNumber,

            controlDate         = LocalDate.now(),
            controlScope        = "",
            controllerId        = null,
            controllerPhone     = "",
            hasDefects          = false,
            deadlineNote        = null,

            generalNotes        = "",

            defectPositions     = emptyList<DefectPositionCreateDto>(),

            defectResolverNote  = null,
            completionDate      = null,
            companyStamp        = null,
            completionSignature = null
        )

        // now mapper.toModel() matches CreateDto's type
        val model = mapper.toModel(createDto)
        model.persist()
        return model
    }
    /**
     * Standard “create” endpoint, untouched.
     */
    @Transactional
    fun createControlReport(dto: ControlReportCreateDto): ControlReportDto {
        val model = mapper.toModel(dto)
        model.persist()
        return mapper.toDto(model)
    }

    fun getControlReport(id: Long): ControlReportDto? =
        repository.findById(id)?.let(mapper::toDto)

    @Transactional
    fun updateControlReport(id: Long, dto: ControlReportUpdateDto): ControlReportDto? {
        val model = repository.findById(id) ?: return null
        mapper.applyUpdate(model, dto)
        return mapper.toDto(model)
    }

    @Transactional
    fun deleteControlReport(id: Long) =
        repository.deleteById(id)

    fun listControlReports(): List<ControlReportDto> =
        repository.listAll().map(mapper::toDto)

    fun listReportsByProject(projectId: Long): List<ControlReportDto> =
        repository.findByProjectId(projectId).map(mapper::toDto)

    @Transactional
    fun addDefectPosition(reportId: Long, dto: DefectPositionCreateDto): ControlReportDto? {
        val report = repository.findById(reportId) ?: return null
        val pos    = DefectPositionModel().apply {
            positionNumber = dto.positionNumber
            normReferences += dto.normReferences
            controlReport  = report
        }
        report.defectPositions.add(pos)
        return mapper.toDto(report)
    }

    @Transactional
    fun updateDefectPosition(
        reportId: Long,
        positionNumber: Int,
        dto: DefectPositionUpdateDto
    ): ControlReportDto? {
        val report = repository.findById(reportId) ?: return null
        val pos = report.defectPositions.firstOrNull { it.positionNumber == positionNumber }
            ?: return null
        pos.normReferences.apply {
            clear()
            addAll(dto.normReferences)
        }
        return mapper.toDto(report)
    }

    @Transactional
    fun removeDefectPosition(reportId: Long, positionNumber: Int): ControlReportDto? {
        val report = repository.findById(reportId) ?: return null
        val pos = report.defectPositions.firstOrNull { it.positionNumber == positionNumber }
            ?: return null
        report.defectPositions.remove(pos)
        return mapper.toDto(report)
    }

    @Transactional
    fun addCompletionConfirmation(
        reportId: Long,
        confirmationDto: CompletionConfirmationCreateDto
    ): ControlReportDto? {
        val report = repository.findById(reportId) ?: return null
        report.completionDate      = confirmationDto.resolvedAt
        report.companyStamp        = confirmationDto.companyStamp
        report.completionSignature = confirmationDto.signature
        return mapper.toDto(report)
    }
}
