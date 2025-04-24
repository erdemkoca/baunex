package ch.baunex.user.service

import ch.baunex.user.model.Role
import ch.baunex.user.model.UserModel
import ch.baunex.user.repository.UserRepository
import ch.baunex.security.utils.PasswordUtil
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class UserService @Inject constructor(
    private val userRepository: UserRepository
) {
    @Transactional
    fun registerUser(user: UserModel): UserModel {
        if (userRepository.findByEmail(user.email) != null) {
            throw IllegalArgumentException("Email already in use")
        }

        user.password = PasswordUtil.hashPassword(user.password)
        val rate = user.hourlyRate ?: 150.0
        userRepository.persist(user)
        return user
    }

    fun getAllUsers(): List<UserModel> = userRepository.listAll()

    fun getUserById(userId: Long): UserModel? = userRepository.findById(userId)

    fun getUserByMail(mail: String): UserModel? = userRepository.findByEmail(mail)

    fun deleteUserByMail(mail: String): UserModel? {
        val user = getUserByMail(mail)
        user?.let { userRepository.delete(it) }
        return user
    }

    fun deleteUserById(id: Long) {
        getUserById(id)?.let { userRepository.delete(it) }
    }

    @Transactional
    fun updateUser(userId: Long, updated: UserModel): UserModel? {
        val user = userRepository.findById(userId) ?: return null

        // Enforce unique constraints
        if (updated.email != user.email && userRepository.findByEmail(updated.email) != null) {
            throw IllegalArgumentException("Email is already in use")
        }

        if (updated.phone != null && updated.phone != user.phone) {
            val existing = userRepository.findUniqueField("phone", updated.phone!!)
            if (existing != null && existing.id != userId) {
                throw IllegalArgumentException("Phone number is already in use")
            }
        }

        // Update fields manually (preferred over reflection)
        user.email = updated.email
        user.role = updated.role
        user.street = updated.street
        user.city = updated.city
        user.phone = updated.phone

        // Optional: only update password if a new one is provided
        if (!updated.password.isNullOrBlank()) {
            user.password = PasswordUtil.hashPassword(updated.password)
        }

        userRepository.updateUser(user)
        return user
    }

    //TODO is transactional necessary here?
    fun existsByEmail(email: String): Boolean {
        return userRepository.findByEmail(email) != null
    }


    @Transactional
    fun updateUserRole(userId: Long, role: Role): UserModel? {
        val user = userRepository.findById(userId) ?: return null
        user.role = role
        userRepository.updateUser(user)
        return user
    }

    @Transactional
    fun deleteAllUsers() {
        userRepository.deleteAll()
    }

    @Transactional
    fun deleteAllUsersExceptSuperadmin() {
        userRepository.delete("email != ?1", "superadmin@example.com")
    }
}
