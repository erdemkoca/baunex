package ch.baunex.web

import ch.baunex.company.dto.CompanyDTO
import ch.baunex.company.facade.CompanyFacade
import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.facade.InvoiceFacade
import ch.baunex.project.dto.ProjectListDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.dto.EmployeeDTO
import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate

@Path("/")
class WebController {

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var timeTrackingFacade: TimeTrackingFacade

    @Inject
    lateinit var invoiceFacade: InvoiceFacade

    @Inject
    lateinit var companyFacade: CompanyFacade

    @CheckedTemplate(requireTypeSafeExpressions = false)
    object Templates {
        @JvmStatic
        external fun index(
            projects: List<ProjectListDTO>,
            timeEntries: List<TimeEntryDTO>,
            currentDate: LocalDate,
            activeMenu: String,
            totalProjects: Int,
            totalTimeEntries: Int,
            totalInvoiceDrafts: Int,
            totalInvoicedAmount: Double,
            totalTimeHours: Double,
            totalMaterialCost: Double,
            totalServiceCost: Double,
            totalCosts: Double,
            recentInvoiceDrafts: List<InvoiceDTO>,
            company: CompanyDTO,
            projectId: Long? = null,
            activeSubMenu: String = ""
        ): TemplateInstance

        @JvmStatic
        external fun projects(
            projectsJson: String,
            currentDate: LocalDate,
            activeMenu: String,
            projectId: Long? = null,
            activeSubMenu: String = ""
        ): TemplateInstance

        @JvmStatic
        external fun projectDetail(
            projectJson: String,
            activeMenu: String,
            currentDate: LocalDate,
            catalogItemsJson: String,
            billingJson: String,
            contactsJson: String,
            customersJson: String,
            categoriesJson: String,
            employeesJson: String,
            projectId: Long? = null,
            activeSubMenu: String = ""
        ): TemplateInstance

        @JvmStatic
        external fun projectCatalog(
            projectJson: String,
            activeMenu: String,
            currentDate: LocalDate,
            catalogItemsJson: String,
            billingJson: String,
            contactsJson: String,
            customersJson: String,
            categoriesJson: String,
            employeesJson: String,
            projectId: Long? = null,
            activeSubMenu: String = ""
        ): TemplateInstance

        @JvmStatic
        external fun projectBilling(
            projectJson: String,
            activeMenu: String,
            currentDate: LocalDate,
            catalogItemsJson: String,
            billingJson: String,
            contactsJson: String,
            customersJson: String,
            categoriesJson: String,
            employeesJson: String,
            projectId: Long? = null,
            activeSubMenu: String = ""
        ): TemplateInstance

        @JvmStatic
        external fun projectContacts(
            projectJson: String,
            activeMenu: String,
            currentDate: LocalDate,
            catalogItemsJson: String,
            billingJson: String,
            contactsJson: String,
            customersJson: String,
            categoriesJson: String,
            employeesJson: String,
            projectId: Long? = null,
            activeSubMenu: String = ""
        ): TemplateInstance

        @JvmStatic
        external fun projectNotes(
            projectNotesJson: String,
            activeMenu: String,
            projectId: Long? = null,
            activeSubMenu: String = ""
        ): TemplateInstance

        @JvmStatic
        external fun projectControlReport(
            projectJson: String,
            controlReportJson: String,
            customerTypesJson: String,
            contractorTypesJson: String,
            projectTypesJson: String,
            currentDate: LocalDate,
            activeMenu: String,
            projectId: Long? = null,
            activeSubMenu: String = "",
            employeesJson: String
        ): TemplateInstance


        @JvmStatic
        external fun employees(employees: List<EmployeeDTO>, currentDate: LocalDate, activeMenu: String): TemplateInstance

        @JvmStatic
        external fun employeeForm(employee: EmployeeDTO?, currentDate: LocalDate, activeMenu: String, roles: List<String>): TemplateInstance

        @JvmStatic
        external fun timeTracking(
            activeMenu: String,
            timeEntriesJson: String,
            holidaysJson: String,
            currentDate: String,
            employeesJson: String,
            projectsJson: String,
            entryJson: String
        ): TemplateInstance

        @JvmStatic
        external fun timeTrackingForm(
            activeMenu: String,
            entryJson: String,
            employeesJson: String,
            projectsJson: String,
            categoriesJson: String,
            catalogItemsJson: String,
            currentDate: String,
            activeSubMenu: String
        ): TemplateInstance

        @JvmStatic
        external fun customers(
            customers: List<CustomerDTO>,
            currentDate: LocalDate,
            activeMenu: String
        ): TemplateInstance

        @JvmStatic
        external fun customerDetail(
            customer: CustomerDTO,
            contacts: List<CustomerContactDTO>,
            currentDate: LocalDate,
            activeMenu: String
        ): TemplateInstance

        @JvmStatic
        external fun customerContacts(
            contacts: List<CustomerContactDTO>,
            customerId: Long,
            currentDate: LocalDate,
            activeMenu: String
        ): TemplateInstance

        @JvmStatic
        external fun customerContactForm(
            contact: CustomerContactDTO?,
            customerId: Long,
            currentDate: LocalDate,
            activeMenu: String
        ): TemplateInstance

        @JvmStatic
        external fun companySettings(
            companyJson: String,
            activeMenu: String,
            currentDate: LocalDate,
            activeSubMenu: String = "company"
        ): TemplateInstance

        @JvmStatic
        external fun holidaySettings(
            activeMenu: String,
            currentDate: LocalDate,
            activeSubMenu: String = "holidays"
        ): TemplateInstance

        @JvmStatic
        external fun holidayTypeSettings(
            activeMenu: String,
            currentDate: LocalDate,
            activeSubMenu: String = "holiday-types"
        ): TemplateInstance

        @JvmStatic
        external fun invoiceShell(activeMenu: String = "invoice"): TemplateInstance

        @JvmStatic
        external fun invoiceList(
            invoicesJson: String,
            projectsJson: String,
            currentDate: LocalDate,
            activeMenu: String
        ): TemplateInstance

        @JvmStatic
        external fun invoiceDetail(
            invoiceJson: String,
            currentDate: LocalDate,
            activeMenu: String,
            companyJson: String,
            billingJson: String,
        ): TemplateInstance
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun index(): Response {
        return Response.seeOther(java.net.URI("/dashboard")).build()
    }

    @GET
    @Path("/dashboard")
    @Produces(MediaType.TEXT_HTML)
    fun dashboard(): Response {
        val currentDate        = LocalDate.now()
        val projects           = projectFacade.getAllProjects()
        val timeEntries        = timeTrackingFacade.getAllTimeEntries()
        val invoice            = invoiceFacade.getAll()
        val company            = companyFacade.getCompany() ?: error("Company info not found")

        val totalProjects      = projects.size
        val totalTimeEntries   = timeEntries.size
        val totalInvoiceDrafts = invoice.size

        val totalInvoicedAmount = invoice
            .map { it.totalAmount?.toDouble() ?: 0.0 }
            .sum()

        val totalTimeHours = timeEntries
            .map { it.hoursWorked }
            .sum()

        val totalMaterialCost = projects
            .map { p ->
                projectFacade.getProjectWithDetails(p.id)
                    ?.catalogItems
                    ?.map { it.totalPrice ?: 0.0 }
                    ?.sum()
                    ?: 0.0
            }
            .sum()

        val totalServiceCost = timeEntries.sumOf { it.cost ?: 0.0 }

        val totalCosts = totalMaterialCost + totalServiceCost

        val recentProjects      = projects.take(5)
        val recentTimeEntries   = timeEntries.take(5)
        val recentInvoiceDrafts = invoice.take(5)

        val template = Templates.index(
            projects             = recentProjects,
            timeEntries          = recentTimeEntries,
            currentDate          = currentDate,
            activeMenu           = "dashboard",
            totalProjects        = totalProjects,
            totalTimeEntries     = totalTimeEntries,
            totalInvoiceDrafts   = totalInvoiceDrafts,
            totalInvoicedAmount  = totalInvoicedAmount,
            totalTimeHours       = totalTimeHours,
            totalMaterialCost    = totalMaterialCost,
            totalServiceCost     = totalServiceCost,
            totalCosts           = totalCosts,
            recentInvoiceDrafts  = recentInvoiceDrafts,
            company              = company
        )

        return Response.ok(template.render()).build()
    }


    @GET
    @Path("/document/{type}/preview")
    fun documentPreview(
        @PathParam("type") type: String,
        @QueryParam("projectId") projectId: Long
    ): String {
        return "WebController/documentPreview"
    }
} 