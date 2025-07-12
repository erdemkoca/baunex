package ch.baunex.timetracking.controller

import ch.baunex.timetracking.dto.HolidayDefinitionDTO
import ch.baunex.timetracking.dto.HolidayDefinitionListDTO
import ch.baunex.timetracking.facade.HolidayDefinitionFacade
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate

@Path("/timetracking/api/holiday-definitions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class HolidayDefinitionController @Inject constructor(
    private val holidayDefinitionFacade: HolidayDefinitionFacade
) {

    @GET
    @Path("/year/{year}")
    fun getHolidaysForYear(@PathParam("year") year: Int): Response {
        return try {
            val holidays = holidayDefinitionFacade.getHolidaysForYear(year)
            Response.ok(HolidayDefinitionListDTO(holidays)).build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @GET
    @Path("/date-range")
    fun getHolidaysForDateRange(
        @QueryParam("startDate") startDate: String,
        @QueryParam("endDate") endDate: String
    ): Response {
        return try {
            val start = LocalDate.parse(startDate)
            val end = LocalDate.parse(endDate)
            val holidays = holidayDefinitionFacade.getHolidaysForDateRange(start, end)
            Response.ok(HolidayDefinitionListDTO(holidays)).build()
        } catch (e: Exception) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid date format. Use YYYY-MM-DD"))
                .build()
        }
    }

    @POST
    fun createHoliday(holiday: HolidayDefinitionDTO): Response {
        return try {
            val createdHoliday = holidayDefinitionFacade.createHoliday(holiday)
            Response.status(Response.Status.CREATED).entity(createdHoliday).build()
        } catch (e: Exception) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @PUT
    @Path("/{id}")
    fun updateHoliday(
        @PathParam("id") id: Long,
        holiday: HolidayDefinitionDTO
    ): Response {
        return try {
            val updatedHoliday = holidayDefinitionFacade.updateHoliday(id, holiday)
            if (updatedHoliday != null) {
                Response.ok(updatedHoliday).build()
            } else {
                Response.status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Holiday not found"))
                    .build()
            }
        } catch (e: Exception) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @DELETE
    @Path("/{id}")
    fun deleteHoliday(@PathParam("id") id: Long): Response {
        return try {
            val deleted = holidayDefinitionFacade.deleteHoliday(id)
            if (deleted) {
                Response.noContent().build()
            } else {
                Response.status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Holiday not found"))
                    .build()
            }
        } catch (e: Exception) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @GET
    @Path("/is-holiday")
    fun isHoliday(@QueryParam("date") date: String): Response {
        return try {
            val checkDate = LocalDate.parse(date)
            val isHoliday = holidayDefinitionFacade.isHoliday(checkDate)
            Response.ok(mapOf("isHoliday" to isHoliday)).build()
        } catch (e: Exception) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid date format. Use YYYY-MM-DD"))
                .build()
        }
    }

    @GET
    @Path("/working-days")
    fun calculateWorkingDays(
        @QueryParam("startDate") startDate: String,
        @QueryParam("endDate") endDate: String
    ): Response {
        return try {
            val start = LocalDate.parse(startDate)
            val end = LocalDate.parse(endDate)
            val workingDays = holidayDefinitionFacade.calculateWorkingDays(start, end)
            Response.ok(mapOf("workingDays" to workingDays)).build()
        } catch (e: Exception) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid date format. Use YYYY-MM-DD"))
                .build()
        }
    }

    @POST
    @Path("/generate/{year}")
    fun generateHolidaysForYear(@PathParam("year") year: Int): Response {
        return try {
            holidayDefinitionFacade.generateHolidaysForYear(year)
            Response.ok(mapOf("message" to "Holidays generated successfully for year $year")).build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
} 