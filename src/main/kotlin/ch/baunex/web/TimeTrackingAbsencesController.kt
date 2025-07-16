package ch.baunex.web

import ch.baunex.timetracking.dto.HolidayDTO
import ch.baunex.timetracking.dto.EmployeeAbsenceStatsDTO
import ch.baunex.timetracking.dto.PublicHolidayDTO
import ch.baunex.timetracking.facade.HolidayFacade
import ch.baunex.timetracking.service.HolidayDefinitionService
import ch.baunex.user.dto.EmployeeDTO
import ch.baunex.user.facade.EmployeeFacade
import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Path("/timetracking/absences")
class TimeTrackingAbsencesController {

    private val json = Json { ignoreUnknownKeys = true }

    @Inject
    lateinit var holidayFacade: HolidayFacade

    @Inject
    lateinit var employeeFacade: EmployeeFacade

    @Inject
    lateinit var holidayDefinitionService: HolidayDefinitionService

    @CheckedTemplate(requireTypeSafeExpressions = false)
    object Templates {
        @JvmStatic
        external fun absences(
            activeMenu: String,
            activeSubMenu: String,
            holidaysJson: String,
            employeesJson: String,
            pendingHolidaysJson: String,
            approvedHolidaysJson: String,
            rejectedHolidaysJson: String,
            employeeStatsJson: String,
            publicHolidaysJson: String,
            currentYear: Int
        ): TemplateInstance
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun absences(): Response {
        val holidays = holidayFacade.getAllHolidays()
        val employees = employeeFacade.listAll()
        val currentYear = LocalDate.now().year
        
        // Get fresh data after creating samples
        val allHolidays = holidays
        
        // Für das Frontend: requestDate = createdAt
        val pendingHolidays = allHolidays.filter { it.status == "PENDING" || it.status == "UNDEFINED" }
        val approvedHolidays = allHolidays.filter { it.status == "APPROVED" }
        val rejectedHolidays = allHolidays.filter { it.status == "REJECTED" }
        // Die Felder werden im Frontend als request.requestDate = createdAt genutzt
        
        // Get public holidays for the current year
        val publicHolidayModels = holidayDefinitionService.getHolidaysForYear(currentYear)
        val publicHolidays = publicHolidayModels.map { model ->
            PublicHolidayDTO(
                id = model.id,
                year = model.year,
                holidayDate = model.date.toString(),
                name = model.name,
                canton = model.canton,
                isFixed = model.isFixed,
                isEditable = model.isEditable,
                active = model.active,
                isWorkFree = model.isWorkFree,
                holidayType = model.holidayType.name,
                description = model.description
            )
        }
        
        // Calculate employee statistics with proper structure
        val employeeStats = employees.map { employee ->
            val employeeHolidays = allHolidays.filter { it.employeeId == employee.id }
            val approvedDays = employeeHolidays.filter { it.status == "APPROVED" }
                .sumOf { 
                    val days = java.time.temporal.ChronoUnit.DAYS.between(it.startDate, it.endDate) + 1
                    days.toInt()
                }
            val pendingDays = employeeHolidays.filter { it.status == "PENDING" || it.status == "UNDEFINED" }
                .sumOf { 
                    val days = java.time.temporal.ChronoUnit.DAYS.between(it.startDate, it.endDate) + 1
                    days.toInt()
                }
            
            EmployeeAbsenceStatsDTO(
                employee = employee,
                totalHolidays = employeeHolidays.size,
                approvedDays = approvedDays,
                pendingDays = pendingDays,
                remainingDays = (employee.vacationDays - approvedDays)
            )
        }
        
        val page = Templates.absences(
            activeMenu = "timetracking",
            activeSubMenu = "absences",
            holidaysJson = json.encodeToString(allHolidays),
            employeesJson = json.encodeToString(employees),
            pendingHolidaysJson = json.encodeToString(pendingHolidays),
            approvedHolidaysJson = json.encodeToString(approvedHolidays),
            rejectedHolidaysJson = json.encodeToString(rejectedHolidays),
            employeeStatsJson = json.encodeToString(employeeStats),
            publicHolidaysJson = json.encodeToString(publicHolidays),
            currentYear = currentYear
        )
        
        return Response.ok(page.render()).build()
    }
} 