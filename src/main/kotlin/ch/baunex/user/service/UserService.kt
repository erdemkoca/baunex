package ch.baunex.user.service

import ch.baunex.user.model.Role
import ch.baunex.user.model.UserModel
import ch.baunex.user.repository.UserRepository
import ch.baunex.user.utils.PasswordUtil
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class UserService @Inject constructor(
    private val userRepository: UserRepository
) {
    @Transactional
    fun registerUser(email: String, password: String, role: Role): UserModel {
        val hashedPassword = PasswordUtil.hashPassword(password)
        val user = UserModel(email, hashedPassword, role)
        userRepository.persist(user)
        return user
    }

    fun getUserByEmail(email: String): UserModel? {
        return userRepository.findByEmail(email)
    }
}