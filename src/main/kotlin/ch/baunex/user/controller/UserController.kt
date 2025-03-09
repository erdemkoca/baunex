package ch.baunex.user.controller

import ch.baunex.user.PasswordUtil
import ch.baunex.user.UserRepository
import ch.baunex.user.dto.UserDTO
import ch.baunex.user.dto.UserResponseDTO
import ch.baunex.user.model.UserModel
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UserController @Inject constructor(
    private val userRepository: UserRepository
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
}
