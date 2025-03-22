package ch.baunex.user.controller

import ch.baunex.user.dto.*
import ch.baunex.user.facade.UserFacade
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.*

@Path("/api/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AdminUserController @Inject constructor(
    private val userFacade: UserFacade
) {

    private fun hasAdminRole(securityContext: SecurityContext): Boolean {
        return securityContext.isUserInRole("ADMIN")
    }

    @GET
    @Path("/all")
    fun getAllUsers(@Context securityContext: SecurityContext): Response {
        return if (hasAdminRole(securityContext)) {
            val users = userFacade.getAllUsers()
            Response.ok(UserResponseDTOList(users)).build()
        } else {
            Response.status(Response.Status.FORBIDDEN).entity(MessageResponse("User lacks ADMIN role")).build()
        }
    }

    @GET
    @Path("/{id}")
    fun getUserById(@Context securityContext: SecurityContext, @PathParam("id") id: Long): Response {
        return if (hasAdminRole(securityContext)) {
            val user = userFacade.getUserById(id)
            if (user != null) {
                Response.ok(UserResponseDTO(user.id, user.email, user.role, user.phone, user.street)).build()
            } else {
                Response.status(Response.Status.NOT_FOUND).entity(MessageResponse("User not found")).build()
            }
        } else {
            Response.status(Response.Status.FORBIDDEN).entity(MessageResponse("User lacks ADMIN role")).build()
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    fun updateUser(@Context securityContext: SecurityContext, @PathParam("id") id: Long, updateDTO: UpdateUserDTO): Response {
        return if (hasAdminRole(securityContext)) {
            try {
                val updatedUser = userFacade.updateUser(id, updateDTO)
                    ?: return Response.status(Response.Status.NOT_FOUND).entity(MessageResponse("User not found")).build()

                Response.ok(UserResponseDTO(updatedUser.id!!, updatedUser.email, updatedUser.role, updatedUser.phone, updatedUser.street)).build()
            } catch (e: IllegalArgumentException) {
                Response.status(Response.Status.CONFLICT).entity(MessageResponse(e.message ?: "Conflict occurred")).build()
            }
        } else {
            Response.status(Response.Status.FORBIDDEN).entity(MessageResponse("User lacks ADMIN role")).build()
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    fun deleteUser(@Context securityContext: SecurityContext, @PathParam("id") id: Long): Response {
        return if (hasAdminRole(securityContext)) {
            try {
                userFacade.deleteUserById(id)
                Response.ok(MessageResponse("User deleted successfully")).build()
            } catch (e: Exception) {
                Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(MessageResponse("Failed to delete user")).build()
            }
        } else {
            Response.status(Response.Status.FORBIDDEN).entity(MessageResponse("User lacks ADMIN role")).build()
        }
    }

    @PUT
    @Path("/{id}/role")
    @Transactional
    fun updateUserRole(@Context securityContext: SecurityContext, @PathParam("id") id: Long, roleUpdateDTO: RoleUpdateDTO): Response {
        return if (hasAdminRole(securityContext)) {
            try {
                val updatedUser = userFacade.updateUserRole(id, roleUpdateDTO.role)
                    ?: return Response.status(Response.Status.NOT_FOUND).entity(MessageResponse("User not found")).build()

                Response.ok(updatedUser).build()
            } catch (e: IllegalArgumentException) {
                Response.status(Response.Status.CONFLICT).entity(MessageResponse(e.message ?: "Conflict occurred")).build()
            }
        } else {
            Response.status(Response.Status.FORBIDDEN).entity(MessageResponse("User lacks ADMIN role")).build()
        }
    }
}

