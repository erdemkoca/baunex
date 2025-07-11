package ch.baunex.timetracking.controller

import ch.baunex.catalog.facade.CatalogFacade
import ch.baunex.notes.facade.NoteAttachmentFacade
import ch.baunex.notes.model.NoteCategory
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import ch.baunex.user.facade.EmployeeFacade
import ch.baunex.user.model.Role
import ch.baunex.web.WebController.Templates
import org.jboss.resteasy.reactive.multipart.FileUpload
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.serialization.encodeToString
import org.jboss.logging.Logger
import org.jboss.resteasy.reactive.RestForm
import java.io.InputStream
import java.time.LocalDate
import ch.baunex.serialization.SerializationUtils.json
import ch.baunex.timetracking.dto.HolidayApprovalDTO
import ch.baunex.timetracking.dto.HolidayDTO
import ch.baunex.timetracking.facade.HolidayFacade
import ch.baunex.timetracking.service.WorkSummaryService
import ch.baunex.timetracking.dto.EmployeeDailyWorkDTO
import ch.baunex.timetracking.dto.WeeklyWorkSummaryDTO

@Path("/timetracking")
@ApplicationScoped
class TimeTrackingController {

    @Inject lateinit var timeTrackingFacade: TimeTrackingFacade
    @Inject lateinit var employeeFacade: EmployeeFacade
    @Inject lateinit var projectFacade: ProjectFacade
    @Inject lateinit var catalogFacade: CatalogFacade
    @Inject lateinit var noteAttachmentFacade: NoteAttachmentFacade
    @Inject lateinit var holidayFacade: HolidayFacade
    @Inject lateinit var workSummaryService: WorkSummaryService

    private val log = Logger.getLogger(TimeTrackingController::class.java)
    private fun today() = LocalDate.now().toString()

    //─── HTML ────────────────────────────────────────────────────────────────────

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun listView(): Response {
        val entries   = timeTrackingFacade.getAllTimeEntries()
        val emps      = employeeFacade.listAll()
        val projs     = projectFacade.getAllProjects()
        val holidays  = holidayFacade.getAllHolidays()
        val page = Templates.timeTracking(
            activeMenu      = "timetracking",
            timeEntriesJson = json.encodeToString(entries),
            holidaysJson     = json.encodeToString(holidays),
            //TODO give weeklyhours instead of giving all timeentries. not to handle this in frontend. use TimeTrackingAnalysisService
            currentDate     = today(),
            employeesJson   = json.encodeToString(emps),
            projectsJson    = json.encodeToString(projs),
            entryJson       = ""
        )
        return Response.ok(page.render()).build()
    }

    @GET
    @Path("/{id}") @Produces(MediaType.TEXT_HTML)
    fun formView(@PathParam ("id") id: Long): Response {
        val entry       = if (id == 0L) null else timeTrackingFacade.getTimeEntryById(id)
        val emps        = employeeFacade.listAll()
        val projs       = projectFacade.getAllProjects()
        val cats        = catalogFacade.getAllItems()
        val categories  = NoteCategory.values().toList()
        val page = Templates.timeTrackingForm(
            entryJson        = entry?.let { json.encodeToString(it) } ?: "",
            employeesJson    = json.encodeToString(emps),
            projectsJson     = json.encodeToString(projs),
            catalogItemsJson = json.encodeToString(cats),
            categoriesJson   = json.encodeToString(categories),
            currentDate      = today(),
            activeMenu       = "timetracking"
        )
        return Response.ok(page.render()).build()
    }

    //─── JSON API ────────────────────────────────────────────────────────────────
    @POST
    @Path("/api") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    fun createApi(dto: TimeEntryDTO): TimeEntryDTO =
        timeTrackingFacade.logTime(dto)

    @PUT
    @Path("/api/{id}") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    fun updateApi(@PathParam("id") id: Long, dto: TimeEntryDTO): TimeEntryDTO? =
        timeTrackingFacade.updateTimeEntry(id, dto)

    @POST
    @Path("/api/{id}/approve")
    fun approveApi(@PathParam ("id") id: Long): Response {
        val adminId = employeeFacade.findByRole(Role.ADMIN).id
        return if (timeTrackingFacade.approveEntry(id, adminId))
            Response.noContent().build()
        else
            Response.status(Response.Status.NOT_FOUND).build()
    }

    @POST
    @Path("/api/upload/note-attachment")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    fun uploadAttachment(
        @FormParam("noteId") noteId: Long,
        @RestForm("file") stream: InputStream,
        @RestForm("file") upload: FileUpload?
    ): Response {
        if (upload == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Missing file")).build()
        }
        return try {
            val dto = noteAttachmentFacade.uploadAttachment(
                noteId, stream, upload.fileName()
            )
            Response.ok(dto).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.NOT_FOUND).build()
        } catch (e: Exception) {
            log.error("upload error", e)
            Response.serverError()
                .entity(mapOf("error" to e.message)).build()
        }
    }


    //─── Holiday JSON API ────────────────────────────────────────────────────────────────

    @GET
    @Path("/api/holidays")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAllHolidays(): List<HolidayDTO> =
        holidayFacade.getAllHolidays()

    @GET
    @Path("/api/holidays/employee/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getHolidaysForEmployee(@PathParam("id") employeeId: Long): List<HolidayDTO> =
        holidayFacade.getHolidaysForEmployee(employeeId)

    @POST
    @Path("/api/holidays")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun requestHoliday(dto: HolidayDTO): HolidayDTO =
        holidayFacade.requestHoliday(dto)

    @POST
    @Path("/api/holidays/{id}/approve")
    @Consumes(MediaType.APPLICATION_JSON)
    fun approveHoliday(
        @PathParam("id") holidayId: Long,
        dto: HolidayApprovalDTO
    ): Response {
        val updated = holidayFacade.approveHoliday(dto)
        return if (updated != null) Response.ok(updated).build()
        else Response.status(Response.Status.NOT_FOUND).build()
    }

    //─── Work Summary JSON API ────────────────────────────────────────────────────────────────

    @GET
    @Path("/api/summary/daily")
    @Produces(MediaType.APPLICATION_JSON)
    fun getDailyWorkSummary(
        @QueryParam("employeeId") employeeId: Long?,
        @QueryParam("from") from: String,
        @QueryParam("to") to: String
    ): List<EmployeeDailyWorkDTO> {
        val fromDate = LocalDate.parse(from)
        val toDate = LocalDate.parse(to)
        
        return if (employeeId != null) {
            workSummaryService.getDailyWorkSummary(employeeId, fromDate, toDate)
        } else {
            workSummaryService.getAllEmployeesDailyWorkSummary(fromDate, toDate)
        }
    }

    @GET
    @Path("/api/summary/weekly")
    @Produces(MediaType.APPLICATION_JSON)
    fun getWeeklyWorkSummary(
        @QueryParam("employeeId") employeeId: Long?,
        @QueryParam("year") year: Int,
        @QueryParam("week") week: Int
    ): List<WeeklyWorkSummaryDTO> {
        return if (employeeId != null) {
            val summary = workSummaryService.getWeeklyWorkSummary(employeeId, year, week)
            if (summary != null) listOf(summary) else emptyList()
        } else {
            workSummaryService.getAllEmployeesWeeklyWorkSummary(year, week)
        }
    }

    @GET
    @Path("/api/summary/expected-hours")
    @Produces(MediaType.APPLICATION_JSON)
    fun getExpectedHours(
        @QueryParam("employeeId") employeeId: Long,
        @QueryParam("date") date: String
    ): Map<String, Double> {
        val localDate = LocalDate.parse(date)
        val expectedHours = workSummaryService.calculateExpectedHours(employeeId, localDate)
        return mapOf("expectedHours" to expectedHours)
    }

}
