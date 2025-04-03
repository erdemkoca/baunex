package ch.baunex.user.mapping

import ch.baunex.user.dto.UpdateUserDTO
import ch.baunex.user.dto.UserDTO
import ch.baunex.user.dto.UserResponseDTO
import ch.baunex.user.model.UserModel

fun UserModel.toResponseDTO() = UserResponseDTO(
    id = this.id!!,
    email = this.email,
    role = this.role,
    phone = this.phone,
    street = this.street
)

fun UserDTO.toModel(): UserModel {
    return UserModel().apply {
        email = this@toModel.email
        password = this@toModel.password ?: ""
        role = this@toModel.role
        phone = this@toModel.phone
        street = this@toModel.street
        city = this@toModel.city
    }
}

fun UpdateUserDTO.toModel(): UserModel {
    val user = UserModel()
    this.email?.let { user.email = it }
    this.password?.let { user.password = it }
    this.role?.let { user.role = it }
    this.phone?.let { user.phone = it }
    this.street?.let { user.street = it }
    //this.city?.let { user.city = it }
    return user
}
