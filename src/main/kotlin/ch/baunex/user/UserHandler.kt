package ch.baunex.user

import ch.baunex.user.dto.UpdateUserDTO
import ch.baunex.user.dto.UserDTO
import ch.baunex.user.dto.UserResponseDTO
import ch.baunex.user.model.Role
import ch.baunex.user.model.UserModel
import ch.baunex.user.repository.UserRepository
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@RequestScoped
class UserHandler {

    @Inject
    lateinit var userRepository: UserRepository

    @Transactional
    fun saveUser(dto: UserDTO): UserResponseDTO {
        val user = UserModel().apply {
            email = dto.email!!
            password = dto.password!!
            role = dto.role ?: Role.USER
            phone = dto.phone
            street = dto.street
        }
        userRepository.persist(user)
        return UserResponseDTO(user.id, user.email, user.role, user.phone, user.street)
    }



    fun getAllUsers(): List<UserResponseDTO> {
        return userRepository.findAll()
            .list<UserModel>()
            .map { user ->
                UserResponseDTO(
                    id = user.id,
                    email = user.email,
                    role = user.role,
                    phone = user.phone,
                    street = user.street
                )
            }
    }



    fun getUserById(id: Long): UserResponseDTO? {
        return userRepository.findById(id)?.let {
            UserResponseDTO(it.id, it.email, it.role, it.phone, it.street)
        }
    }


    @Transactional
    fun updateUser(id: Long, dto: UpdateUserDTO): UserResponseDTO? {
        val user = userRepository.findById(id) ?: return null

        dto.email?.let { user.email = it }
        dto.password?.let { user.password = it }
        dto.phone?.let { user.phone = it }
        dto.street?.let { user.street = it }

        userRepository.persist(user)
        return UserResponseDTO(user.id, user.email, user.role, user.phone, user.street)
    }


    @Transactional
    fun deleteUser(id: Long): Boolean {
        return userRepository.deleteById(id)
    }
}
