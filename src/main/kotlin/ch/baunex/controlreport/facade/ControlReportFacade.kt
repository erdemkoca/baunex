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


    fun listReportsByProject(projectId: Long): List<ControlReportDto> =
        controlReportService.listReportsByProject(projectId)

    fun updateReportByProject(projectId: Long, dto: ControlReportUpdateDto): ControlReportDto? =
        controlReportService.updateByProjectId(projectId, dto)

}
