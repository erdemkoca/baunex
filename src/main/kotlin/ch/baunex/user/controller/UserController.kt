package ch.baunex.user.controller

import ch.baunex.user.dto.*
import ch.baunex.user.facade.UserFacade
import ch.baunex.security.utils.RoleUtil
import io.quarkus.arc.All
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.*
import kotlinx.serialization.Serializable
import jakarta.transaction.Transactional

@Serializable
data class TokenResponse(val token: String)

@Serializable
data class MessageResponse(val message: String)

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UserController @Inject constructor(
    private val userFacade: UserFacade,
    private val roleUtil: RoleUtil
) {

    @POST
    @Transactional
    //TODO DB normalisieren
    fun createUser(userDTO: UserDTO): Response {
        if (userDTO.email.isNullOrBlank() || userDTO.password.isNullOrBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(MessageResponse("Email and password are required"))
                .build()
        }

        return try {
            val user = userFacade.registerUser(userDTO)
            Response.status(Response.Status.CREATED).entity(
                UserResponseDTO(
                    user.id, user.email, user.role, user.phone, user.street
                )
            ).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.CONFLICT).entity(MessageResponse(e.message ?: "Conflict occurred")).build()
        }
    }

    @POST
    @Path("/login")
    @Transactional
    fun login(loginDTO: LoginDTO): Response {
        val token = userFacade.authenticate(loginDTO)
        return if (token != null) {
            Response.ok(TokenResponse(token)).build()
        } else {
            Response.status(Response.Status.UNAUTHORIZED).entity(MessageResponse("Invalid credentials")).build()
        }
    }

    @GET
    @Path("/allUsers")
    fun getAllUsers(@Context securityContext: SecurityContext): Response {
        if (!roleUtil.hasRole(securityContext, "ADMIN")) {
            return Response.status(Response.Status.FORBIDDEN).entity(MessageResponse("Access denied")).build()
        }
        //TODO !! and admin string enum
        val users = userFacade.getAllUsers().map { user ->
            UserResponseDTO(user.id!!, user.email, user.role, user.phone, user.street)
        }
        return Response.ok(UserResponseDTOList(users)).build()
    }
    //admin-controller or adroles falls überlappt. Pathordenrred berechtigungsgründer

    @PUT
    @Path("/{id}")
    //id with JWT notevery ID every user another request, user/admin request. change own
    //this is for admin
    @Transactional
    fun updateUser(@PathParam("id") userId: Long, updateDTO: UpdateUserDTO, @Context securityContext: SecurityContext): Response {
        if (securityContext.userPrincipal == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(MessageResponse("Authentication required")).build()
        }

        val user = userFacade.getUserById(userId)
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(MessageResponse("User not found")).build()
        }

        if (!roleUtil.hasRole(securityContext, "ADMIN") && securityContext.userPrincipal.name != user.email) {
            return Response.status(Response.Status.FORBIDDEN).entity(MessageResponse("Access denied")).build()
        }

        return try {
            val updatedUser = userFacade.updateUser(userId, updateDTO)
            if (updatedUser != null) {
                Response.ok(
                    UserResponseDTO(updatedUser.id!!, updatedUser.email, updatedUser.role, updatedUser.phone, updatedUser.street)
                ).build()
            } else {
                Response.status(Response.Status.NOT_FOUND).entity(MessageResponse("User not found")).build()
            }
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.CONFLICT).entity(MessageResponse(e.message ?: "Conflict occurred")).build()
        }
    }


    @GET
    @Path("/testAdmin")
    fun adminOnlyEndpoint(@Context securityContext: SecurityContext): Response {
        if (securityContext.userPrincipal == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(MessageResponse("Authentication required")).build()
        }
        if (!roleUtil.hasRole(securityContext, "ADMIN")) {
            return Response.status(Response.Status.FORBIDDEN).entity(MessageResponse("Access denied")).build()
        }
        return Response.ok(MessageResponse("Welcome, Admin!")).build()
    }
}
