package ch.baunex.web

import ch.baunex.catalog.facade.CatalogFacade
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.TimeEntryCatalogItemDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import ch.baunex.user.facade.EmployeeFacade
import ch.baunex.timetracking.service.TimeEntryCostService
import ch.baunex.user.model.Role
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI
import java.time.LocalDate
import java.util.logging.Logger

@Path("/timetracking")
class WebTimeTrackingController {

    private val logger = Logger.getLogger(WebTimeTrackingController::class.java.name)

    @Inject
    lateinit var timeTrackingFacade: TimeTrackingFacade

    @Inject
    lateinit var employeeFacade: EmployeeFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var catalogFacade: CatalogFacade

    @Inject
    lateinit var timeEntryCostService: TimeEntryCostService

    private fun getCurrentDate(): String = LocalDate.now().toString()

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun view(): Response {
        val timeEntries = timeTrackingFacade.getAllTimeEntries()
        val employees   = employeeFacade.listAll()
        val projects    = projectFacade.getAllProjects()
        val template = WebController.Templates.timetracking(
            activeMenu  = "timetracking",
            timeEntries = timeEntries,
            currentDate = getCurrentDate(),
            employees   = employees,
            projects    = projects,
            entry       = null
        )
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    fun newEntry(): Response {
        val employees = employeeFacade.listAll()
        val projects = projectFacade.getAllProjects()
        val catalogItems = catalogFacade.getAllItems()
        val template = WebController.Templates.timetrackingForm(
            entry = null,
            employees = employees,
            projects = projects,
            currentDate = getCurrentDate(),
            activeMenu = "timetracking",
            catalogItems = catalogItems
        )
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    fun edit(@PathParam("id") id: Long): Response {
        val entry = timeTrackingFacade.getTimeEntryById(id)
            ?: return Response.status(Response.Status.NOT_FOUND).build()
        val employees = employeeFacade.listAll()
        val projects = projectFacade.getAllProjects()
        val catalogItems = catalogFacade.getAllItems()
        
        val template = WebController.Templates.timetrackingForm(
            entry = entry,
            employees = employees,
            projects = projects,
            currentDate = getCurrentDate(),
            activeMenu = "timetracking",
            catalogItems = catalogItems
        )
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun saveEntry(
        @FormParam("id") id: Long?,
        @FormParam("employeeId") employeeId: Long,
        @FormParam("projectId") projectId: Long,
        @FormParam("date") date: LocalDate,
        @FormParam("hoursWorked") hoursWorked: Double,
        @FormParam("note") note: String?,
        @FormParam("hourlyRate") hourlyRate: Double?,
        @FormParam("notBillable") notBillable: String?,
        @FormParam("invoiced") invoiced: String?,
        @FormParam("catalogItemDescription") catalogItemDescription: String?,
        @FormParam("catalogItemPrice") catalogItemPrice: Double?,
        @FormParam("catalogItemIds") catalogItemIds: List<String>?,
        @FormParam("catalogItemQuantities") catalogItemQuantities: List<String>?,
        @FormParam("catalogItemNames") catalogItemNames: List<String>?,
        @FormParam("catalogItemPrices") catalogItemPrices: List<String>?,
        // Surcharges
        @FormParam("hasNightSurcharge") hasNightSurcharge: String?,
        @FormParam("hasWeekendSurcharge") hasWeekendSurcharge: String?,
        @FormParam("hasHolidaySurcharge") hasHolidaySurcharge: String?,
        // Additional Costs
        @FormParam("travelTimeMinutes") travelTimeMinutes: Int = 0,
        @FormParam("disposalCost") disposalCost: Double = 0.0,
        @FormParam("hasWaitingTime") hasWaitingTime: String?,
        @FormParam("waitingTimeMinutes") waitingTimeMinutes: Int = 0
    ): Response {
        // Debug logging
        logger.info("Form parameters received:")
        logger.info("notBillable: $notBillable")
        logger.info("invoiced: $invoiced")
        logger.info("hasNightSurcharge: $hasNightSurcharge")
        logger.info("hasWeekendSurcharge: $hasWeekendSurcharge")
        logger.info("hasHolidaySurcharge: $hasHolidaySurcharge")
        logger.info("hasWaitingTime: $hasWaitingTime")

        val catalogItems = if (catalogItemIds != null && catalogItemQuantities != null && 
                             catalogItemNames != null && catalogItemPrices != null) {
            catalogItemIds.zip(catalogItemQuantities.zip(catalogItemNames.zip(catalogItemPrices))).map { (id, rest) ->
                val (quantity, namePrice) = rest
                val (name, price) = namePrice
                TimeEntryCatalogItemDTO(
                    catalogItemId = id.toLongOrNull(),
                    quantity = quantity.toIntOrNull() ?: 1,
                    itemName = name,
                    unitPrice = price.toDoubleOrNull() ?: 0.0,
                    totalPrice = (quantity.toIntOrNull() ?: 1) * (price.toDoubleOrNull() ?: 0.0)
                )
            }
        } else {
            emptyList()
        }

        val dto = TimeEntryDTO(
            employeeId = employeeId,
            projectId = projectId,
            date = date,
            hoursWorked = hoursWorked,
            note = note,
            hourlyRate = hourlyRate,
            billable = notBillable != "true",
            invoiced = invoiced == "true",
            catalogItemDescription = catalogItemDescription,
            catalogItemPrice = catalogItemPrice,
            catalogItems = catalogItems,
            hasNightSurcharge = hasNightSurcharge == "true",
            hasWeekendSurcharge = hasWeekendSurcharge == "true",
            hasHolidaySurcharge = hasHolidaySurcharge == "true",
            travelTimeMinutes = travelTimeMinutes,
            disposalCost = disposalCost,
            hasWaitingTime = hasWaitingTime == "true",
            waitingTimeMinutes = waitingTimeMinutes,
            costBreakdown = timeEntryCostService.calculateCostBreakdown(
                TimeEntryDTO(
                    employeeId = employeeId,
                    projectId = projectId,
                    date = date,
                    hoursWorked = hoursWorked,
                    note = note,
                    hourlyRate = hourlyRate,
                    billable = notBillable != "true",
                    invoiced = invoiced == "true",
                    catalogItemDescription = catalogItemDescription,
                    catalogItemPrice = catalogItemPrice,
                    catalogItems = catalogItems,
                    hasNightSurcharge = hasNightSurcharge == "true",
                    hasWeekendSurcharge = hasWeekendSurcharge == "true",
                    hasHolidaySurcharge = hasHolidaySurcharge == "true",
                    travelTimeMinutes = travelTimeMinutes,
                    disposalCost = disposalCost,
                    hasWaitingTime = hasWaitingTime == "true",
                    waitingTimeMinutes = waitingTimeMinutes
                )
            )
        )

        // Debug logging for DTO values
        logger.info("DTO values:")
        logger.info("billable: ${dto.billable}")
        logger.info("invoiced: ${dto.invoiced}")
        logger.info("hasNightSurcharge: ${dto.hasNightSurcharge}")
        logger.info("hasWeekendSurcharge: ${dto.hasWeekendSurcharge}")
        logger.info("hasHolidaySurcharge: ${dto.hasHolidaySurcharge}")
        logger.info("hasWaitingTime: ${dto.hasWaitingTime}")

        if (id == null) {
            timeTrackingFacade.logTime(dto)
        } else {
            timeTrackingFacade.updateTimeEntry(id, dto)
        }

        return Response.seeOther(URI("/timetracking")).build()
    }

    @GET
    @Path("/{id}/delete")
    fun delete(@PathParam("id") id: Long): Response {
        timeTrackingFacade.deleteTimeEntry(id)
        return Response.seeOther(URI("/timetracking")).build()
    }

    @POST
    @Path("/{id}/approve")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun approve(@PathParam("id") id: Long, @FormParam("approverId") approverId: Long): Response {
        val approverId = employeeFacade.findByRole(Role.ADMIN).id
        val success = timeTrackingFacade.approveEntry(id, approverId)
        return if (success) {
            Response.ok().build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
        //TODO now approverId is hardcoded
    }
}
