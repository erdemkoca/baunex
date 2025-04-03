package ch.baunex.web

import ch.baunex.project.facade.ProjectFacade
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import ch.baunex.user.facade.UserFacade
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
    lateinit var userFacade: UserFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    private fun getCurrentDate(): String = LocalDate.now().toString()

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun view(): Response {
        val template = WebController.Templates.timetracking(
            timeEntries = timeTrackingFacade.getAllTimeEntries(),
            users = userFacade.getAllUsers(),
            projects = projectFacade.getAllProjects(),
            currentDate = getCurrentDate(),
            activeMenu = "timetracking",
            entry = null
        )
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    fun newEntry(): Response {
        val template = WebController.Templates.timetrackingForm(
            entry = null,
            users = userFacade.getAllUsers(),
            projects = projectFacade.getAllProjects(),
            currentDate = getCurrentDate(),
            activeMenu = "timetracking"
        )
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun saveEntry(
        @FormParam("id") id: Long?,
        @FormParam("userId") userId: Long,
        @FormParam("projectId") projectId: Long,
        @FormParam("date") date: String,
        @FormParam("hoursWorked") hoursWorked: Double,
        @FormParam("note") note: String?
    ): Response {
        val dto = TimeEntryDTO(userId, projectId, date, hoursWorked, note)

        if (id == null) {
            timeTrackingFacade.logTime(dto)
        } else {
            timeTrackingFacade.updateTimeEntry(id, dto)
        }

        return Response.seeOther(URI("/timetracking")).build()
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    fun edit(@PathParam("id") id: Long): Response {
        val entry = timeTrackingFacade.getTimeEntryById(id)
            ?: return Response.status(Response.Status.NOT_FOUND).build()

        val template = WebController.Templates.timetrackingForm(
            entry = entry,
            users = userFacade.getAllUsers(),
            projects = projectFacade.getAllProjects(),
            currentDate = getCurrentDate(),
            activeMenu = "timetracking"
        )
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}/delete")
    fun delete(@PathParam("id") id: Long): Response {
        timeTrackingFacade.deleteTimeEntry(id)
        return Response.seeOther(URI("/timetracking")).build()
    }
}
