package ch.baunex.controlreport.facade

import ch.baunex.controlreport.dto.*
import ch.baunex.controlreport.service.ControlReportService
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ControlReportFacade(
    private val controlReportService: ControlReportService
) {
    fun getOrInitializeReport(projectId: Long): ControlReportDto =
        controlReportService.getOrInitializeByProjectId(projectId)

    fun updateReportByProject(projectId: Long, dto: ControlReportDto): ControlReportDto? {
        println("Updating control report for project $projectId with DTO: $dto")
        return controlReportService.updateByProjectId(projectId, dto)
    }
}
