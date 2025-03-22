package ch.baunex.user.repository

import ch.baunex.user.model.UserModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@ApplicationScoped
class UserRepository : PanacheRepository<UserModel> {
    fun findByEmail(email: String): UserModel? {
        return find("email", email).firstResult()
    }
    fun findByEmail2(email: String): UserModel? {
        return find("SELECT u FROM UserModel u WHERE u.email = ?1", email).firstResult()
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

    fun getAllUsers(): List<UserModel> {
        return listAll()
    }

    fun findByRefreshToken(refreshToken: String): UserModel? {
        return find("refreshToken", refreshToken).firstResult()
    }

    @Transactional
    override fun deleteAll(): Long {
        return delete("1=1")  // Deletes all users and returns count
    }


}
