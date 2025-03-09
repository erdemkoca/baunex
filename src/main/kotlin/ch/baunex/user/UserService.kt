package ch.baunex.user

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class UserService @Inject constructor(
    private val userRepository: UserRepository
) {
    @Transactional
    fun registerUser(email: String, password: String, role: RoleModel): UserModel {
        val hashedPassword = PasswordUtil.hashPassword(password)
        val user = UserModel(email, hashedPassword, role)
        userRepository.persist(user)
        return user
    }

    fun getUserByEmail(email: String): UserModel? {
        return userRepository.findByEmail(email)
    }
}