package ch.baunex.timetracking.controller

import ch.baunex.timetracking.dto.HolidayDefinitionDTO
import ch.baunex.timetracking.dto.HolidayDefinitionListDTO
import ch.baunex.timetracking.facade.HolidayDefinitionFacade
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate
import org.jboss.logging.Logger

@Path("/timetracking/api/holiday-definitions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class HolidayDefinitionController @Inject constructor(
    private val holidayDefinitionFacade: HolidayDefinitionFacade
) {
    
    private val log = Logger.getLogger(HolidayDefinitionController::class.java)

    @GET
    @Path("/year/{year}")
    fun getHolidaysForYear(@PathParam("year") year: Int): Response {
        log.info("Fetching holiday definitions for year: $year")
        return try {
            val holidays = holidayDefinitionFacade.getHolidaysForYear(year)
            log.info("Successfully fetched ${holidays.size} holiday definitions for year: $year")
            Response.ok(HolidayDefinitionListDTO(holidays)).build()
        } catch (e: Exception) {
            log.error("Failed to fetch holiday definitions for year: $year", e)
            throw e
        }
    }

    @GET
    @Path("/date-range")
    fun getHolidaysForDateRange(
        @QueryParam("startDate") startDate: String,
        @QueryParam("endDate") endDate: String
    ): Response {
        log.info("Fetching holiday definitions for date range: $startDate to $endDate")
        return try {
            val start = LocalDate.parse(startDate)
            val end = LocalDate.parse(endDate)
            val holidays = holidayDefinitionFacade.getHolidaysForDateRange(start, end)
            log.info("Successfully fetched ${holidays.size} holiday definitions for date range: $startDate to $endDate")
            Response.ok(HolidayDefinitionListDTO(holidays)).build()
        } catch (e: Exception) {
            log.error("Failed to fetch holiday definitions for date range: $startDate to $endDate", e)
            throw e
        }
    }

    @POST
    fun createHoliday(holiday: HolidayDefinitionDTO): Response {
        log.info("Creating holiday definition: ${holiday.name} for date: ${holiday.date}")
        return try {
            val createdHoliday = holidayDefinitionFacade.createHoliday(holiday)
            log.info("Successfully created holiday definition with ID: ${createdHoliday.id}")
            Response.status(Response.Status.CREATED).entity(createdHoliday).build()
        } catch (e: Exception) {
            log.error("Failed to create holiday definition: ${holiday.name}", e)
            throw e
        }
    }

    @PUT
    @Path("/{id}")
    fun updateHoliday(
        @PathParam("id") id: Long,
        holiday: HolidayDefinitionDTO
    ): Response {
        log.info("Updating holiday definition with ID: $id, name: ${holiday.name}")
        return try {
            val updatedHoliday = holidayDefinitionFacade.updateHoliday(id, holiday)
            if (updatedHoliday != null) {
                log.info("Successfully updated holiday definition with ID: $id")
                Response.ok(updatedHoliday).build()
            } else {
                log.warn("Holiday definition with ID $id not found for update")
                Response.status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Holiday not found"))
                    .build()
            }
        } catch (e: Exception) {
            log.error("Failed to update holiday definition with ID: $id", e)
            throw e
        }
    }

    @DELETE
    @Path("/{id}")
    fun deleteHoliday(@PathParam("id") id: Long): Response {
        log.info("Deleting holiday definition with ID: $id")
        return try {
            val deleted = holidayDefinitionFacade.deleteHoliday(id)
            if (deleted) {
                log.info("Successfully deleted holiday definition with ID: $id")
                Response.noContent().build()
            } else {
                log.warn("Holiday definition with ID $id not found for deletion")
                Response.status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Holiday not found"))
                    .build()
            }
        } catch (e: Exception) {
            log.error("Failed to delete holiday definition with ID: $id", e)
            throw e
        }
    }

    @GET
    @Path("/is-holiday")
    fun isHoliday(@QueryParam("date") date: String): Response {
        log.info("Checking if date is holiday: $date")
        return try {
            val checkDate = LocalDate.parse(date)
            val isHoliday = holidayDefinitionFacade.isHoliday(checkDate)
            log.info("Date $date is holiday: $isHoliday")
            Response.ok(mapOf("isHoliday" to isHoliday)).build()
        } catch (e: Exception) {
            log.error("Failed to check if date is holiday: $date", e)
            throw e
        }
    }

    @GET
    @Path("/working-days")
    fun calculateWorkingDays(
        @QueryParam("startDate") startDate: String,
        @QueryParam("endDate") endDate: String
    ): Response {
        log.info("Calculating working days from $startDate to $endDate")
        return try {
            val start = LocalDate.parse(startDate)
            val end = LocalDate.parse(endDate)
            val workingDays = holidayDefinitionFacade.calculateWorkingDays(start, end)
            log.info("Calculated $workingDays working days from $startDate to $endDate")
            Response.ok(mapOf("workingDays" to workingDays)).build()
        } catch (e: Exception) {
            log.error("Failed to calculate working days from $startDate to $endDate", e)
            throw e
        }
    }

    @POST
    @Path("/generate/{year}")
    fun generateHolidaysForYear(@PathParam("year") year: Int): Response {
        log.info("Generating holiday definitions for year: $year")
        return try {
            holidayDefinitionFacade.generateHolidaysForYear(year)
            log.info("Successfully generated holiday definitions for year: $year")
            Response.ok(mapOf("message" to "Holidays generated successfully for year $year")).build()
        } catch (e: Exception) {
            log.error("Failed to generate holiday definitions for year: $year", e)
            throw e
        }
    }
} 