package ch.baunex.web

import ch.baunex.billing.dto.BillingDTO
import ch.baunex.catalog.dto.CatalogItemDTO
import ch.baunex.catalog.dto.ProjectCatalogItemDTO
import ch.baunex.company.dto.CompanyDTO
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

    @CheckedTemplate(requireTypeSafeExpressions = false)
    object Templates {
        @JvmStatic
        external fun index(projects: List<ProjectListDTO>, currentDate: LocalDate, activeMenu: String, timeEntries: List<TimeEntryResponseDTO>): TemplateInstance

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
        val projects = projectFacade.getAllProjects().map { it }
        val timeEntries = timeTrackingFacade.getAllTimeEntries()
        val template = Templates.index(projects, LocalDate.now(), "dashboard", timeEntries)
        return Response.ok(template.render()).build()
    }
} 