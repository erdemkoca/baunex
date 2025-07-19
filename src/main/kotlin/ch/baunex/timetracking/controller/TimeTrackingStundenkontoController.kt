package ch.baunex.timetracking.controller

import ch.baunex.timetracking.dto.MonthlyHoursAccountDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import ch.baunex.timetracking.facade.HolidayFacade
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

    @Inject
    lateinit var holidayFacade: HolidayFacade

    @CheckedTemplate(basePath = "timetracking", requireTypeSafeExpressions = false)
    object Templates {
        @JvmStatic
        external fun stundenkonto(
            employees: List<EmployeeDTO>,
            currentYear: Int,
            selectedEmployeeId: Long?,
            monthlyAccount: MonthlyHoursAccountDTO?,
            years: List<Int>,
            vacationStats: Map<String, Any>?
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

        val monthlyAccount = if (selectedEmployeeId > 0) {
            val result = workSummaryService.getMonthlyHoursAccount(selectedEmployeeId, currentYear)
            result?.monthlyData?.forEach { month ->
                println("DEBUG: Controller - Month ${month.month} - ${month.monthName}")
            }
            result
        } else {
            println("DEBUG: Controller - selectedEmployeeId is 0, returning null")
            null
        }

        // Calculate vacation statistics for selected employee
        val vacationStats = if (selectedEmployeeId > 0) {
            val employee = employees.find { it.id == selectedEmployeeId }
            if (employee != null) {
                val totalVacationDays = employee.vacationDays
                val usedVacationDays = workSummaryService.calculateUsedVacationDays(selectedEmployeeId, currentYear)
                val remainingVacationDays = totalVacationDays - usedVacationDays
                
                // NEW: Calculate cumulative statistics
                val cumulativeSickLeaveDays = workSummaryService.calculateCumulativeSickLeaveDays(selectedEmployeeId)
                val cumulativeVacationDays = workSummaryService.calculateCumulativeVacationDays(selectedEmployeeId)
                
                mapOf(
                    "totalVacationDays" to totalVacationDays,
                    "usedVacationDays" to usedVacationDays,
                    "remainingVacationDays" to remainingVacationDays,
                    "cumulativeSickLeaveDays" to cumulativeSickLeaveDays,
                    "cumulativeVacationDays" to cumulativeVacationDays
                )
            } else null
        } else null

        val template = Templates.stundenkonto(
            employees = employees,
            currentYear = currentYear,
            selectedEmployeeId = selectedEmployeeId,
            monthlyAccount = monthlyAccount,
            years = years,
            vacationStats = vacationStats
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

    @GET
    @Path("/api/debug/holidays")
    @Produces(MediaType.APPLICATION_JSON)
    fun debugHolidays(@QueryParam("employeeId") employeeId: Long?): Response {
        val testEmployeeId = employeeId ?: 1L
        
        val sickLeaveDays = workSummaryService.calculateCumulativeSickLeaveDays(testEmployeeId)
        val vacationDays = workSummaryService.calculateCumulativeVacationDays(testEmployeeId)
        
        val jsonResponse = """
            {
                "employeeId": $testEmployeeId,
                "sickLeaveDays": $sickLeaveDays,
                "vacationDays": $vacationDays,
                "timestamp": "${java.time.LocalDateTime.now()}"
            }
        """.trimIndent()
        
        return Response.ok(jsonResponse).build()
    }

    @GET
    @Path("/api/debug/holidays-db")
    @Produces(MediaType.APPLICATION_JSON)
    fun debugHolidaysDb(@QueryParam("employeeId") employeeId: Long?): Response {
        val testEmployeeId = employeeId ?: 1L
        val today = java.time.LocalDate.now()
        val startDate = today.minusMonths(6)
        
        // Use the existing service methods instead of direct repository access
        val sickLeaveDays = workSummaryService.calculateCumulativeSickLeaveDays(testEmployeeId)
        val vacationDays = workSummaryService.calculateCumulativeVacationDays(testEmployeeId)
        
        val jsonResponse = """
            {
                "employeeId": $testEmployeeId,
                "sickLeaveDays": $sickLeaveDays,
                "vacationDays": $vacationDays,
                "startDate": "$startDate",
                "endDate": "$today",
                "timestamp": "${java.time.LocalDateTime.now()}"
            }
        """.trimIndent()
        
        return Response.ok(jsonResponse).build()
    }

    @GET
    @Path("/api/debug/holidays-raw")
    @Produces(MediaType.APPLICATION_JSON)
    fun debugHolidaysRaw(@QueryParam("employeeId") employeeId: Long?): Response {
        val testEmployeeId = employeeId ?: 1L
        val today = java.time.LocalDate.now()
        val startDate = today.minusMonths(6)
        
        // Use the new service method
        val holidaySummary = workSummaryService.getHolidaySummary(testEmployeeId, startDate, today)
        
        val jsonResponse = """
            {
                "employeeId": $testEmployeeId,
                "holidaySummary": "${holidaySummary}",
                "dateRange": "$startDate to $today",
                "timestamp": "${java.time.LocalDateTime.now()}"
            }
        """.trimIndent()
        
        return Response.ok(jsonResponse).build()
    }

    @GET
    @Path("/api/holidays/cumulative-sick-leave")
    @Produces(MediaType.APPLICATION_JSON)
    fun getCumulativeSickLeaveDays(
        @QueryParam("employeeId") employeeId: Long?,
        @QueryParam("week") week: Int?,
        @QueryParam("year") year: Int?
    ): Response {
        val targetEmployeeId = employeeId ?: 1L
        val targetWeek = week ?: 1
        val targetYear = year ?: LocalDate.now().year
        
        val cumulativeSickLeaveDays = workSummaryService.calculateCumulativeSickLeaveDaysUpToWeek(targetEmployeeId, targetWeek, targetYear)
        
        val jsonResponse = """
            {
                "employeeId": $targetEmployeeId,
                "week": $targetWeek,
                "year": $targetYear,
                "cumulativeSickLeaveDays": $cumulativeSickLeaveDays,
                "timestamp": "${java.time.LocalDateTime.now()}"
            }
        """.trimIndent()
        
        return Response.ok(jsonResponse).build()
    }

    @GET
    @Path("/api/holidays/cumulative-vacation")
    @Produces(MediaType.APPLICATION_JSON)
    fun getCumulativeVacationDays(
        @QueryParam("employeeId") employeeId: Long?,
        @QueryParam("week") week: Int?,
        @QueryParam("year") year: Int?
    ): Response {
        val targetEmployeeId = employeeId ?: 1L
        val targetWeek = week ?: 1
        val targetYear = year ?: LocalDate.now().year
        
        val cumulativeVacationDays = workSummaryService.calculateCumulativeVacationDaysUpToWeek(targetEmployeeId, targetWeek, targetYear)
        
        val jsonResponse = """
            {
                "employeeId": $targetEmployeeId,
                "week": $targetWeek,
                "year": $targetYear,
                "cumulativeVacationDays": $cumulativeVacationDays,
                "timestamp": "${java.time.LocalDateTime.now()}"
            }
        """.trimIndent()
        
        return Response.ok(jsonResponse).build()
    }
} 