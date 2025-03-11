package ch.baunex.user.facade

import ch.baunex.user.dto.LoginDTO
import ch.baunex.user.dto.UserDTO
import ch.baunex.user.dto.UserResponseDTO
import ch.baunex.user.service.AuthService
import ch.baunex.user.service.UserService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class UserFacade @Inject constructor(
    private val userService: UserService,
    private val authService: AuthService
) {
    fun registerUser(userDTO: UserDTO): UserResponseDTO {
        val user = userService.registerUser(userDTO)
        return UserResponseDTO(user.id!!, user.email, user.role)
    }

    fun authenticateUser(loginDTO: LoginDTO): String? {
        return authService.authenticate(loginDTO)
    }
}
