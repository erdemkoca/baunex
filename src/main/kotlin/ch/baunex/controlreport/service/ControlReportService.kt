package ch.baunex.controlreport.service

import ch.baunex.controlreport.dto.*
import ch.baunex.controlreport.mapper.ControlReportMapper
import ch.baunex.controlreport.model.DefectPositionModel
import ch.baunex.controlreport.repository.ControlReportRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class ControlReportService(
    private val mapper: ControlReportMapper
) {
    @Inject
    lateinit var repository: ControlReportRepository


    @Transactional
    fun createControlReport(dto: ControlReportCreateDto): ControlReportDto {
        val model = mapper.toModel(dto)
        model.persist()
        return mapper.toDto(model)
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
