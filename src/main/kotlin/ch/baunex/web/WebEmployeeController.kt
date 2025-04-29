package ch.baunex.web

import ch.baunex.user.dto.EmployeeCreateDTO
import ch.baunex.user.dto.EmployeeDTO
import ch.baunex.user.facade.EmployeeFacade
import ch.baunex.user.model.Role
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI
import java.time.LocalDate

@Path("/employees")
class WebEmployeeController {

    @Inject
    lateinit var employeeFacade: EmployeeFacade

    private fun now() = LocalDate.now()

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun list(): Response {
        val employees: List<EmployeeDTO> = employeeFacade.listAll()
        val template = WebController.Templates.employees(employees, now(), "employees")
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    fun newForm(): Response {
        val roles = listOf(Role.ADMIN, Role.PROJECT_MANAGER, Role.EMPLOYEE, Role.ELECTRICIAN, Role.ACCOUNTANT)
            .map { it.name }
        val template = WebController.Templates.employeeForm(null, now(), "employees", roles)
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    fun editForm(@PathParam("id") id: Long): Response {
        val employee = employeeFacade.findById(id)
            ?: return Response.status(Response.Status.NOT_FOUND).build()
        val roles = listOf(Role.ADMIN, Role.PROJECT_MANAGER, Role.EMPLOYEE, Role.ELECTRICIAN, Role.ACCOUNTANT)
            .map { it.name }
        val template = WebController.Templates.employeeForm(employee, now(), "employees", roles)
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun save(
        @FormParam("id") id: Long?,
        @FormParam("firstName") firstName: String,
        @FormParam("lastName") lastName: String,
        @FormParam("street") street: String?,
        @FormParam("city") city: String?,
        @FormParam("zipCode") zipCode: String?,
        @FormParam("country") country: String?,
        @FormParam("phone") phone: String?,
        @FormParam("email") email: String,
        @FormParam("password") password: String,
        @FormParam("role") role: String,
        @FormParam("ahvNumber") ahvNumber: String,
        @FormParam("bankIban") bankIban: String?,
        @FormParam("hourlyRate") hourlyRate: Double?
    ): Response {
        val createDto = EmployeeCreateDTO(
            firstName   = firstName,
            lastName    = lastName,
            street      = street,
            city        = city,
            zipCode     = zipCode,
            country     = country,
            phone       = phone,
            email       = email,
            password    = password,
            role        = role,
            ahvNumber   = ahvNumber,
            bankIban    = bankIban,
            hourlyRate  = hourlyRate ?: 150.0
        )

        if (id == null) {
            employeeFacade.create(createDto)
        } else {
            employeeFacade.update(id, createDto)
        }
        return Response.seeOther(URI("/employees")).build()
    }

    @GET
    @Path("/{id}/delete")
    fun delete(@PathParam("id") id: Long): Response {
        employeeFacade.delete(id)
        return Response.seeOther(URI("/employees")).build()
    }
}
