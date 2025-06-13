package ch.baunex.timetracking.controller

import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/time-tracking")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class TimeTrackingController @Inject constructor(
    private val facade: TimeTrackingFacade
) {

    @POST
    @Transactional
    fun logTime(entry: TimeEntryDTO): Response {
        val created = facade.logTime(entry)
        return Response.status(Response.Status.CREATED).entity(created).build()
    }

    @GET
    fun getAll(): List<TimeEntryResponseDTO> = facade.getAllTimeEntries()

    @GET
    @Path("/{id}")
    fun getById(@PathParam("id") id: Long): Response {
        val entry = facade.getTimeEntryById(id)
            ?: return Response.status(Response.Status.NOT_FOUND).build()
        return Response.ok(entry).build()
    }

    @PUT
    @Path("/{id}")
    @Transactional
    fun update(@PathParam("id") id: Long, updatedEntry: TimeEntryDTO): Response {
        val updated = facade.updateTimeEntry(id, updatedEntry)
            ?: return Response.status(Response.Status.NOT_FOUND).build()
        return Response.ok(updated).build()
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    fun delete(@PathParam("id") id: Long): Response {
        val deleted = facade.deleteTimeEntry(id)
        return if (deleted) Response.noContent().build()
        else Response.status(Response.Status.NOT_FOUND).build()
    }
}
