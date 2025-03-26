package ch.baunex.user.facade

import ch.baunex.user.dto.*
import ch.baunex.user.model.UserModel
import ch.baunex.user.repository.UserRepository
import ch.baunex.security.service.AuthService
import ch.baunex.user.service.UserService
import ch.baunex.security.utils.PasswordUtil
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
        val user = userService.registerUser(userDTO)
        return UserResponseDTO(user.id, user.email, user.role, user.phone, user.email)
    }

    fun listUsers(): List<UserResponseDTO> {
        return userService.listUsers()
    }

    fun getAllUsers(): List<UserResponseDTO> {
        return userService.getAllUsers().map { user ->
            UserResponseDTO(
                id = user.id,
                email = user.email,
                role = user.role,
                phone = user.phone,
                street = user.street
            )
        }
    }


    fun authenticate(loginDTO: LoginDTO): Pair <String, String>? {
        val user = userRepository.findByEmail(loginDTO.email) ?: return null

        return if (PasswordUtil.verifyPassword(loginDTO.password, user.password)) {
            authService.authenticate(user.email, user.password)
        } else {
            null
        }
    }

    fun updateUser(userId: Long, updateDTO: UpdateUserDTO): UserResponseDTO? {
        val user = userService.updateUser(userId, updateDTO) ?: return null
        return UserResponseDTO(user.id, user.email, user.role, user.phone, user.street)
    }

    fun getUserById(userId: Long): UserResponseDTO? {
        val user = userService.getUserById(userId) ?: return null
        return UserResponseDTO(user.id, user.email, user.role, user.phone, user.street)
    }

    fun getUserByMail(mail: String): UserResponseDTO? {
        val user = userService.getUserByMail(mail) ?: return null
        return UserResponseDTO(user.id, user.email, user.role, user.phone, user.street)
    }


    fun deleteUserByMail(mail: String): UserResponseDTO? {
        val user = userService.deleteUserByMail(mail) ?: return null
        return UserResponseDTO(user.id, user.email, user.role, user.phone, user.street)
    }


    fun deleteUserById(userId: Long) {
        return userService.deleteUserById(userId)
    }

    fun updateUserRole(userId: Long, role: Role): UserResponseDTO? {
        return userService.updateUserRole(userId, role)
    }

    @Transactional
    fun deleteAllUsers() {
        userService.deleteAllUsers()
    }

    fun deleteAllUsersExceptSuperadmin() {
        userService.deleteAllUsersExceptSuperadmin()
    }



}
