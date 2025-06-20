package ch.baunex.controlreport.service

import ch.baunex.company.repository.CompanyRepository
import ch.baunex.controlreport.dto.*
import ch.baunex.controlreport.mapper.ControlReportMapper
import ch.baunex.controlreport.model.ContractorType
import ch.baunex.controlreport.model.ControlReportModel
import ch.baunex.controlreport.repository.ControlReportRepository
import ch.baunex.controlreport.repository.DefectPositionRepository
import ch.baunex.project.repository.ProjectRepository
import ch.baunex.user.repository.EmployeeRepository
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
    @Inject lateinit var controlReportRepository: ControlReportRepository
    @Inject lateinit var projectRepository: ProjectRepository
    @Inject lateinit var companyRepository: CompanyRepository
    @Inject lateinit var employeeRepository: EmployeeRepository
    @Inject lateinit var defectPositionRepository: DefectPositionRepository


    @Transactional
    fun getOrInitializeByProjectId(projectId: Long): ControlReportDto {
        val model = getOrInitializeModel(projectId)
        return mapper.toDto(model)
    }

    @Transactional
    fun getOrInitializeModel(projectId: Long): ControlReportModel {
        controlReportRepository.findByProjectId(projectId).firstOrNull()?.let { return it }

        val project = projectRepository.findById(projectId)
            ?: throw NotFoundException("Project $projectId")
        val company = companyRepository.findFirst()
            ?: throw IllegalStateException("No company configured")
        val customer = project.customer

        val model = ControlReportModel().apply {
            this.project = project

            // Auftragnehmer
            this.contractorType = ContractorType.CONTROL_ORGAN
            this.contractorCompany = company.name
            this.contractorStreet = company.street
            this.contractorPostalCode = company.zipCode
            this.contractorCity = company.city

            // Kontrolle
            this.controlDate = LocalDate.now()
            this.controlScope = ""
            this.employee = null
            this.hasDefects = false
            this.deadlineNote = null
            this.generalNotes = ""
            this.createdAt = LocalDateTime.now()
            this.updatedAt = LocalDateTime.now()
        }

        model.persist()
        model.reportNumber = (model.id + 1000).toInt()
        return model
    }

    @Transactional
    fun updateByProjectId(projectId: Long, dto: ControlReportDto): ControlReportDto? {
        // 1) Report holen oder neu anlegen
        val report = controlReportRepository
            .findByProjectId(projectId)
            .firstOrNull()
            ?: getOrInitializeModel(projectId)

        // 2) Report-Felder updaten
        mapper.applyUpdate(report, dto)

        // 3) Controller zuweisen (wenn vorhanden)
        dto.controlData.controllerId?.let { controllerId ->
            report.employee = employeeRepository.findById(controllerId)
                ?: throw NotFoundException("Employee $controllerId nicht gefunden")
        }

        // 4) Alle existierenden DefectPositions für diesen Report holen
        val existingPositions = defectPositionRepository.findByControlReportId(report.id)

        // 5) Mängelpositionen updaten
        dto.defectPositions?.forEach { dpDto ->
            val dpModel = dpDto.id?.let { id ->
                existingPositions.find { it.id == id }
                    ?: throw NotFoundException("DefectPosition $id nicht gefunden")
            } ?: throw NotFoundException("Neue DefectPositions ohne ID werden hier nicht unterstützt")

            dpModel.buildingLocation = dpDto.buildingLocation.orEmpty()
            dpModel.description      = dpDto.description
            dpModel.updatedAt        = LocalDateTime.now()
        }

        // 6) Gemapptes Ergebnis zurückgeben
        return mapper.toDto(report)
    }
}
