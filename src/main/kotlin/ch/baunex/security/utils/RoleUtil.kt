package ch.baunex.security.utils

import ch.baunex.user.repository.UserRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.SecurityContext

@ApplicationScoped
class RoleUtil @Inject constructor(
    private val userRepository: UserRepository
) {
    fun hasRole(securityContext: SecurityContext, vararg requiredRoles: String): Boolean {
        val email = securityContext.userPrincipal?.name ?: return false
        val userRole = userRepository.getUserRole(email) ?: return false

        return requiredRoles.contains(userRole)
    }
}
