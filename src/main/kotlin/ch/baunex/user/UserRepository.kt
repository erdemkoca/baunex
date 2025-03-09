package ch.baunex.user


import ch.baunex.user.model.UserModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class UserRepository : PanacheRepository<UserModel> {
    fun findByEmail(email: String): UserModel? {
        return find("email", email).firstResult()
    }
}