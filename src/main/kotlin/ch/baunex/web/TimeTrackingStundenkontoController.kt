package ch.baunex.web

import ch.baunex.timetracking.dto.MonthlyHoursAccountDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import ch.baunex.user.dto.EmployeeDTO
import ch.baunex.user.facade.EmployeeFacade
import ch.baunex.timetracking.service.WorkSummaryService
import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Path("/timetracking/stundenkonto")
class TimeTrackingStundenkontoController {

    private val json = Json { ignoreUnknownKeys = true }

    @Inject
    lateinit var employeeFacade: EmployeeFacade

    @Inject
    lateinit var workSummaryService: WorkSummaryService

    @CheckedTemplate(requireTypeSafeExpressions = false)
    object Templates {
        @JvmStatic
        external fun stundenkonto(
            employees: List<EmployeeDTO>,
            currentYear: Int,
            selectedEmployeeId: Long?,
            monthlyAccount: MonthlyHoursAccountDTO?,
            years: List<Int>
        ): TemplateInstance
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun getStundenkontoPage(
        @QueryParam("employeeId") employeeId: Long?,
        @QueryParam("year") year: Int?
    ): Response {
        val currentYear = year ?: LocalDate.now().year
        val employees = employeeFacade.listAll()
        // Standard: erster Mitarbeiter, falls keiner gewählt
        val selectedEmployeeId = employeeId ?: employees.firstOrNull()?.id ?: 0L

        // Generate years list (current year and 5 years back)
        val years = (currentYear downTo currentYear - 5).toList()

        println("DEBUG: Controller - currentYear: $currentYear")
        println("DEBUG: Controller - selectedEmployeeId: $selectedEmployeeId")
        println("DEBUG: Controller - employees count: ${employees.size}")
        println("DEBUG: Controller - TEST DEBUG LOG - THIS SHOULD APPEAR")

        val monthlyAccount = if (selectedEmployeeId > 0) {
            println("DEBUG: Controller - calling getMonthlyHoursAccount for employee $selectedEmployeeId, year $currentYear")
            val result = workSummaryService.getMonthlyHoursAccount(selectedEmployeeId, currentYear)
            println("DEBUG: Controller - getMonthlyHoursAccount returned: ${result != null}")
            if (result != null) {
                println("DEBUG: Controller - monthlyData size: ${result.monthlyData.size}")
                result.monthlyData.forEach { month ->
                    println("DEBUG: Controller - Month ${month.month} - ${month.monthName}")
                }
            }
            result
        } else {
            println("DEBUG: Controller - selectedEmployeeId is 0, returning null")
            null
        }

        val template = Templates.stundenkonto(
            employees = employees,
            currentYear = currentYear,
            selectedEmployeeId = selectedEmployeeId,
            monthlyAccount = monthlyAccount,
            years = years
        )
        .data("activeMenu", "timetracking")
        .data("activeSubMenu", "stundenkonto")

        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/api/monthly-account")
    @Produces(MediaType.APPLICATION_JSON)
    fun getMonthlyAccount(
        @QueryParam("employeeId") employeeId: Long,
        @QueryParam("year") year: Int
    ): Response {
        val account = workSummaryService.getMonthlyHoursAccount(employeeId, year)
        return if (account != null) {
            Response.ok(account).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }
} 