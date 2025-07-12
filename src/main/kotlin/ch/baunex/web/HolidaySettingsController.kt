package ch.baunex.web

import ch.baunex.timetracking.facade.HolidayDefinitionFacade
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import java.time.LocalDate

@Path("/settings/holidays")
class HolidaySettingsController @Inject constructor(
    private val holidayDefinitionFacade: HolidayDefinitionFacade,
    private val jsonWebToken: JsonWebToken
) {

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun getHolidaySettingsPage(): Response {
        val currentDate = LocalDate.now()
        
        val template = WebController.Templates.holidaySettings(
            activeMenu = "settings",
            currentDate = currentDate,
            activeSubMenu = "holidays"
        )
        
        return Response.ok(template.render()).build()
    }
} 