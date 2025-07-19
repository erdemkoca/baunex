package ch.baunex.timetracking.controller

import ch.baunex.catalog.facade.CatalogFacade
import ch.baunex.notes.facade.NoteAttachmentFacade
import ch.baunex.notes.model.NoteCategory
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.ErrorResponseDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import ch.baunex.user.facade.EmployeeFacade
import ch.baunex.user.model.Role
import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
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
import ch.baunex.timetracking.dto.WeeklyApprovalRequest
import ch.baunex.timetracking.facade.HolidayFacade
import ch.baunex.timetracking.service.WorkSummaryService
import ch.baunex.timetracking.dto.EmployeeDailyWorkDTO
import ch.baunex.timetracking.dto.WeeklyWorkSummaryDTO
import ch.baunex.timetracking.dto.MonthlyHoursAccountDTO
import ch.baunex.timetracking.dto.ExpectedHoursDTO
import java.net.URI

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
    @Inject lateinit var holidayDefinitionService: ch.baunex.timetracking.service.HolidayDefinitionService
    @Inject lateinit var overviewService: ch.baunex.timetracking.service.OverviewService

    private val log = Logger.getLogger(TimeTrackingController::class.java)
    private fun today() = LocalDate.now().toString()

    @CheckedTemplate(basePath = "timetracking", requireTypeSafeExpressions = false)
    object Templates {
        @JvmStatic
        external fun form(
            entryJson: String,
            employeesJson: String,
            projectsJson: String,
            catalogItemsJson: String,
            categoriesJson: String,
            currentDate: String,
            activeMenu: String,
            activeSubMenu: String
        ): TemplateInstance
        
        @JvmStatic
        external fun calendar(
            activeMenu: String,
            activeSubMenu: String,
            timeEntriesJson: String,
            holidaysJson: String,
            currentDate: String,
            employeesJson: String,
            projectsJson: String,
            monthlyAccountJson: String
        ): TemplateInstance
        
        @JvmStatic
        external fun absences(
            activeMenu: String,
            activeSubMenu: String,
            holidaysJson: String,
            employeesJson: String,
            pendingHolidaysJson: String,
            approvedHolidaysJson: String,
            rejectedHolidaysJson: String,
            employeeStatsJson: String,
            publicHolidaysJson: String,
            currentYear: Int
        ): TemplateInstance
        
        @JvmStatic
        external fun overview(
            activeMenu: String,
            activeSubMenu: String,
            overviewDataJson: String
        ): TemplateInstance
        

    }

    //─── HTML ────────────────────────────────────────────────────────────────────

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun indexView(): Response {
        // Redirect to overview as the main time tracking page
        return Response.seeOther(URI("/timetracking/overview")).build()
    }
    
    @GET
    @Path("/overview")
    @Produces(MediaType.TEXT_HTML)
    fun overviewView(
        @QueryParam("employeeId") employeeId: Long?
    ): Response {
        val overviewData = overviewService.getOverviewData(employeeId)
        val page = Templates.overview(
            activeMenu = "timetracking",
            activeSubMenu = "overview",
            overviewDataJson = json.encodeToString(overviewData)
        )
        return Response.ok(page.render()).build()
    }
    
    @GET
    @Path("/calendar")
    @Produces(MediaType.TEXT_HTML)
    fun calendarView(
        @QueryParam("employeeId") employeeId: Long?,
        @QueryParam("year") year: Int?
    ): Response {
        val currentYear = year ?: LocalDate.now().year
        val employees = employeeFacade.listAll()
        val selectedEmployeeId = employeeId ?: employees.firstOrNull()?.id ?: 0L
        
        val entries = timeTrackingFacade.getAllTimeEntries()
        val projects = projectFacade.getAllProjects()
        val holidays = holidayFacade.getAllHolidays()
        
        // Load monthly account for consistent saldo calculation
        val monthlyAccount = if (selectedEmployeeId > 0) {
            workSummaryService.getMonthlyHoursAccount(selectedEmployeeId, currentYear)
        } else null
        
        val page = Templates.calendar(
            activeMenu = "timetracking",
            activeSubMenu = "calendar",
            timeEntriesJson = json.encodeToString(entries),
            holidaysJson = json.encodeToString(holidays),
            currentDate = LocalDate.now().toString(),
            employeesJson = json.encodeToString(employees),
            projectsJson = json.encodeToString(projects),
            monthlyAccountJson = monthlyAccount?.let { json.encodeToString(it) } ?: "null"
        )
        
        return Response.ok(page.render()).build()
    }
    
    @GET
    @Path("/absences")
    @Produces(MediaType.TEXT_HTML)
    fun absencesView(): Response {
        val holidays = holidayFacade.getAllHolidays()
        val employees = employeeFacade.listAll()
        val currentYear = LocalDate.now().year
        
        // Get fresh data after creating samples
        val allHolidays = holidays
        
        // Für das Frontend: requestDate = createdAt
        val pendingHolidays = allHolidays.filter { it.status == "PENDING" || it.status == "UNDEFINED" }
        val approvedHolidays = allHolidays.filter { it.status == "APPROVED" }
        val rejectedHolidays = allHolidays.filter { it.status == "REJECTED" }
        
        // Get public holidays for the current year
        val publicHolidayModels = holidayDefinitionService.getHolidaysForYear(currentYear)
        val publicHolidays = publicHolidayModels.map { model ->
            ch.baunex.timetracking.dto.PublicHolidayDTO(
                id = model.id,
                year = model.year,
                holidayDate = model.date.toString(),
                name = model.name,
                canton = model.canton,
                isFixed = model.isFixed,
                isEditable = model.isEditable,
                active = model.active,
                isWorkFree = model.isWorkFree,
                holidayType = model.holidayType.displayName,
                description = model.description
            )
        }
        
        // Calculate employee statistics with proper structure
        val employeeStats = employees.map { employee ->
            val employeeHolidays = allHolidays.filter { it.employeeId == employee.id }
            val approvedDays = employeeHolidays.filter { it.status == "APPROVED" }
                .sumOf { 
                    val days = java.time.temporal.ChronoUnit.DAYS.between(it.startDate, it.endDate) + 1
                    days.toInt()
                }
            val pendingDays = employeeHolidays.filter { it.status == "PENDING" || it.status == "UNDEFINED" }
                .sumOf { 
                    val days = java.time.temporal.ChronoUnit.DAYS.between(it.startDate, it.endDate) + 1
                    days.toInt()
                }
            
            ch.baunex.timetracking.dto.EmployeeAbsenceStatsDTO(
                employee = employee,
                totalHolidays = employeeHolidays.size,
                approvedDays = approvedDays,
                pendingDays = pendingDays,
                remainingDays = (employee.vacationDays - approvedDays)
            )
        }
        
        val page = Templates.absences(
            activeMenu = "timetracking",
            activeSubMenu = "absences",
            holidaysJson = json.encodeToString(allHolidays),
            employeesJson = json.encodeToString(employees),
            pendingHolidaysJson = json.encodeToString(pendingHolidays),
            approvedHolidaysJson = json.encodeToString(approvedHolidays),
            rejectedHolidaysJson = json.encodeToString(rejectedHolidays),
            employeeStatsJson = json.encodeToString(employeeStats),
            publicHolidaysJson = json.encodeToString(publicHolidays),
            currentYear = currentYear
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
        val page = Templates.form(
            entryJson        = entry?.let { json.encodeToString(it) } ?: "",
            employeesJson    = json.encodeToString(emps),
            projectsJson     = json.encodeToString(projs),
            catalogItemsJson = json.encodeToString(cats),
            categoriesJson   = json.encodeToString(categories),
            currentDate      = today(),
            activeMenu       = "timetracking",
            activeSubMenu    = "form"
        )
        return Response.ok(page.render()).build()
    }

    //─── JSON API ────────────────────────────────────────────────────────────────
    @GET
    @Path("/api/{id}") @Produces(MediaType.APPLICATION_JSON)
    fun getApi(@PathParam("id") id: Long): Response {
        val entry = timeTrackingFacade.getTimeEntryById(id)
        return if (entry != null) {
            Response.ok(entry).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @POST
    @Path("/api") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    fun createApi(dto: TimeEntryDTO): TimeEntryDTO {
        log.info("Creating new time entry for employee ${dto.employeeId} on project ${dto.projectId}")
        return try {
            timeTrackingFacade.logTime(dto)
        } catch (e: Exception) {
            log.error("Failed to create time entry", e)
            throw e
        }
    }

    @PUT
    @Path("/api/{id}") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    fun updateApi(@PathParam("id") id: Long, dto: TimeEntryDTO): TimeEntryDTO? {
        log.info("Updating time entry $id for employee ${dto.employeeId}")
        return try {
            timeTrackingFacade.updateTimeEntry(id, dto)
        } catch (e: Exception) {
            log.error("Failed to update time entry $id", e)
            throw e
        }
    }

    @POST
    @Path("/api/{id}/approve")
    fun approveApi(@PathParam ("id") id: Long): Response {
        log.info("Approving time entry $id")
        return try {
            val adminId = employeeFacade.findByRole(Role.ADMIN).id
            timeTrackingFacade.approveEntry(id, adminId)
            Response.noContent().build()
        } catch (e: Exception) {
            log.error("Failed to approve time entry $id", e)
            throw e
        }
    }

    @POST
    @Path("/api/weekly/approve")
    @Consumes(MediaType.APPLICATION_JSON)
    fun approveWeeklyApi(request: WeeklyApprovalRequest): Response {
        val adminId = employeeFacade.findByRole(Role.ADMIN).id
        return if (timeTrackingFacade.approveWeeklyEntries(request.employeeId, request.from, request.to, adminId))
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
            val errorResponse = ErrorResponseDTO.create(
                error = "Datei fehlt",
                type = "MissingFileError"
            )
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse).build()
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
            val errorResponse = ErrorResponseDTO.create(
                error = "Fehler beim Hochladen der Datei",
                type = "UploadError",
                details = e.message
            )
            Response.serverError()
                .entity(errorResponse).build()
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

    @GET
    @Path("/api/holidays/conflicts")
    @Produces(MediaType.APPLICATION_JSON)
    fun checkHolidayConflicts(
        @QueryParam("employeeId") employeeId: Long,
        @QueryParam("startDate") startDate: String,
        @QueryParam("endDate") endDate: String
    ): ch.baunex.timetracking.dto.HolidayConflictDTO {
        log.info("Checking holiday conflicts for employee $employeeId from $startDate to $endDate")
        return try {
            val start = LocalDate.parse(startDate)
            val end = LocalDate.parse(endDate)
            log.info("Parsed dates: start=$start, end=$end")
            val result = holidayFacade.getHolidayConflicts(employeeId, start, end)
            log.info("Found ${result.conflictingHolidays.size} conflicts")
            result
        } catch (e: Exception) {
            log.error("Error checking holiday conflicts", e)
            throw e
        }
    }

    @POST
    @Path("/api/holidays/{id}/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    fun cancelHoliday(@PathParam("id") holidayId: Long): Response {
        val canceled = holidayFacade.cancelHoliday(holidayId)
        return if (canceled != null) Response.ok(canceled).build()
        else Response.status(Response.Status.NOT_FOUND).build()
    }

    @POST
    @Path("/api/holidays/{id}/approve")
    @Consumes(MediaType.APPLICATION_JSON)
    fun approveHoliday(
        @PathParam("id") holidayId: Long,
        dto: HolidayApprovalDTO
    ): Response {
        val updated = holidayFacade.approveHoliday(holidayId, dto)
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
    ): ExpectedHoursDTO {
        val localDate = LocalDate.parse(date)
        val expectedHours = workSummaryService.calculateExpectedHours(employeeId, localDate)
        return ExpectedHoursDTO(expectedHours)
    }

    @GET
    @Path("/api/summary/default-workday-hours")
    @Produces(MediaType.APPLICATION_JSON)
    fun getDefaultWorkdayHoursForEmployee(
        @QueryParam("employeeId") employeeId: Long
    ): Response {
        val defaultHours = workSummaryService.getDefaultWorkdayHoursForEmployee(employeeId)
        return Response.ok(mapOf("defaultWorkdayHours" to defaultHours)).build()
    }

    @GET
    @Path("/api/summary/monthly-account")
    @Produces(MediaType.APPLICATION_JSON)
    fun getMonthlyHoursAccount(
        @QueryParam("employeeId") employeeId: Long,
        @QueryParam("year") year: Int
    ): Response {
        val account = workSummaryService.getMonthlyHoursAccount(employeeId, year)
        return if (account != null) {
            Response.ok(account).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @GET
    @Path("/api/summary/cumulative-up-to-week")
    @Produces(MediaType.APPLICATION_JSON)
    fun getCumulativeHoursAccountUpToWeek(
        @QueryParam("employeeId") employeeId: Long,
        @QueryParam("year") year: Int,
        @QueryParam("week") week: Int
    ): Response {
        val account = workSummaryService.getCumulativeHoursAccountUpToWeek(employeeId, year, week)
        return if (account != null) {
            Response.ok(account).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }

}
