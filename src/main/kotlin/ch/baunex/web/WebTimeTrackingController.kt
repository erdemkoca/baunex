package ch.baunex.web

import ch.baunex.project.facade.ProjectFacade
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import ch.baunex.user.facade.EmployeeFacade
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI
import java.time.LocalDate

@Path("/timetracking")
class WebTimeTrackingController {

    @Inject
    lateinit var timeTrackingFacade: TimeTrackingFacade

    @Inject
    lateinit var employeeFacade: EmployeeFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    private fun getCurrentDate(): String = LocalDate.now().toString()

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun view(): Response {
        val timeEntries = timeTrackingFacade.getAllTimeEntries()
        val employees   = employeeFacade.listAll()
        val projects    = projectFacade.getAllProjects()
        val template = WebController.Templates.timetracking(
            activeMenu  = "timetracking",
            timeEntries = timeEntries,
            currentDate = getCurrentDate(),
            employees   = employees,
            projects    = projects,
            entry       = null
        )
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    fun newEntry(): Response {
        val employees = employeeFacade.listAll()
        val projects  = projectFacade.getAllProjects()
        val template = WebController.Templates.timetrackingForm(
            entry       = null,
            employees   = employees,
            projects    = projects,
            currentDate = getCurrentDate(),
            activeMenu  = "timetracking"
        )
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    fun edit(@PathParam("id") id: Long): Response {
        val entry     = timeTrackingFacade.getTimeEntryById(id)
            ?: return Response.status(Response.Status.NOT_FOUND).build()
        val employees = employeeFacade.listAll()
        val projects  = projectFacade.getAllProjects()
        val template = WebController.Templates.timetrackingForm(
            entry       = entry,
            employees   = employees,
            projects    = projects,
            currentDate = getCurrentDate(),
            activeMenu  = "timetracking"
        )
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun saveEntry(
        @FormParam("id") id: Long?,
        @FormParam("employeeId") employeeId: Long,
        @FormParam("projectId") projectId: Long,
        @FormParam("date") date: LocalDate,
        @FormParam("hoursWorked") hoursWorked: Double,
        @FormParam("note") note: String?,
        @FormParam("hourlyRate") hourlyRate: Double?,
        @FormParam("billable") billable: Boolean,
        @FormParam("invoiced") invoiced: Boolean,
        @FormParam("catalogItemDescription") catalogItemDescription: String?,
        @FormParam("catalogItemPrice") catalogItemPrice: Double?
    ): Response {
        val dto = TimeEntryDTO(
            employeeId              = employeeId,
            projectId               = projectId,
            date                    = date,
            hoursWorked             = hoursWorked,
            note                    = note,
            hourlyRate              = hourlyRate,
            billable                = billable,
            invoiced                = invoiced,
            catalogItemDescription  = catalogItemDescription,
            catalogItemPrice        = catalogItemPrice
        )

        if (id == null) {
            timeTrackingFacade.logTime(dto)
        } else {
            timeTrackingFacade.updateTimeEntry(id, dto)
        }

        return Response.seeOther(URI("/timetracking")).build()
    }

    @GET
    @Path("/{id}/delete")
    fun delete(@PathParam("id") id: Long): Response {
        timeTrackingFacade.deleteTimeEntry(id)
        return Response.seeOther(URI("/timetracking")).build()
    }
}
