package ch.baunex.user.controller

import ch.baunex.user.dto.LoginDTO
import ch.baunex.user.dto.UserDTO
import ch.baunex.user.dto.UserResponseDTO
import ch.baunex.user.service.AuthService
import ch.baunex.user.service.UserService
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(val token: String)

@Serializable
data class MessageResponse(val message: String)

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UserController @Inject constructor(
    private val userService: UserService,
    private val authService: AuthService
) {

    @POST
    @Transactional
    fun createUser(userDTO: UserDTO): Response {
        val user = userService.registerUser(userDTO)
        return Response.status(Response.Status.CREATED)
            .entity(UserResponseDTO(user.id!!, user.email, user.role))
            .build()
    }


    @POST
    @Path("/login")
    @Transactional
    fun login(loginDTO: LoginDTO): Response {
        val token = authService.authenticate(loginDTO)
        return if (token != null) {
            Response.ok(TokenResponse(token)).build()
        } else {
            Response.status(Response.Status.UNAUTHORIZED).entity(MessageResponse("Invalid credentials")).build()
        }
    }

    @GET
    @RolesAllowed("ADMIN")
    @Path("/adminListUsers")
    fun listUsers(): List<UserResponseDTO> {
        return userService.listUsers()
    }

    @GET
    @RolesAllowed("ADMIN")
    @Path("/testAdmin")
    fun adminOnlyEndpoint(): Response {
        return Response.ok(MessageResponse("Welcome, Admin!")).build()
    }
}
