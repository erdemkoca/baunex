package ch.baunex.controlreport.service

import ch.baunex.company.repository.CompanyRepository
import ch.baunex.controlreport.dto.*
import ch.baunex.controlreport.mapper.ControlReportMapper
import ch.baunex.controlreport.model.ContractorType
import ch.baunex.controlreport.model.ControlReportModel
import ch.baunex.controlreport.model.DefectPositionModel
import ch.baunex.controlreport.repository.ControlReportRepository
import ch.baunex.project.repository.ProjectRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException
import java.time.LocalDate
import java.time.LocalDateTime

@ApplicationScoped
class ControlReportService(
    private val mapper: ControlReportMapper
) {
    @Inject lateinit var repository: ControlReportRepository
    @Inject lateinit var projectRepository: ProjectRepository
    @Inject lateinit var companyRepository: CompanyRepository

    @Transactional
    fun getOrInitializeByProjectId(projectId: Long): ControlReportDto {
        val model = getOrInitializeModel(projectId)
        return mapper.toDto(model)
    }

    @Transactional
    fun getOrInitializeModel(projectId: Long): ControlReportModel {
        // 1) Return existing if there is one
        repository.findByProjectId(projectId).firstOrNull()
            ?.let { return it }

        // 2) Otherwise bootstrap a blank report
        val project = projectRepository.findById(projectId)
            ?: throw NotFoundException("Project $projectId")
        val company = companyRepository.findFirst()
            ?: throw IllegalStateException("No company configured")

        val model = ControlReportModel().apply {
            // link the owning project
            this.project = project

            // client = the project’s customer
            this.customer = project.customer

            // contractor defaults
            this.contractorType        = ContractorType.CONTROL_ORGAN
            this.contractorCompany     = company.name
            this.contractorStreet      = company.street
            this.contractorHouseNumber = null
            this.contractorPostalCode  = company.zipCode
            this.contractorCity        = company.city

            // installation location defaults
            this.installationStreet      = project.customer.person.details.street.orEmpty()
            this.installationHouseNumber = null
            this.installationPostalCode  = project.customer.person.details.zipCode.orEmpty()
            this.installationCity        = project.customer.person.details.city.orEmpty()
            this.parcelNumber            = project.parcelNumber ?: ""

            // control data defaults
            this.controlDate    = LocalDate.now()
            this.controlScope   = ""
            this.employee       = null       // no controller yet
            this.hasDefects     = false
            this.deadlineNote   = null
            this.generalNotes   = ""

            // empty collections (they’re already initialized)
            // this.defectPositions = mutableListOf()
            // this.notes           = mutableListOf()

            // completion defaults
            this.defectResolverNote  = null
            this.completionDate      = null

            // metadata timestamps
            this.createdAt = LocalDateTime.now()
            this.updatedAt = LocalDateTime.now()
        }

        model.persist()
        return model
    }

    @Transactional
    fun createControlReport(dto: ControlReportCreateDto): ControlReportDto {
        val model = mapper.toModel(dto)
        model.persist()
        return mapper.toDto(model)
    }

    @Transactional
    fun updateByProjectId(projectId: Long, dto: ControlReportUpdateDto): ControlReportDto? {
        val model = repository.findByProjectId(projectId).firstOrNull()
            ?: return null
        mapper.applyUpdate(model, dto)
        return mapper.toDto(model)
    }

    fun listReportsByProject(projectId: Long): List<ControlReportDto> =
        repository.findByProjectId(projectId).map(mapper::toDto)
}
