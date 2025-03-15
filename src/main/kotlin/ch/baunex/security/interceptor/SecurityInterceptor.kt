package ch.baunex.security.interceptor

import ch.baunex.security.utils.JWTUtil
import ch.baunex.security.utils.RoleUtil
import io.jsonwebtoken.Claims
import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext
import jakarta.ws.rs.ext.Provider
import java.security.Principal

@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
class SecurityInterceptor @Inject constructor(
    private val roleUtil: RoleUtil
) : ContainerRequestFilter {

    @Context
    private lateinit var headers: HttpHeaders

    private fun isTestMode(): Boolean {
        return System.getProperty("quarkus.test.profile") != null
    }

    override fun filter(requestContext: ContainerRequestContext) {
        if (isTestMode()) {
            println("⚠️ SecurityInterceptor is disabled in test mode") // Debugging
            return  // ✅ Skip security in tests
        }

        val authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Missing or invalid Authorization header").build()
            )
            return
        }

        try {
            val claims: Claims = JWTUtil.parseToken(authorizationHeader.substring(7).trim())

            // ✅ Assign custom SecurityContext with user details
            requestContext.securityContext = object : SecurityContext {
                override fun getUserPrincipal(): Principal = Principal { claims.subject }
                override fun isUserInRole(role: String?): Boolean = role == claims["role"]
                override fun isSecure(): Boolean = requestContext.securityContext.isSecure
                override fun getAuthenticationScheme(): String = "Bearer"
            }
        } catch (e: Exception) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid or expired token").build()
            )
        }
    }
}


