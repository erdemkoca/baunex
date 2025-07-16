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

@Path("/api/holiday-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class HolidayTypeController @Inject constructor(
    private val holidayTypeFacade: HolidayTypeFacade
) {

    @GET
    fun getActiveHolidayTypes(): List<HolidayTypeDTO> {
        return holidayTypeFacade.getActiveHolidayTypes()
    }

    @GET
    @Path("/all")
    fun getAllHolidayTypes(): List<HolidayTypeDTO> {
        return holidayTypeFacade.getAllHolidayTypes()
    }

    @GET
    @Path("/{id}")
    fun getHolidayTypeById(@PathParam("id") id: Long): Response {
        val holidayType = holidayTypeFacade.getHolidayTypeById(id)
        return if (holidayType != null) {
            Response.ok(holidayType).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @GET
    @Path("/code/{code}")
    fun getHolidayTypeByCode(@PathParam("code") code: String): Response {
        val holidayType = holidayTypeFacade.getHolidayTypeByCode(code)
        return if (holidayType != null) {
            Response.ok(holidayType).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @GET
    @Path("/expected-hours/{code}")
    fun getExpectedHoursForHolidayType(@PathParam("code") code: String): Response {
        val expectedHours = holidayTypeFacade.getExpectedHoursForHolidayType(code)
        return Response.ok(mapOf("expectedHours" to expectedHours)).build()
    }

    @GET
    @Path("/default-workday-hours")
    fun getDefaultWorkdayHours(): Response {
        val defaultHours = holidayTypeFacade.getDefaultWorkdayHours()
        return Response.ok(mapOf("defaultWorkdayHours" to defaultHours)).build()
    }

    @POST
    fun createHolidayType(dto: HolidayTypeCreateDTO): Response {
        return try {
            val created = holidayTypeFacade.createHolidayType(dto)
            Response.status(Response.Status.CREATED).entity(created).build()
        } catch (e: WebApplicationException) {
            Response.status(e.response.status).entity(mapOf("error" to e.message)).build()
        }
    }

    @PUT
    @Path("/{id}")
    fun updateHolidayType(@PathParam("id") id: Long, dto: HolidayTypeUpdateDTO): Response {
        return try {
            val updated = holidayTypeFacade.updateHolidayType(id, dto)
            if (updated != null) {
                Response.ok(updated).build()
            } else {
                Response.status(Response.Status.NOT_FOUND).build()
            }
        } catch (e: WebApplicationException) {
            Response.status(e.response.status).entity(mapOf("error" to e.message)).build()
        }
    }

    @DELETE
    @Path("/{id}")
    fun deleteHolidayType(@PathParam("id") id: Long): Response {
        return try {
            val deleted = holidayTypeFacade.deleteHolidayType(id)
            if (deleted) {
                Response.ok().build()
            } else {
                Response.status(Response.Status.NOT_FOUND).build()
            }
        } catch (e: WebApplicationException) {
            Response.status(e.response.status).entity(mapOf("error" to e.message)).build()
        }
    }

    @POST
    @Path("/{id}/activate")
    fun activateHolidayType(@PathParam("id") id: Long): Response {
        return try {
            val activated = holidayTypeFacade.activateHolidayType(id)
            if (activated != null) {
                Response.ok(activated).build()
            } else {
                Response.status(Response.Status.NOT_FOUND).build()
            }
        } catch (e: WebApplicationException) {
            Response.status(e.response.status).entity(mapOf("error" to e.message)).build()
        }
    }

    @POST
    @Path("/{id}/deactivate")
    fun deactivateHolidayType(@PathParam("id") id: Long): Response {
        return try {
            val deactivated = holidayTypeFacade.deactivateHolidayType(id)
            if (deactivated != null) {
                Response.ok(deactivated).build()
            } else {
                Response.status(Response.Status.NOT_FOUND).build()
            }
        } catch (e: WebApplicationException) {
            Response.status(e.response.status).entity(mapOf("error" to e.message)).build()
        }
    }

    @GET
    @Path("/paginated")
    fun getHolidayTypesPaginated(
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("size") @DefaultValue("20") size: Int
    ): HolidayTypeListDTO {
        return holidayTypeFacade.getHolidayTypesPaginated(page, size)
    }
} 