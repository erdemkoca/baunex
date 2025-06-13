package ch.baunex.controlreport.facade

import ch.baunex.controlreport.dto.*
import ch.baunex.controlreport.service.ControlReportService
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ControlReportFacade(
    private val controlReportService: ControlReportService
) {

    fun createReport(createDto: ControlReportCreateDto): ControlReportDto {
        return controlReportService.createControlReport(createDto)
    }

    fun listReports(): List<ControlReportDto> {
        return controlReportService.listControlReports()
    }

    fun getReportByProjectId(projectId: Long): ControlReportDto? =
        controlReportService.listReportsByProject(projectId)
            .firstOrNull()

    fun getOrInitializeReport(projectId: Long): ControlReportDto =
        controlReportService.getOrInitializeByProjectId(projectId)


    fun listReportsByProject(projectId: Long): List<ControlReportDto> =
        controlReportService.listReportsByProject(projectId)

    /** Einzelnen Report abfragen */
    fun getReport(id: Long): ControlReportDto? {
        return controlReportService.getControlReport(id)
    }

    /** Report aktualisieren */
    fun updateReport(id: Long, updateDto: ControlReportUpdateDto): ControlReportDto? {
        return controlReportService.updateControlReport(id, updateDto)
    }

    /** Report löschen */
    fun deleteReport(id: Long) {
        controlReportService.deleteControlReport(id)
    }

    /** Defect-Position hinzufügen (gibt den aktualisierten Report zurück) */
    fun addDefectPosition(
        reportId: Long,
        createDto: DefectPositionCreateDto
    ): ControlReportDto? {
        return controlReportService.addDefectPosition(reportId, createDto)
    }

    /** Defect-Position ändern (gibt den aktualisierten Report zurück) */
    fun updateDefectPosition(
        reportId: Long,
        positionNumber: Int,
        updateDto: DefectPositionUpdateDto
    ): ControlReportDto? {
        return controlReportService.updateDefectPosition(reportId, positionNumber, updateDto)
    }

    /** Defect-Position entfernen (gibt den aktualisierten Report zurück) */
    fun removeDefectPosition(
        reportId: Long,
        positionNumber: Int
    ): ControlReportDto? {
        return controlReportService.removeDefectPosition(reportId, positionNumber)
    }

    /** Abschlussbestätigung hinzufügen (gibt den aktualisierten Report zurück) */
    fun addCompletionConfirmation(
        reportId: Long,
        createDto: CompletionConfirmationCreateDto
    ): ControlReportDto? {
        return controlReportService.addCompletionConfirmation(reportId, createDto)
    }
}
