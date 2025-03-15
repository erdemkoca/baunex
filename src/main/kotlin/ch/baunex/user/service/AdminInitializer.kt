import ch.baunex.user.model.Role
import ch.baunex.user.model.UserModel
import ch.baunex.user.repository.UserRepository
import ch.baunex.security.utils.PasswordUtil
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
    //TODO not used form config
) {

    fun onStart(@Observes event: StartupEvent) {
        createSuperAdminIfNotExists()
    }

    @Transactional
    fun createSuperAdminIfNotExists() {
        val existingUser = userRepository.findByEmail("superadmin@example.com")
        println("🔍 Checking if superadmin exists: $existingUser")

        if (existingUser == null) {
            val hashedPassword = PasswordUtil.hashPassword("superadminpassword")
            val superAdmin = UserModel().apply {
                email = "superadmin@example.com"
                password = hashedPassword
                role = Role.SUPERADMIN
            }
            userRepository.persist(superAdmin)
            println("Superadmin created successfully")
        } else {
            println("Superadmin already exists")
        }
    }

}
