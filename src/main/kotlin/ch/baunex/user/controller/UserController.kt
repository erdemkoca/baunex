package ch.baunex.user.controller

import ch.baunex.user.dto.LoginDTO
import ch.baunex.user.dto.UpdateUserDTO
import ch.baunex.user.dto.UserDTO
import ch.baunex.user.dto.UserResponseDTO
import ch.baunex.user.facade.UserFacade
import ch.baunex.user.utils.RoleUtil
import jakarta.inject.Inject
import jakarta.ws.rs.core.SecurityContext
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
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
    private val userFacade: UserFacade,
    private val securityContext: SecurityContext,
    private val roleUtil: RoleUtil
) {

    @POST
    @Transactional
    fun createUser(userDTO: UserDTO): Response {
        val user = userFacade.registerUser(userDTO)
        return Response.status(Response.Status.CREATED)
            .entity(UserResponseDTO(user.id, user.email, user.role))
            .build()
    }


    @POST
    @Path("/login")
    @Transactional
    fun login(loginDTO: LoginDTO): Response {
        val token = userFacade.authenticateUser(loginDTO)
        return if (token != null) {
            Response.ok(TokenResponse(token)).build()
        } else {
            Response.status(Response.Status.UNAUTHORIZED).entity(MessageResponse("Invalid credentials")).build()
        }
    }

    @GET
    @Path("/adminListUsers")
    fun listUsers(@Context securityContext: SecurityContext): List<UserResponseDTO> {
        if (!roleUtil.hasRole(securityContext, "ADMIN")) {
            throw ForbiddenException("Access denied")
        }
        return userFacade.listUsers()
    }

    @GET
    @Path("/testAdmin")
    fun adminOnlyEndpoint(): Response {
        if (!roleUtil.hasRole(securityContext, "ADMIN")) {
            return Response.status(Response.Status.FORBIDDEN).entity("Access denied").build()
        }
        return Response.ok(MessageResponse("Welcome, Admin!")).build()
    }

    @GET
    @Path("/allUsers")
    fun getAllUsers(): List<UserResponseDTO> {
        return userFacade.getAllUsers().map { user ->
            UserResponseDTO(user.id!!, user.email, user.role)
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    fun updateUser(@PathParam("id") userId: Long, updateDTO: UpdateUserDTO): Response {
        return try {
            val updatedUser = userFacade.updateUser(userId, updateDTO)
            if (updatedUser != null) {
                Response.ok(UserResponseDTO(updatedUser.id!!, updatedUser.email, updatedUser.role)).build()
            } else {
                Response.status(Response.Status.NOT_FOUND).entity("User not found").build()
            }
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.CONFLICT).entity(e.message).build()
        }
    }


}
