package ch.baunex.user.service

import ch.baunex.user.dto.UpdateUserDTO
import ch.baunex.user.dto.UserDTO
import ch.baunex.user.dto.UserResponseDTO
import ch.baunex.user.model.UserModel
import ch.baunex.user.repository.UserRepository
import ch.baunex.security.utils.PasswordUtil
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import kotlin.reflect.full.declaredMemberProperties
import jakarta.persistence.Column
import kotlin.reflect.jvm.javaField

@ApplicationScoped
class UserService @Inject constructor(
    private val userRepository: UserRepository
) {
    @Transactional
    fun registerUser(userDTO: UserDTO): UserModel {
        if (userDTO.email?.let { userRepository.findByEmail(it) } != null) {
            throw IllegalArgumentException("Email already in use")
        }

        val hashedPassword = PasswordUtil.hashPassword(userDTO.password ?: throw IllegalArgumentException("Password is required"))

        val newUser = UserModel().apply {
            email = userDTO.email ?: throw IllegalArgumentException("Email is required")
            password = hashedPassword
            role = userDTO.role
        }

        userDTO::class.declaredMemberProperties.forEach { property ->
            val value = property.getter.call(userDTO)
            val fieldName = property.name

            if (value != null && fieldName !in listOf("email", "password", "role")) {
                val userField = UserModel::class.declaredMemberProperties.find { it.name == fieldName }
                if (userField is kotlin.reflect.KMutableProperty1<*, *>) {
                    userField.setter.call(newUser, value)
                }
            }
        }

        userRepository.persist(newUser)
        return newUser
    }

    fun listUsers(): List<UserResponseDTO> {
        return userRepository.listAll().map { user ->
            UserResponseDTO(user.id!!, user.email, user.role, user.phone, user.email)
        }
    }

    fun getAllUsers(): List<UserModel> {
        return userRepository.listAll()
    }

    fun getUserById(userId: Long): UserModel? {
        return userRepository.findById(userId)
    }

    @Transactional
    fun updateUser(userId: Long, updateDTO: UpdateUserDTO): UserModel? {
        val user = userRepository.findById(userId) ?: return null  // User not found

        // Get Unique Fields
        val uniqueFields = UserModel::class.declaredMemberProperties
            .filter { prop ->
                prop.javaField?.getAnnotation(Column::class.java)?.unique == true
            }
            .map { it.name }

        updateDTO::class.declaredMemberProperties.forEach { property ->
            val value = property.getter.call(updateDTO) // Get field value dynamically

            if (value != null) { // Update only if field is provided
                val fieldName = property.name

                // Check Uniqueness Dynamically
                if (fieldName in uniqueFields) {
                    val existingUser = userRepository.findUniqueField(fieldName, value.toString())
                    if (existingUser != null && existingUser.id != userId) {
                        throw IllegalArgumentException("$fieldName is already in use")
                    }
                }

                // Update Field Dynamically
                val userField = UserModel::class.declaredMemberProperties.find { it.name == fieldName }
                if (userField is kotlin.reflect.KMutableProperty1<*, *>) {
                    userField.setter.call(user, value)
                }
            }
        }

        userRepository.updateUser(user)
        return user
    }
}
