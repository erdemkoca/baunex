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
import java.net.URI
import java.time.LocalDate
import ch.baunex.serialization.SerializationUtils.json


@Path("/timetracking")
@ApplicationScoped
class TimeTrackingController {

    @Inject
    lateinit var timeTrackingFacade: TimeTrackingFacade

    @Inject
    lateinit var employeeFacade: EmployeeFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var catalogFacade: CatalogFacade

    @Inject
    lateinit var noteAttachmentFacade: NoteAttachmentFacade

    private val logger = Logger.getLogger(TimeTrackingController::class.java)

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
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    fun form(@PathParam("id") id: Long): Response {
        val entry = if (id == 0L) null else timeTrackingFacade.getTimeEntryById(id)

        val employees = employeeFacade.listAll()
        val projects = projectFacade.getAllProjects()
        val catalogItems = catalogFacade.getAllItems()
        val categories = NoteCategory.values().toList()

        val template = Templates.timeTrackingForm(
            entryJson = if (entry != null) json.encodeToString(entry) else "",
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
    @Consumes(MediaType.APPLICATION_JSON)
    fun saveEntry(dto: TimeEntryDTO): Response {
        val maybeSaved = if (dto.id == null || dto.id == 0L) {
            timeTrackingFacade.logTime(dto)
        } else {
            timeTrackingFacade.updateTimeEntry(dto.id, dto)
        }

        if (maybeSaved == null) {
            throw WebApplicationException("Failed to save time entry", 500)
        }

        return Response.ok(maybeSaved).build()
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
    fun getAllJson(): List<TimeEntryDTO> = timeTrackingFacade.getAllTimeEntries()

    @POST
    @Path("/api/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun saveJson(dto: TimeEntryDTO): TimeEntryDTO {
        val saved: TimeEntryDTO = if (dto.id == null || dto.id == 0L) {
            timeTrackingFacade.logTime(dto)
        } else {
            timeTrackingFacade.updateTimeEntry(dto.id, dto)
        } ?: throw WebApplicationException("Failed to save entry", 500)

        // Attachment-Linking jetzt über saved.timeEntry.notes
        dto.notes.forEachIndexed { idx, noteDto ->
            if (noteDto.attachments.isNotEmpty()) {
                saved.notes.getOrNull(idx)?.let { savedNote ->
                    noteAttachmentFacade.linkAttachments(
                        noteId         = savedNote.id!!,
                        attachmentIds  = noteDto.attachments.map { it.id }
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