package ch.baunex.controlreport.controller

import ch.baunex.common.dto.EnumOption
import ch.baunex.controlreport.dto.ControlReportCreateDto
import ch.baunex.controlreport.dto.ControlReportDto
import ch.baunex.controlreport.dto.ControlReportUpdateDto
import ch.baunex.controlreport.facade.ControlReportFacade
import ch.baunex.controlreport.model.ContractorType
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.serialization.SerializationUtils.json
import ch.baunex.user.facade.EmployeeFacade
import ch.baunex.user.model.CustomerType
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

    @Inject
    lateinit var employeeFacade: EmployeeFacade

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun editOrNew(@PathParam("projectId") projectId: Long): Response {
        val project       = projectFacade.getProjectWithDetails(projectId) ?: throw NotFoundException()
        val reportDto     = reportFacade.getOrInitializeReport(projectId)
        val customerTypes   = CustomerType.values().map { EnumOption(it.name, it.displayName) }
        val contractorTypes = ContractorType.values().map { EnumOption(it.name, it.displayName) }

        val employees = employeeFacade.listAll()

        val tpl = WebController.Templates.projectControlReport(
            projectJson            = json.encodeToString(project),
            controlReportJson      = json.encodeToString(reportDto),
            customerTypesJson        = json.encodeToString(customerTypes),
            contractorTypesJson    = json.encodeToString(contractorTypes),
            currentDate            = LocalDate.now(),
            activeMenu             = "projects",
            projectId              = projectId,
            activeSubMenu          = "controlreport",
            employeesJson          = json.encodeToString(employees)
        )
        return Response.ok(tpl.render()).build()
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getReportsByProject(@PathParam("projectId") projectId: Long): List<ControlReportDto> =
        reportFacade.listReportsByProject(projectId)

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun create(
        dto: ControlReportCreateDto
    ): ControlReportDto {
        return reportFacade.createReport(dto)
    }

//    @PUT
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    fun update(
//        @PathParam("projectId") projectId: Long,
//        dto: ControlReportUpdateDto
//    ): ControlReportDto {
//        return reportFacade.updateReport(projectId, dto)
//            ?: throw NotFoundException()
//    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun update(
        @PathParam("projectId") projectId: Long,
        dto: ControlReportUpdateDto
    ): ControlReportDto =
        reportFacade
            .updateReportByProject(projectId, dto)
            ?: throw NotFoundException("No report for project $projectId")

}
