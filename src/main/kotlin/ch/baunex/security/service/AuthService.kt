//package ch.baunex.security.service
//
//
//import ch.baunex.security.dto.RefreshTokenDTO
//import ch.baunex.security.utils.PasswordUtil
//import ch.baunex.user.dto.LoginDTO
//import ch.baunex.security.utils.JWTUtil
//import jakarta.enterprise.context.ApplicationScoped
//import jakarta.inject.Inject
//import jakarta.transaction.Transactional
//import java.util.*
//
//@ApplicationScoped
//class AuthService @Inject constructor(
//    private val userRepository: UserRepository
//) {
//    fun authenticate(email: String, password: String): Pair<String, String>? {
//        val user = userRepository.findByEmail(email) ?: return null
//        if (!PasswordUtil.verifyPassword(password, user.password)) return null
//
//        val accessToken = JWTUtil.generateToken(user.email, user.id, user.role.name)
//        val refreshToken = UUID.randomUUID().toString() // Generate refresh token
//
//        user.refreshToken = refreshToken  // Store in DB
//        userRepository.updateUser(user)
//
//        return Pair(accessToken, refreshToken) // Return both tokens
//    }
//
//
//    //TODO refresh unneccesary,
//    @Transactional
//    fun refreshToken(refreshTokenDTO: RefreshTokenDTO): Pair<String, String>? {
//        val user = userRepository.findByRefreshToken(refreshTokenDTO.refreshToken) ?: return null
//
//        val newAccessToken = JWTUtil.generateToken(user.email, user.id, user.role.name)
//        val newRefreshToken = UUID.randomUUID().toString()
//
//        user.refreshToken = newRefreshToken  // Replace old token
//        userRepository.updateUser(user)
//
//        return Pair(newAccessToken, newRefreshToken) // Return new tokens
//    }
//
//}
