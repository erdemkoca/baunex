package ch.baunex.company.controller

import ch.baunex.company.service.CompanySettingsService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/company-settings")
@ApplicationScoped
class CompanySettingsController {

    @Inject
    lateinit var companySettingsService: CompanySettingsService

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getCompanySettings(): Response {
        val settings = companySettingsService.getCompanySettings()
        val jsonResponse = """
            {
                "plannedWeeklyHours": ${settings.plannedWeeklyHours},
                "defaultWorkdaysPerWeek": ${settings.defaultWorkdaysPerWeek}
            }
        """.trimIndent()
        return Response.ok(jsonResponse).build()
    }

    @GET
    @Path("/planned-weekly-hours")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPlannedWeeklyHours(): Response {
        val hours = companySettingsService.getPlannedWeeklyHours()
        return Response.ok(mapOf("plannedWeeklyHours" to hours)).build()
    }

    @GET
    @Path("/default-workdays-per-week")
    @Produces(MediaType.APPLICATION_JSON)
    fun getDefaultWorkdaysPerWeek(): Response {
        val days = companySettingsService.getDefaultWorkdaysPerWeek()
        return Response.ok(mapOf("defaultWorkdaysPerWeek" to days)).build()
    }

    @PUT
    @Path("/planned-weekly-hours")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updatePlannedWeeklyHours(request: Map<String, Double>): Response {
        val hours = request["plannedWeeklyHours"] ?: return Response.status(400).entity("plannedWeeklyHours is required").build()
        val settings = companySettingsService.updatePlannedWeeklyHours(hours)
        return Response.ok(mapOf("plannedWeeklyHours" to settings.plannedWeeklyHours)).build()
    }

    @PUT
    @Path("/default-workdays-per-week")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateDefaultWorkdaysPerWeek(request: Map<String, Int>): Response {
        val days = request["defaultWorkdaysPerWeek"] ?: return Response.status(400).entity("defaultWorkdaysPerWeek is required").build()
        val settings = companySettingsService.updateDefaultWorkdaysPerWeek(days)
        return Response.ok(mapOf("defaultWorkdaysPerWeek" to settings.defaultWorkdaysPerWeek)).build()
    }
} 