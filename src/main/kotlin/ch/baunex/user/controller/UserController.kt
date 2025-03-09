package ch.baunex.user.controller

import ch.baunex.user.RoleModel
import ch.baunex.user.UserService
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UserController(private val userService: UserService) {

    @POST
    @Path("/register")
    fun registerUser(request: RegisterRequest): Response {
        val user = userService.registerUser(request.email, request.password, request.role)
        return Response.ok(user).build()
    }

    @GET
    @Path("/{email}")
    fun getUser(@PathParam("email") email: String): Response {
        val user = userService.getUserByEmail(email) ?: return Response.status(404).build()
        return Response.ok(user).build()
    }
}

data class RegisterRequest(val email: String, val password: String, val role: RoleModel)