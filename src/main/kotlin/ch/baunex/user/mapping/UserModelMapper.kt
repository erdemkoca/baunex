package ch.baunex.user.mapping

import ch.baunex.user.dto.UpdateUserDTO
import ch.baunex.user.dto.UserDTO
import ch.baunex.user.dto.UserResponseDTO
import ch.baunex.user.model.UserModel

fun UserModel.toUserResponseDTO() = UserResponseDTO(
    id = this.id!!,
    email = this.email,
    role = this.role,
    phone = this.phone,
    street = this.street
)

fun UserDTO.toUserModel(): UserModel {
    return UserModel().apply {
        email = this@toUserModel.email
        password = this@toUserModel.password ?: ""
        role = this@toUserModel.role
        phone = this@toUserModel.phone
        street = this@toUserModel.street
        city = this@toUserModel.city
    }
}

fun UpdateUserDTO.toUserModel(): UserModel {
    val user = UserModel()
    this.email?.let { user.email = it }
    this.password?.let { user.password = it }
    this.role?.let { user.role = it }
    this.phone?.let { user.phone = it }
    this.street?.let { user.street = it }
    return user
}
