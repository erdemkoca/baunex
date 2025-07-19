package ch.baunex.web

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate

@Path("/settings")
class SettingsController {

    @CheckedTemplate(basePath = "settings", requireTypeSafeExpressions = false)
    object Templates {
        @JvmStatic
        external fun settings(
            activeMenu: String,
            currentDate: LocalDate,
            activeSubMenu: String
        ): TemplateInstance

        @JvmStatic
        external fun definitions(
            activeMenu: String,
            currentDate: LocalDate,
            activeSubMenu: String
        ): TemplateInstance
    }

    @GET
    @Path("/holiday-types")
    @Produces(MediaType.TEXT_HTML)
    fun getHolidayTypeSettingsPage(): Response {
        val currentDate = LocalDate.now()
        
        val template = Templates.settings(
            activeMenu = "settings",
            currentDate = currentDate,
            activeSubMenu = "holiday-types"
        )

        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/holidays")
    @Produces(MediaType.TEXT_HTML)
    fun getHolidayDefinitionsPage(): Response {
        val currentDate = LocalDate.now()
        
        val template = Templates.definitions(
            activeMenu = "settings",
            currentDate = currentDate,
            activeSubMenu = "holidays"
        )

        return Response.ok(template.render()).build()
    }
} 