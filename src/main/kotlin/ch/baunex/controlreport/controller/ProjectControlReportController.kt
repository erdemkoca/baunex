package ch.baunex.controlreport.controller

import ch.baunex.controlreport.dto.ControlReportDto
import ch.baunex.controlreport.facade.ControlReportFacade
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.serialization.SerializationUtils.json
import ch.baunex.web.WebController
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.serialization.encodeToString
import java.time.LocalDate

@Path("/projects/{projectId}/controlreport")
@ApplicationScoped
class ProjectControlReportController {

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var reportFacade: ControlReportFacade

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun show(@PathParam("projectId") projectId: Long): Response {
        val project = projectFacade.getProjectWithDetails(projectId)
            ?: throw NotFoundException()

        val reports: List<ControlReportDto> =
            reportFacade.listReportsByProject(projectId)

        val reportsJson = json.encodeToString(reports)
        // val singleReportJson = json.encodeToString(reports.firstOrNull())

        val tpl = WebController.Templates.projectControlReport(
            projectJson       = json.encodeToString(project),
            controlReportsJson = reportsJson,
            currentDate       = LocalDate.now(),
            activeMenu        = "projects",
            projectId         = projectId,
            activeSubMenu     = "controlreport"
        )

        return Response.ok(tpl.render()).build()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getReportsByProject(@PathParam("projectId") projectId: Long): List<ControlReportDto> =
        reportFacade.listReportsByProject(projectId)
}
