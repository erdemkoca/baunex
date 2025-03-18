package ch.baunex.user.facade

import ch.baunex.user.dto.*
import ch.baunex.user.model.UserModel
import ch.baunex.user.repository.UserRepository
import ch.baunex.security.service.AuthService
import ch.baunex.user.service.UserService
import ch.baunex.security.utils.PasswordUtil
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class UserFacade @Inject constructor(
    private val userService: UserService,
    private val authService: AuthService,
    private val userRepository: UserRepository

) {
    fun registerUser(userDTO: UserDTO): UserResponseDTO {
        val user = userService.registerUser(userDTO)
        return UserResponseDTO(user.id, user.email, user.role, user.phone, user.email)
    }

    fun listUsers(): List<UserResponseDTO> {
        return userService.listUsers()
    }

    fun getAllUsers(): List<UserModel> {
        return userService.getAllUsers()
    }

    fun authenticate(loginDTO: LoginDTO): String? {
        val user = userRepository.findByEmail(loginDTO.email) ?: return null

        return if (PasswordUtil.verifyPassword(loginDTO.password, user.password)) {
            authService.authenticate(loginDTO)
        } else {
            null
        }
    }

    fun updateUser(userId: Long, updateDTO: UpdateUserDTO): UserModel? {
        return userService.updateUser(userId, updateDTO)
    }

    fun getUserById(userId: Long): UserModel? {
        return userService.getUserById(userId)
    }


}
