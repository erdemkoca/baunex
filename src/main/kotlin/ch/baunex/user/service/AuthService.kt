package ch.baunex.user.service


import ch.baunex.user.utils.PasswordUtil
import ch.baunex.user.repository.UserRepository
import ch.baunex.user.dto.LoginDTO
import ch.baunex.user.utils.JWTUtil
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class AuthService @Inject constructor(
    private val userRepository: UserRepository
) {
    fun authenticate(loginDTO: LoginDTO): String? {
        val user = userRepository.findByEmail(loginDTO.email) ?: return null
        return if (PasswordUtil.verifyPassword(loginDTO.password, user.password)) {
            JWTUtil.generateToken(user.email, user.role.name)
        } else {
            null
        }
    }
}
