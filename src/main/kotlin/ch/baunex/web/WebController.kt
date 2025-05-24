package ch.baunex.web

import ch.baunex.billing.dto.BillingDTO
import ch.baunex.catalog.dto.CatalogItemDTO
import ch.baunex.catalog.dto.ProjectCatalogItemDTO
import ch.baunex.company.dto.CompanyDTO
import ch.baunex.company.facade.CompanyFacade
import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.dto.InvoiceDraftDTO
import ch.baunex.invoice.facade.InvoiceDraftFacade
import ch.baunex.project.dto.ProjectDetailDTO
import ch.baunex.project.dto.ProjectListDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.timetracking.dto.TimeEntryCatalogItemDTO
import ch.baunex.user.dto.UserResponseDTO
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
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
    lateinit var invoiceDraftFacade: InvoiceDraftFacade

    @Inject
    lateinit var companyFacade: CompanyFacade

    @CheckedTemplate(requireTypeSafeExpressions = false)
    object Templates {
        @JvmStatic
        external fun index(
            projects: List<ProjectListDTO>,
            timeEntries: List<TimeEntryResponseDTO>,
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
            recentInvoiceDrafts: List<InvoiceDraftDTO>,
            company: CompanyDTO
        ): TemplateInstance

        @JvmStatic
        external fun projects(projects: List<ProjectListDTO>, currentDate: LocalDate, activeMenu: String): TemplateInstance

        @JvmStatic
        external fun projectDetail(
            project: ProjectDetailDTO,
            activeMenu: String,
            currentDate: LocalDate,
            catalogItems: List<CatalogItemDTO>,
            billing: BillingDTO,
            contacts: List<CustomerContactDTO>,
            customers: List<CustomerDTO>
        ): TemplateInstance

        @JvmStatic
        external fun users(users: List<UserResponseDTO>, currentDate: LocalDate, activeMenu: String): TemplateInstance

        @JvmStatic
        external fun userForm(user: UserResponseDTO?, currentDate: LocalDate, activeMenu: String, roles: List<String>): TemplateInstance

        @JvmStatic
        external fun employees(employees: List<EmployeeDTO>, currentDate: LocalDate, activeMenu: String): TemplateInstance

        @JvmStatic
        external fun employeeForm(employee: EmployeeDTO?, currentDate: LocalDate, activeMenu: String, roles: List<String>): TemplateInstance

        @JvmStatic
        external fun timetracking(
            activeMenu: String,
            timeEntries: List<TimeEntryResponseDTO>,
            currentDate: String,
            employees: List<EmployeeDTO>,
            projects: List<ProjectListDTO>,
            entry: TimeEntryResponseDTO? = null): TemplateInstance

        @JvmStatic
        external fun timetrackingForm(
            entry: TimeEntryResponseDTO?,
            employees: List<EmployeeDTO>,
            projects: List<ProjectListDTO>,
            currentDate: String,
            activeMenu: String,
            catalogItems: List<CatalogItemDTO>
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
        external fun companySettings(company: CompanyDTO?, activeMenu: String, currentDate: LocalDate): TemplateInstance

        @JvmStatic
        external fun invoiceList(
            invoices: List<InvoiceDTO>,
            projects: List<ProjectListDTO>,
            currentDate: LocalDate,
            activeMenu: String
        ): TemplateInstance

        @JvmStatic
        external fun invoiceDetail(
            invoice: InvoiceDTO,
            currentDate: LocalDate,
            activeMenu: String
        ): TemplateInstance

        @JvmStatic
        external fun invoiceDraftList(
            drafts: List<InvoiceDraftDTO>,
            projects: List<ProjectListDTO>,
            currentDate: LocalDate,
            activeMenu: String
        ): TemplateInstance

        @JvmStatic
        external fun invoiceDraftForm(
            draft: InvoiceDraftDTO?,
            customers: List<CustomerDTO>,
            projects: List<ProjectListDTO>,
            currentDate: LocalDate,
            dueDate: LocalDate,
            activeMenu: String,
            selectedProject: ProjectDetailDTO? = null,
            company: CompanyDTO? = null,
            billing: BillingDTO? = null
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
        val currentDate = LocalDate.now()
        val projects = projectFacade.getAllProjects()
        val timeEntries = timeTrackingFacade.getAllTimeEntries()
        val invoiceDrafts = invoiceDraftFacade.getAll()
        val company = companyFacade.getCompany() ?: throw IllegalStateException("Company information not found")

        // Calculate statistics
        val totalProjects = projects.size
        val totalTimeEntries = timeEntries.size
        val totalInvoiceDrafts = invoiceDrafts.size
        val totalInvoicedAmount = invoiceDrafts.sumOf { it.totalBrutto ?: 0.0 }
        val totalTimeHours = timeEntries.sumOf { it.hoursWorked }
        
        // Calculate material costs from project details
        val totalMaterialCost = projects.sumOf { project ->
            val projectDetail = projectFacade.getProjectWithDetails(project.id)
            projectDetail?.catalogItems?.sumOf { item -> item.totalPrice ?: 0.0 } ?: 0.0
        }
        
        val totalServiceCost = timeEntries.sumOf { it.cost ?: 0.0 }
        val totalCosts = totalServiceCost + totalMaterialCost

        // Get recent items
        val recentProjects = projects.take(5)
        val recentTimeEntries = timeEntries.take(5)
        val recentInvoiceDrafts = invoiceDrafts.take(5)

        val template = Templates.index(
            projects = recentProjects,
            timeEntries = recentTimeEntries,
            currentDate = currentDate,
            activeMenu = "dashboard",
            totalProjects = totalProjects,
            totalTimeEntries = totalTimeEntries,
            totalInvoiceDrafts = totalInvoiceDrafts,
            totalInvoicedAmount = totalInvoicedAmount,
            totalTimeHours = totalTimeHours,
            totalMaterialCost = totalMaterialCost,
            totalServiceCost = totalServiceCost,
            totalCosts = totalCosts,
            recentInvoiceDrafts = recentInvoiceDrafts,
            company = company
        )
        return Response.ok(template.render()).build()
    }
} 