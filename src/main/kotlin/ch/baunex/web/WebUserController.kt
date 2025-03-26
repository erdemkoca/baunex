package ch.baunex.web

import ch.baunex.user.UserHandler
import ch.baunex.user.dto.UpdateUserDTO
import ch.baunex.user.dto.UserDTO
import ch.baunex.user.model.Role
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI
import java.time.LocalDate

@Path("/users")
class WebUserController {

    @Inject
    lateinit var userHandler: UserHandler

    private fun getCurrentDate(): LocalDate = LocalDate.now()

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun users(): Response {
        val users = userHandler.getAllUsers()
        val template = WebController.Templates.users(users, getCurrentDate(), "users")
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun saveUser(
        @FormParam("id") id: Long?,
        @FormParam("email") email: String,
        @FormParam("password") password: String,
        @FormParam("role") role: String?,
        @FormParam("phone") phone: String?,
        @FormParam("street") street: String?
    ): Response {
        if (id == null) {
            // Create new
            val dto = UserDTO(
                email = email,
                password = password,
                role = role?.let { Role.valueOf(it) },
                phone = phone,
                street = street
            )
            userHandler.saveUser(dto)
        } else {
            // Update existing
            val dto = UpdateUserDTO(
                email = email,
                password = if (password.isBlank()) null else password,
                role = role?.let { Role.valueOf(it) },
                phone = phone,
                street = street
            )
            userHandler.updateUser(id, dto)
        }

        return Response.seeOther(URI("/users")).build()
    }



    @GET
    @Path("new")
    @Produces(MediaType.TEXT_HTML)
    fun newUser(): Response {
        val roles = listOf("USER", "ADMIN", "PROJECT_MANAGER", "EMPLOYEE", "CLIENT", "SUPERADMIN")
        val template = WebController.Templates.userForm(null, getCurrentDate(), "users", roles)
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}/delete")
    fun deleteUser(@PathParam("id") id: Long): Response {
        userHandler.deleteUser(id)
        return Response.seeOther(URI("/users")).build()
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    fun editUser(@PathParam("id") id: Long): Response {
        val user = userHandler.getUserById(id)
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build()
        }

        val roles = listOf("USER", "ADMIN", "PROJECT_MANAGER", "EMPLOYEE", "CLIENT", "SUPERADMIN")
        val template = WebController.Templates.userForm(user, getCurrentDate(), "users", roles)
        return Response.ok(template.render()).build()
    }





}
