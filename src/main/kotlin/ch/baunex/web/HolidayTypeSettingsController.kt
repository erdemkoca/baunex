package ch.baunex.web

import ch.baunex.timetracking.facade.HolidayTypeFacade
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import java.time.LocalDate

@Path("/settings/holiday-types")
class HolidayTypeSettingsController @Inject constructor(
    private val holidayTypeFacade: HolidayTypeFacade,
    private val jsonWebToken: JsonWebToken
) {

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun getHolidayTypeSettingsPage(): Response {
        val currentDate = LocalDate.now()
        
        val template = WebController.Templates.holidayTypeSettings(
            activeMenu = "settings",
            currentDate = currentDate,
            activeSubMenu = "holiday-types"
        )
        
        return Response.ok(template.render()).build()
    }
} 