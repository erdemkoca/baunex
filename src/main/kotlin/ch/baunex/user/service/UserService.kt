package ch.baunex.user.service

import ch.baunex.user.dto.UserDTO
import ch.baunex.user.dto.UserResponseDTO
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
    fun registerUser(userDTO: UserDTO): UserResponseDTO {
        val hashedPassword = PasswordUtil.hashPassword(userDTO.password)
        val newUser = UserModel(userDTO.email, hashedPassword, userDTO.role)
        userRepository.persist(newUser)
        return UserResponseDTO(newUser.id!!, newUser.email, newUser.role)
    }


    fun listUsers(): List<UserResponseDTO> {
        return userRepository.listAll().map { user ->
            UserResponseDTO(user.id!!, user.email, user.role)
        }
    }
}