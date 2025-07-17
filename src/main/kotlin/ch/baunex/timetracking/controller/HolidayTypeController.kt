package ch.baunex.timetracking.controller

import ch.baunex.timetracking.dto.HolidayTypeDTO
import ch.baunex.timetracking.dto.HolidayTypeCreateDTO
import ch.baunex.timetracking.dto.HolidayTypeUpdateDTO
import ch.baunex.timetracking.dto.HolidayTypeListDTO
import ch.baunex.timetracking.facade.HolidayTypeFacade
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.logging.Logger

@Path("/api/holiday-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class HolidayTypeController @Inject constructor(
    private val holidayTypeFacade: HolidayTypeFacade
) {
    
    private val log = Logger.getLogger(HolidayTypeController::class.java)

    @GET
    fun getActiveHolidayTypes(): List<HolidayTypeDTO> {
        log.info("Fetching active holiday types")
        return try {
            val holidayTypes = holidayTypeFacade.getActiveHolidayTypes()
            log.info("Successfully fetched ${holidayTypes.size} active holiday types")
            holidayTypes
        } catch (e: Exception) {
            log.error("Failed to fetch active holiday types", e)
            throw e
        }
    }

    @GET
    @Path("/all")
    fun getAllHolidayTypes(): List<HolidayTypeDTO> {
        log.info("Fetching all holiday types")
        return try {
            val holidayTypes = holidayTypeFacade.getAllHolidayTypes()
            log.info("Successfully fetched ${holidayTypes.size} holiday types")
            holidayTypes
        } catch (e: Exception) {
            log.error("Failed to fetch all holiday types", e)
            throw e
        }
    }

    @GET
    @Path("/{id}")
    fun getHolidayTypeById(@PathParam("id") id: Long): Response {
        log.info("Fetching holiday type by ID: $id")
        return try {
            val holidayType = holidayTypeFacade.getHolidayTypeById(id)
            if (holidayType != null) {
                log.info("Successfully fetched holiday type with ID: $id")
                Response.ok(holidayType).build()
            } else {
                log.warn("Holiday type with ID $id not found")
                Response.status(Response.Status.NOT_FOUND).build()
            }
        } catch (e: Exception) {
            log.error("Failed to fetch holiday type with ID: $id", e)
            throw e
        }
    }

    @GET
    @Path("/code/{code}")
    fun getHolidayTypeByCode(@PathParam("code") code: String): Response {
        log.info("Fetching holiday type by code: $code")
        return try {
            val holidayType = holidayTypeFacade.getHolidayTypeByCode(code)
            if (holidayType != null) {
                log.info("Successfully fetched holiday type with code: $code")
                Response.ok(holidayType).build()
            } else {
                log.warn("Holiday type with code $code not found")
                Response.status(Response.Status.NOT_FOUND).build()
            }
        } catch (e: Exception) {
            log.error("Failed to fetch holiday type with code: $code", e)
            throw e
        }
    }

    @GET
    @Path("/expected-hours/{code}")
    fun getExpectedHoursForHolidayType(@PathParam("code") code: String): Response {
        log.info("Fetching expected hours for holiday type code: $code")
        return try {
            val expectedHours = holidayTypeFacade.getExpectedHoursForHolidayType(code)
            log.info("Successfully fetched expected hours ($expectedHours) for holiday type code: $code")
            Response.ok(mapOf("expectedHours" to expectedHours)).build()
        } catch (e: Exception) {
            log.error("Failed to fetch expected hours for holiday type code: $code", e)
            throw e
        }
    }

    @GET
    @Path("/default-workday-hours")
    fun getDefaultWorkdayHours(): Response {
        log.info("Fetching default workday hours")
        return try {
            val defaultHours = holidayTypeFacade.getDefaultWorkdayHours()
            log.info("Successfully fetched default workday hours: $defaultHours")
            Response.ok(mapOf("defaultWorkdayHours" to defaultHours)).build()
        } catch (e: Exception) {
            log.error("Failed to fetch default workday hours", e)
            throw e
        }
    }

    @POST
    fun createHolidayType(dto: HolidayTypeCreateDTO): Response {
        log.info("Creating new holiday type with code: ${dto.code}, displayName: ${dto.displayName}")
        return try {
            val created = holidayTypeFacade.createHolidayType(dto)
            log.info("Successfully created holiday type with ID: ${created.id}")
            Response.status(Response.Status.CREATED).entity(created).build()
        } catch (e: Exception) {
            log.error("Failed to create holiday type with code: ${dto.code}", e)
            throw e
        }
    }

    @PUT
    @Path("/{id}")
    fun updateHolidayType(@PathParam("id") id: Long, dto: HolidayTypeUpdateDTO): Response {
        log.info("Updating holiday type with ID: $id, displayName: ${dto.displayName}")
        return try {
            val updated = holidayTypeFacade.updateHolidayType(id, dto)
            if (updated != null) {
                log.info("Successfully updated holiday type with ID: $id")
                Response.ok(updated).build()
            } else {
                log.warn("Holiday type with ID $id not found for update")
                Response.status(Response.Status.NOT_FOUND).build()
            }
        } catch (e: Exception) {
            log.error("Failed to update holiday type with ID: $id", e)
            throw e
        }
    }

    @DELETE
    @Path("/{id}")
    fun deleteHolidayType(@PathParam("id") id: Long): Response {
        log.info("Deleting holiday type with ID: $id")
        return try {
            val deleted = holidayTypeFacade.deleteHolidayType(id)
            if (deleted) {
                log.info("Successfully deleted holiday type with ID: $id")
                Response.ok().build()
            } else {
                log.warn("Holiday type with ID $id not found for deletion")
                Response.status(Response.Status.NOT_FOUND).build()
            }
        } catch (e: Exception) {
            log.error("Failed to delete holiday type with ID: $id", e)
            throw e
        }
    }

    @POST
    @Path("/{id}/activate")
    fun activateHolidayType(@PathParam("id") id: Long): Response {
        log.info("Activating holiday type with ID: $id")
        return try {
            val activated = holidayTypeFacade.activateHolidayType(id)
            if (activated != null) {
                log.info("Successfully activated holiday type with ID: $id")
                Response.ok(activated).build()
            } else {
                log.warn("Holiday type with ID $id not found for activation")
                Response.status(Response.Status.NOT_FOUND).build()
            }
        } catch (e: Exception) {
            log.error("Failed to activate holiday type with ID: $id", e)
            throw e
        }
    }

    @POST
    @Path("/{id}/deactivate")
    fun deactivateHolidayType(@PathParam("id") id: Long): Response {
        log.info("Deactivating holiday type with ID: $id")
        return try {
            val deactivated = holidayTypeFacade.deactivateHolidayType(id)
            if (deactivated != null) {
                log.info("Successfully deactivated holiday type with ID: $id")
                Response.ok(deactivated).build()
            } else {
                log.warn("Holiday type with ID $id not found for deactivation")
                Response.status(Response.Status.NOT_FOUND).build()
            }
        } catch (e: Exception) {
            log.error("Failed to deactivate holiday type with ID: $id", e)
            throw e
        }
    }

    @GET
    @Path("/paginated")
    fun getHolidayTypesPaginated(
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("size") @DefaultValue("20") size: Int
    ): HolidayTypeListDTO {
        log.info("Fetching holiday types paginated (page: $page, size: $size)")
        return try {
            val result = holidayTypeFacade.getHolidayTypesPaginated(page, size)
            log.info("Successfully fetched ${result.holidayTypes.size} holiday types (page: $page, total: ${result.totalCount})")
            result
        } catch (e: Exception) {
            log.error("Failed to fetch holiday types paginated (page: $page, size: $size)", e)
            throw e
        }
    }
} 