package ch.baunex.timetracking.controller

import ch.baunex.catalog.facade.CatalogFacade
import ch.baunex.notes.dto.NoteCreateDto
import ch.baunex.notes.facade.NoteAttachmentFacade
import ch.baunex.notes.model.NoteCategory
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.timetracking.dto.TimeEntryCatalogItemDTO
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import ch.baunex.timetracking.service.TimeEntryCostService
import ch.baunex.user.facade.EmployeeFacade
import ch.baunex.user.model.Role
import ch.baunex.web.WebController.Templates
import org.jboss.resteasy.reactive.multipart.FileUpload
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriBuilder
import kotlinx.serialization.encodeToString
import org.jboss.logging.Logger
import org.jboss.resteasy.reactive.RestForm
import java.io.InputStream
import java.net.URI
import java.time.LocalDate
import ch.baunex.serialization.SerializationUtils.json


@Path("/timetracking")
@ApplicationScoped
class TimeTrackingRestController {

    @Inject
    lateinit var timeTrackingFacade: TimeTrackingFacade

    @Inject
    lateinit var employeeFacade: EmployeeFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var catalogFacade: CatalogFacade

    @Inject
    lateinit var timeEntryCostService: TimeEntryCostService

    @Inject
    lateinit var noteAttachmentFacade: NoteAttachmentFacade

    private val logger = Logger.getLogger(TimeTrackingRestController::class.java)

    private fun getCurrentDate(): String = LocalDate.now().toString()

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun viewList(): Response {
        val entries = timeTrackingFacade.getAllTimeEntries()
        val employees = employeeFacade.listAll()
        val projects = projectFacade.getAllProjects()
        val template = Templates.timeTracking(
            activeMenu = "timetracking",
            timeEntriesJson = json.encodeToString(entries),
            currentDate = getCurrentDate(),
            employeesJson = json.encodeToString(employees),
            projectsJson = json.encodeToString(projects),
            entryJson = ""
        )
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    fun newEntryForm(): Response {
        val employees = employeeFacade.listAll()
        val projects = projectFacade.getAllProjects()
        val catalogItems = catalogFacade.getAllItems()
        val categories = NoteCategory.values().toList()
        val template = Templates.timeTrackingForm(
            entryJson = "",
            employeesJson = json.encodeToString(employees),
            projectsJson = json.encodeToString(projects),
            currentDate = getCurrentDate(),
            activeMenu = "timetracking",
            catalogItemsJson = json.encodeToString(catalogItems),
            categoriesJson = json.encodeToString(categories)
        )
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    fun editForm(@PathParam("id") id: Long): Response {
        val entry = timeTrackingFacade.getTimeEntryById(id)
            ?: return Response.status(Response.Status.NOT_FOUND).build()
        val employees = employeeFacade.listAll()
        val projects = projectFacade.getAllProjects()
        val catalogItems = catalogFacade.getAllItems()
        val categories = NoteCategory.values().toList()
        val template = Templates.timeTrackingForm(
            entryJson = json.encodeToString(entry),
            employeesJson = json.encodeToString(employees),
            projectsJson = json.encodeToString(projects),
            currentDate = getCurrentDate(),
            activeMenu = "timetracking",
            catalogItemsJson = json.encodeToString(catalogItems),
            categoriesJson = json.encodeToString(categories)
        )
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun saveHtmlEntry(
        @FormParam("id") id: Long?,
        @FormParam("employeeId") employeeId: Long,
        @FormParam("projectId") projectId: Long,
        @FormParam("date") date: LocalDate,
        @FormParam("hoursWorked") hoursWorked: Double,
        @FormParam("title") title: String,
        @FormParam("hourlyRate") hourlyRate: Double?,
        @FormParam("notBillable") notBillable: String?,
        @FormParam("invoiced") invoiced: String?,
        @FormParam("catalogItemIds") catalogItemIds: List<String>?,
        @FormParam("catalogItemQuantities") catalogItemQuantities: List<String>?,
        @FormParam("catalogItemNames") catalogItemNames: List<String>?,
        @FormParam("catalogItemPrices") catalogItemPrices: List<String>?,
        @FormParam("hasNightSurcharge") hasNightSurcharge: String?,
        @FormParam("hasWeekendSurcharge") hasWeekendSurcharge: String?,
        @FormParam("hasHolidaySurcharge") hasHolidaySurcharge: String?,
        @FormParam("travelTimeMinutes") travelTimeMinutes: Int = 0,
        @FormParam("disposalCost") disposalCost: Double = 0.0,
        @FormParam("hasWaitingTime") hasWaitingTime: String?,
        @FormParam("waitingTimeMinutes") waitingTimeMinutes: Int = 0,
        @FormParam("noteTitle") noteTitle: String?,
        @FormParam("noteContent") noteContent: String?,
        @FormParam("noteCategory") noteCategory: String?
    ): Response {
        // map form to DTO (same logic as before)
        val catalogItems = (catalogItemIds ?: emptyList()).zip(
            catalogItemQuantities.orEmpty().zip(
                catalogItemNames.orEmpty().zip(catalogItemPrices.orEmpty())
            )
        ).map { (id, rest) ->
            val (quantity, namePrice) = rest
            val (name, price) = namePrice
            TimeEntryCatalogItemDTO(
                catalogItemId = id.toLongOrNull(),
                quantity = quantity.toIntOrNull() ?: 1,
                itemName = name,
                unitPrice = price.toDoubleOrNull() ?: 0.0,
                totalPrice = (quantity.toIntOrNull() ?: 1) * (price.toDoubleOrNull() ?: 0.0)
            )
        }

        val singleNote: NoteCreateDto? = if (!noteContent.isNullOrBlank()) {
            NoteCreateDto(
                id            = 0L,               // 0L fuer neu (wird in Service ueberschrieben)
                projectId     = projectId,        // oder null, je nachdem
                timeEntryId   = null,             // im Create‐Fall wird erst TimeEntry gespeichert
                documentId    = null,
                title         = noteTitle,
                content       = noteContent,
                category      = NoteCategory.valueOf(noteCategory ?: "INFO"),
                tags          = emptyList(),
                attachments   = emptyList()
            )
        } else {
            null
        }
        TimeEntryDTO(
            employeeId = employeeId,
            projectId = projectId,
            date = date,
            hoursWorked = hoursWorked,
            title = title,
            notes = if (singleNote != null) listOf(singleNote) else emptyList(),
            hourlyRate = hourlyRate,
            billable = notBillable != "true",
            invoiced = invoiced == "true",
            catalogItems = catalogItems,
            hasNightSurcharge = hasNightSurcharge == "true",
            hasWeekendSurcharge = hasWeekendSurcharge == "true",
            hasHolidaySurcharge = hasHolidaySurcharge == "true",
            travelTimeMinutes = travelTimeMinutes,
            disposalCost = disposalCost,
            hasWaitingTime = hasWaitingTime == "true",
            waitingTimeMinutes = waitingTimeMinutes,
        ) // placeholder


        // assemble TimeEntryDTO and call facade
        val dto = TimeEntryDTO(
            employeeId = employeeId,
            projectId = projectId,
            date = date,
            hoursWorked = hoursWorked,
            title = title,
            notes = emptyList(),
            hourlyRate = hourlyRate,
            billable = notBillable != "true",
            invoiced = invoiced == "true",
            catalogItems = catalogItems,
            hasNightSurcharge = hasNightSurcharge == "true",
            hasWeekendSurcharge = hasWeekendSurcharge == "true",
            hasHolidaySurcharge = hasHolidaySurcharge == "true",
            travelTimeMinutes = travelTimeMinutes,
            disposalCost = disposalCost,
            hasWaitingTime = hasWaitingTime == "true",
            waitingTimeMinutes = waitingTimeMinutes
        )
        if (id == null) timeTrackingFacade.logTime(dto)
        else timeTrackingFacade.updateTimeEntry(id, dto)

        return Response.seeOther(UriBuilder.fromPath("/timetracking").build()).build()
    }

    @GET
    @Path("/{id}/delete")
    fun deleteHtmlEntry(@PathParam("id") id: Long): Response {
        timeTrackingFacade.deleteTimeEntry(id)
        return Response.seeOther(URI.create("/timetracking")).build()
    }

    @POST
    @Path("/{id}/approve")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun approveHtmlEntry(@PathParam("id") id: Long): Response {
        val approverId = employeeFacade.findByRole(Role.ADMIN).id
        timeTrackingFacade.approveEntry(id, approverId)
        return Response.ok().build()
    }

    // REST Endpoints remain unchanged

    @GET
    @Path("/api/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getJson(@PathParam("id") id: Long): Response {
        val entry = timeTrackingFacade.getTimeEntryById(id)
        return if (entry != null) Response.ok(entry).build() else Response.status(Response.Status.NOT_FOUND).build()
    }

    @GET
    @Path("/api/list")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAllJson(): List<TimeEntryResponseDTO> = timeTrackingFacade.getAllTimeEntries()

    @POST
    @Path("/api/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun saveJson(entry: TimeEntryDTO): TimeEntryResponseDTO {
        // 1) Persist entry (and its notes) first
        val maybeSaved = if (entry.id == null) {
            timeTrackingFacade.logTime(entry)
        } else {
            timeTrackingFacade.updateTimeEntry(entry.id, entry)
        }
        val saved = maybeSaved ?: throw WebApplicationException("Failed to save entry", 500)

        // 2) Link attachments: requestNote.attachments is List<Long>
        entry.notes.forEachIndexed { idx, requestNote ->
            if (requestNote.attachments.isNotEmpty()) {
                // Find the matching saved note by index
                saved.notes.getOrNull(idx)?.let { savedNote ->
                    noteAttachmentFacade.linkAttachments(
                        noteId = savedNote.id,
                        attachmentIds = requestNote.attachments
                    )
                }
            }
        }

        return saved
    }

    @POST
    @Path("/api/{id}/delete")
    @Produces(MediaType.APPLICATION_JSON)
    fun deleteJson(@PathParam("id") id: Long): Response = try {
        timeTrackingFacade.deleteTimeEntry(id)
        Response.ok().build()
    } catch (e: Exception) {
        logger.error("Error deleting time entry", e)
        Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.message).build()
    }

    @POST
    @Path("/api/{id}/approve")
    @Produces(MediaType.APPLICATION_JSON)
    fun approveJson(@PathParam("id") id: Long): Response = try {
        val approverId = employeeFacade.findByRole(Role.ADMIN).id
        val success = timeTrackingFacade.approveEntry(id, approverId)
        if (success) Response.ok().build() else Response.status(Response.Status.NOT_FOUND).build()
    } catch (e: Exception) {
        logger.error("Error approving time entry", e)
        Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.message).build()
    }

    @POST
    @Path("/api/upload/note-attachment")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    fun uploadAttachment(
        @FormParam("noteId") noteId: Long,
        @RestForm("file") fileStream: InputStream,
        @RestForm("file") fileDetails: FileUpload?
    ): Response {
        if (fileDetails == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "No file uploaded"))
                .build()
        }

        return try {
            val dto = noteAttachmentFacade.uploadAttachment(
                noteId,
                fileStream,
                fileDetails.fileName()
            )
            Response.ok(dto).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.NOT_FOUND).build()
        } catch (e: Exception) {
            Response.serverError()
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @DELETE
    @Path("/api/note-attachment/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun deleteAttachment(@PathParam("id") id: Long): Response {
        noteAttachmentFacade.deleteAttachment(id)
        return Response.ok().build()
    }

    @GET
    @Path("/api/employees")
    @Produces(MediaType.APPLICATION_JSON)
    fun getEmployees() = employeeFacade.listAll()

    @GET
    @Path("/api/projects")
    @Produces(MediaType.APPLICATION_JSON)
    fun getProjects() = projectFacade.getAllProjects()

    @GET
    @Path("/api/catalog-items")
    @Produces(MediaType.APPLICATION_JSON)
    fun getCatalogItems() = catalogFacade.getAllItems()

    @GET
    @Path("/api/note-categories")
    @Produces(MediaType.APPLICATION_JSON)
    fun getNoteCategories() = NoteCategory.values().toList()
}