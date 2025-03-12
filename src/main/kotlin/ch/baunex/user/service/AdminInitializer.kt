import ch.baunex.user.model.Role
import ch.baunex.user.model.UserModel
import ch.baunex.user.repository.UserRepository
import ch.baunex.user.utils.PasswordUtil
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty


@ApplicationScoped
class AdminInitializer @Inject constructor(
    private val userRepository: UserRepository,
    @ConfigProperty(name = "superadmin.email") private val superAdminEmail: String,
    @ConfigProperty(name = "superadmin.password") private val superAdminPassword: String
) {

    fun onStart(@Observes event: StartupEvent) {
        createSuperAdminIfNotExists()
    }

    @Transactional
    fun createSuperAdminIfNotExists() {
        if (userRepository.findByEmail("superadmin@example.com") == null) {
            val hashedPassword = PasswordUtil.hashPassword("superadminpassword")
            val superAdmin = UserModel("superadmin@example.com", hashedPassword, Role.SUPERADMIN)
            userRepository.persist(superAdmin)
        }
    }
}
