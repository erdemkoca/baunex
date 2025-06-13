package ch.baunex.controlreport.service

import ch.baunex.company.repository.CompanyRepository
import ch.baunex.controlreport.dto.*
import ch.baunex.controlreport.mapper.ControlReportMapper
import ch.baunex.controlreport.model.ClientType
import ch.baunex.controlreport.model.DefectPositionModel
import ch.baunex.controlreport.repository.ControlReportRepository
import ch.baunex.project.repository.ProjectRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException
import java.time.LocalDateTime

@ApplicationScoped
class ControlReportService(
    private val mapper: ControlReportMapper
) {
    @Inject
    lateinit var repository: ControlReportRepository

    @Inject
    lateinit var projectRepository: ProjectRepository

    @Inject
    lateinit var companyRepository: CompanyRepository

    @Transactional
    fun createControlReport(dto: ControlReportCreateDto): ControlReportDto {
        val model = mapper.toModel(dto)
        model.persist()
        return mapper.toDto(model)
    }

    @Transactional
    fun getOrInitializeByProjectId(projectId: Long): ControlReportDto {
        // 1) Try existing
        val existing = repository.findByProjectId(projectId)
            .firstOrNull()
        if (existing != null) {
            return mapper.toDto(existing)
        }

        val company = companyRepository.findFirst()
        // 2) No report yet → build a blank one
        val project = projectRepository.findById(projectId)
            ?: throw NotFoundException("Project $projectId")
        // pull whatever you need: customer, your company, etc.
        val clientDto = ClientDto(
            type       = ClientType.OWNER,
            name       = project.customer.person.firstName + project.customer.person.lastName,
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
            buildingType = "Mehrfamilienhaus", //TODO maybe an enum with Mehrfamilienhaus, Wohnung etc
            parcelNumber = "" // TODO maybe needed idk project.parcelNumber
        )
        // finally hand back a ControlReportDto
        return ControlReportDto(
            id                   = 0L,          // zero signals “new”
            reportNumber         = "",
            pageCount            = 1,
            currentPage          = 1,
            client               = clientDto,
            contractor           = contractorDto,
            installationLocation = installDto,
            controlScope         = "",
            controlData          = ControlDataDto(
                controlDate    = LocalDateTime.now(),
                controllerName = "",
                phoneNumber    = "",
                hasDefects     = false,
                deadlineNote   = null
            ),
            generalNotes         = "",
            defectPositions      = emptyList(),
            defectResolverNote   = null,
            completionConfirmation = null,
            createdAt            = LocalDateTime.now(),
            updatedAt            = LocalDateTime.now()
        )
    }


    fun getControlReport(id: Long): ControlReportDto? {
        val model = repository.findById(id) ?: return null
        return mapper.toDto(model)
    }

    @Transactional
    fun updateControlReport(id: Long, dto: ControlReportUpdateDto): ControlReportDto? {
        val model = repository.findById(id) ?: return null
        mapper.applyUpdate(model, dto)
        return mapper.toDto(model)
    }

    @Transactional
    fun deleteControlReport(id: Long) {
        repository.deleteById(id)
    }

    fun listControlReports(): List<ControlReportDto> {
        return repository.listAll().map { mapper.toDto(it) }
    }

    fun listReportsByProject(projectId: Long): List<ControlReportDto> =
        repository.findByProjectId(projectId)
            .map { mapper.toDto(it) }

    @Transactional
    fun addDefectPosition(reportId: Long, dto: DefectPositionCreateDto): ControlReportDto? {
        val report = repository.findById(reportId) ?: return null
        val pos = DefectPositionModel().apply {
            positionNumber = dto.positionNumber
            photoUrl       = dto.photoUrl
            description    = dto.description
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
        // direkt hier updaten – kein weiterer Mapper-Call nötig
        pos.apply {
            photoUrl            = dto.photoUrl
            description         = dto.description
            normReferences.clear()
            normReferences.addAll(dto.normReferences)
            resolvedAt          = dto.resolvedAt
            resolutionStamp     = dto.resolutionStamp
            resolutionSignature = dto.resolutionSignature
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
        // statt eines nicht vorhandenen Objekts setzen wir direkt die drei Felder:
        report.completionDate        = confirmationDto.resolvedAt
        report.companyStamp          = confirmationDto.companyStamp
        report.completionSignature   = confirmationDto.signature
        return mapper.toDto(report)
    }
}
