package ch.baunex.user.controller

import ch.baunex.user.dto.*
import ch.baunex.user.facade.UserFacade
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.*
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UserController @Inject constructor(
    private val userFacade: UserFacade,

    @ConfigProperty(name = "quarkus.security.enabled")
    private val securityEnabled: Boolean
) {

    init {
        println("🔍 Quarkus Security Enabled: $securityEnabled")
    }
    @POST
    @Transactional
    fun createUser(userDTO: UserDTO): Response {
        if (userDTO.email.isNullOrBlank() || userDTO.password.isNullOrBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(MessageResponse("Email and password are required"))
                .build()
        }

        // Check if the user already exists
        val existingUser = userFacade.getUserByMail(userDTO.email)
        if (existingUser != null) {
            return Response.status(Response.Status.CONFLICT)
                .entity(MessageResponse("User with this email already exists"))
                .build()
        }

        return try {
            val user = userFacade.registerUser(userDTO)
            Response.status(Response.Status.CREATED).entity(
                UserResponseDTO(user.id, user.email, user.role, user.phone, user.street)
            ).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.CONFLICT)
                .entity(MessageResponse(e.message ?: "Conflict occurred"))
                .build()
        }
    }


    @GET
    @Path("/me")
    fun getCurrentUser(@Context securityContext: SecurityContext): Response {
        val email = securityContext.userPrincipal?.name ?: return Response.status(Response.Status.UNAUTHORIZED).build()
        val user = userFacade.getUserByMail(email)
        return if (user != null) {
            Response.ok(UserResponseDTO(user.id!!, user.email, user.role, user.phone, user.street)).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).entity(MessageResponse("User not found")).build()
        }
    }

    @PUT
    @Path("/me")
    @Transactional
    fun updateCurrentUser(updateDTO: UpdateUserDTO, @Context securityContext: SecurityContext): Response {
        val email = securityContext.userPrincipal?.name
            ?: return Response.status(Response.Status.UNAUTHORIZED).build()

        val user = userFacade.getUserByMail(email)
            ?: return Response.status(Response.Status.NOT_FOUND).build()

//        if (updateDTO.email.isNullOrBlank()) { //|| updateDTO.password.isNullOrBlank(
//            return Response.status(Response.Status.BAD_REQUEST).build()
//        } //gives error, since sometimes you just want to update the street for instance, you dont send it via dto.

        // 🔍 Prevent email duplication
        if (updateDTO.email != user.email) {
            val existingUser = updateDTO.email?.let { userFacade.getUserByMail(it) }
            if (existingUser != null) {
                return Response.status(Response.Status.CONFLICT)
                    .entity(MessageResponse("Email is already in use."))
                    .build()
            }
        }

        return try {
            val updatedUser = user.id?.let { userFacade.updateUser(it, updateDTO) }
                ?: return Response.status(Response.Status.NOT_FOUND).build()

            Response.ok(UserResponseDTO(updatedUser.id, updatedUser.email, updatedUser.role, updatedUser.phone, updatedUser.street)).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.CONFLICT)
                .entity(MessageResponse(e.message ?: "Conflict occurred"))
                .build()
        }
    }



    @DELETE
    @Path("/me")
    @Transactional
    fun deleteCurrentUser(@Context securityContext: SecurityContext): Response {
        val email = securityContext.userPrincipal?.name ?: return Response.status(Response.Status.UNAUTHORIZED).build()
        return try {
            userFacade.deleteUserByMail(email)
            Response.ok(MessageResponse("User deleted successfully")).build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(MessageResponse("Failed to delete user")).build()
        }
    }
}
