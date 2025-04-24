package ch.baunex.user.facade

import ch.baunex.user.dto.*
import ch.baunex.user.repository.UserRepository
import ch.baunex.security.service.AuthService
import ch.baunex.user.service.UserService
import ch.baunex.security.utils.PasswordUtil
import ch.baunex.user.mapping.toUserModel
import ch.baunex.user.mapping.toUserResponseDTO
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
        val user = userService.registerUser(userDTO.toUserModel())
        return user.toUserResponseDTO()
    }

    fun listUsers(): List<UserResponseDTO> {
        return userService.getAllUsers().map { it.toUserResponseDTO() }
    }

    fun getAllUsers(): List<UserResponseDTO> {
        return userService.getAllUsers().map { it.toUserResponseDTO() }
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
        val updatedModel = updateDTO.toUserModel()
        val updatedUser = userService.updateUser(userId, updatedModel)
        return updatedUser?.toUserResponseDTO()
    }


    fun getUserById(userId: Long): UserResponseDTO? {
        return userService.getUserById(userId)?.toUserResponseDTO()
    }

    fun getUserByMail(mail: String): UserResponseDTO? {
        return userService.getUserByMail(mail)?.toUserResponseDTO()
    }

    fun deleteUserByMail(mail: String): UserResponseDTO? {
        return userService.deleteUserByMail(mail)?.toUserResponseDTO()
    }

    fun deleteUserById(userId: Long) {
        userService.deleteUserById(userId)
    }

    fun updateUserRole(userId: Long, role: Role): UserResponseDTO? {
        return userService.updateUserRole(userId, role)?.toUserResponseDTO()
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
