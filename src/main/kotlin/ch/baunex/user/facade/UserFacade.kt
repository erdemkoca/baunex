package ch.baunex.user.facade

import ch.baunex.user.dto.*
import ch.baunex.user.repository.UserRepository
import ch.baunex.security.service.AuthService
import ch.baunex.user.service.UserService
import ch.baunex.security.utils.PasswordUtil
import ch.baunex.user.mapping.toModel
import ch.baunex.user.mapping.toResponseDTO
import ch.baunex.user.model.Role
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class UserFacade @Inject constructor(
    private val userService: UserService,
    private val authService: AuthService,
    private val userRepository: UserRepository
) {
    fun registerUser(userDTO: UserDTO): UserResponseDTO {
        val user = userService.registerUser(userDTO.toModel())
        return user.toResponseDTO()
    }

    fun listUsers(): List<UserResponseDTO> {
        return userService.getAllUsers().map { it.toResponseDTO() }
    }

    fun getAllUsers(): List<UserResponseDTO> {
        return userService.getAllUsers().map { it.toResponseDTO() }
    }

    fun authenticate(loginDTO: LoginDTO): Pair<String, String>? {
        val user = userRepository.findByEmail(loginDTO.email) ?: return null
        return if (PasswordUtil.verifyPassword(loginDTO.password, user.password)) {
            authService.authenticate(user.email, user.password)
        } else {
            null
        }
    }

    fun updateUser(userId: Long, updateDTO: UpdateUserDTO): UserResponseDTO? {
        val updatedModel = updateDTO.toModel()
        val updatedUser = userService.updateUser(userId, updatedModel)
        return updatedUser?.toResponseDTO()
    }


    fun getUserById(userId: Long): UserResponseDTO? {
        return userService.getUserById(userId)?.toResponseDTO()
    }

    fun getUserByMail(mail: String): UserResponseDTO? {
        return userService.getUserByMail(mail)?.toResponseDTO()
    }

    fun deleteUserByMail(mail: String): UserResponseDTO? {
        return userService.deleteUserByMail(mail)?.toResponseDTO()
    }

    fun deleteUserById(userId: Long) {
        userService.deleteUserById(userId)
    }

    fun updateUserRole(userId: Long, role: Role): UserResponseDTO? {
        return userService.updateUserRole(userId, role)?.toResponseDTO()
    }

    fun existsByEmail(email: String): Boolean {
        return userService.existsByEmail(email)
    }


    @Transactional
    fun deleteAllUsers() {
        userService.deleteAllUsers()
    }

    fun deleteAllUsersExceptSuperadmin() {
        userService.deleteAllUsersExceptSuperadmin()
    }
}
