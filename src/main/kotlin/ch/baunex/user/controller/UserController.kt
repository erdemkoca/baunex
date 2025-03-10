package ch.baunex.user.controller

import ch.baunex.user.utils.PasswordUtil
import ch.baunex.user.dto.LoginDTO
import ch.baunex.user.repository.UserRepository
import ch.baunex.user.dto.UserDTO
import ch.baunex.user.dto.UserResponseDTO
import ch.baunex.user.model.UserModel
import ch.baunex.user.service.AuthService
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(val token: String)

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UserController @Inject constructor(
    private val userRepository: UserRepository,
    private val authService: AuthService
) {

    @POST
    @Transactional
    fun createUser(userDTO: UserDTO): Response {
        val user = UserModel(userDTO.email, PasswordUtil.hashPassword(userDTO.password), userDTO.role)
        userRepository.persist(user)

        return Response.status(Response.Status.CREATED)
            .entity(UserResponseDTO(user.id!!, user.email, user.role))
            .build()
    }

    @GET
    fun listUsers(): List<UserResponseDTO> {
        return userRepository.listAll().map { user: UserModel ->
            UserResponseDTO(user.id!!, user.email, user.role)
        }
    }

    @POST
    @Path("/login")
    @Transactional
    fun login(loginDTO: LoginDTO): Response {
        val token = authService.authenticate(loginDTO)
        return if (token != null) {
            return Response.ok(TokenResponse(token)).build()
        } else {
            Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build()
        }
    }

    @GET
    @RolesAllowed("ADMIN")
    @Path("/adminListUsers")
    fun listUsers2(): List<UserResponseDTO> {
        return userRepository.listAll().map { user ->
            UserResponseDTO(user.id!!, user.email, user.role)
        }
    }

    @RolesAllowed("ADMIN")
    @GET
    @Path("/testAdmin")
    fun adminOnlyEndpoint(): String {
        return "Welcome, Admin!"
    }



}
