package ch.baunex.controlreport.service

import ch.baunex.company.repository.CompanyRepository
import ch.baunex.controlreport.dto.*
import ch.baunex.controlreport.mapper.ControlReportMapper
import ch.baunex.controlreport.model.ContractorType
import ch.baunex.controlreport.model.ControlReportModel
import ch.baunex.controlreport.model.DefectPositionModel
import ch.baunex.controlreport.repository.ControlReportRepository
import ch.baunex.controlreport.repository.DefectPositionRepository
import ch.baunex.notes.model.NoteCategory
import ch.baunex.notes.model.NoteModel
import ch.baunex.notes.repository.NoteRepository
import ch.baunex.project.repository.ProjectRepository
import ch.baunex.user.repository.EmployeeRepository
import ch.baunex.user.service.EmployeeService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@ApplicationScoped
class ControlReportService(
    private val mapper: ControlReportMapper
) {
    @Inject lateinit var controlReportRepository: ControlReportRepository
    @Inject lateinit var projectRepository: ProjectRepository
    @Inject lateinit var companyRepository: CompanyRepository
    @Inject lateinit var employeeRepository: EmployeeRepository
    @Inject lateinit var defectPositionRepository: DefectPositionRepository
    @Inject lateinit var noteRepository: NoteRepository


    @Transactional
    fun getOrInitializeByProjectId(projectId: Long): ControlReportDto {
        val model = getOrInitializeModel(projectId)
        return mapper.toDto(model)
    }

    @Transactional
    fun getOrInitializeModel(projectId: Long): ControlReportModel {
        // 1) Return existing if there is one
        controlReportRepository.findByProjectId(projectId).firstOrNull()
            ?.let { return it }

        // 2) Otherwise bootstrap a blank report
        val project = projectRepository.findById(projectId)
            ?: throw NotFoundException("Project $projectId")
        val company = companyRepository.findFirst()
            ?: throw IllegalStateException("No company configured")

        val model = ControlReportModel().apply {
            // link the owning project
            this.project = project

            // client = the project's customer
            this.customer = project.customer

            // contractor defaults
            this.contractorType        = ContractorType.CONTROL_ORGAN
            this.contractorCompany     = company.name
            this.contractorStreet      = company.street
            this.contractorPostalCode  = company.zipCode
            this.contractorCity        = company.city

            // installation location defaults
            this.installationStreet      = project.customer.person.details.street.orEmpty()
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

            // empty collections (they're already initialized)
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
        model.reportNumber = model.reportNumber?.plus(1000)
        return model
    }

    @Transactional
    fun updateByProjectId(projectId: Long, dto: ControlReportUpdateDto): ControlReportDto? {
        // 1) Report holen oder neu anlegen
        val report = controlReportRepository
            .findByProjectId(projectId)
            .firstOrNull()
            ?: getOrInitializeModel(projectId)

        // 2) Report-Felder updaten
        mapper.applyUpdate(report, dto)

        // Update controller if specified
        if (dto.controllerId != null) {
            report.employee = employeeRepository.findById(dto.controllerId)
                ?: throw NotFoundException("Employee ${dto.controllerId} nicht gefunden")
        }

        // 3) Alle existierenden DefectPositions für diesen Report holen
        val existingPositions = defectPositionRepository.findByControlReportId(report.id)
        println("Found ${existingPositions.size} existing positions")
        existingPositions.forEach { dp ->
            println("Existing position ID: ${dp.id}")
        }

        // 4) Jede Mängel-Position aktualisieren
        dto.defectPositions?.forEach { dpDto ->
            println("Looking for position with ID: ${dpDto.id}")
            // Finde die entsprechende existierende Position
            val dpModel = if (dpDto.id != null) {
                existingPositions.find { it.id == dpDto.id }
                    ?: throw NotFoundException("DefectPosition ${dpDto.id} nicht gefunden")
            } else {
                // Wenn keine ID angegeben ist, nehmen wir die erste Position
                existingPositions.firstOrNull()
                    ?: throw NotFoundException("Keine DefectPosition gefunden")
            }

            // Update die Felder
            dpModel.normReferences = dpDto.normReferences.toMutableList()
            dpModel.note.content = dpDto.noteContent
            dpModel.note.updatedAt = LocalDate.now()
        }

        // 5) Am Ende das ReportModel mappen und zurückgeben
        return mapper.toDto(report)
    }



    fun listReportsByProject(projectId: Long): List<ControlReportDto> =
        controlReportRepository.findByProjectId(projectId).map(mapper::toDto)
}
