package ch.baunex.user.repository

import ch.baunex.user.model.UserModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class UserRepository : PanacheRepository<UserModel> {
    fun findByEmail(email: String): UserModel? {
        return find("email", email).firstResult()
    }

    fun getUserRole(email: String): String? {
        return find("email", email).firstResult<UserModel>()?.role?.name
    }

    fun findUniqueField(fieldName: String, fieldValue: String): UserModel? {
        return find("$fieldName = ?1", fieldValue).firstResult()
    }

    fun updateUser(user: UserModel) {
        persist(user)
    }

}
