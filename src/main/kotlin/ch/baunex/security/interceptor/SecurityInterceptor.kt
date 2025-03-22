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
import jakarta.ws.rs.core.*
import jakarta.ws.rs.ext.Provider
import java.security.Principal

@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
class SecurityInterceptor @Inject constructor(
    private val roleUtil: RoleUtil
) : ContainerRequestFilter {

    init {
        println("🔄 SecurityInterceptor Initialized!")
    }

    @Context
    private lateinit var headers: HttpHeaders

    private fun isTestMode(): Boolean {
        val isTest = System.getProperty("quarkus.profile") == "test"
        println("🧪 isTestMode() called - Result: $isTest")
        return isTest
    }



    override fun filter(requestContext: ContainerRequestContext) {
        println("🚀 SecurityInterceptor FILTER STARTED for ${requestContext.uriInfo.path}") // Debugging

        if (isTestMode()) {
            println("⚠️ SecurityInterceptor is disabled in test mode")
            return
        }

        // Check if request is an authentication request
        val requestUri = requestContext.uriInfo.path
        if (requestUri.startsWith("/api/auth")) {
            println("🔓 Skipping auth check for authentication request: $requestUri")
            return
        }

        val authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)
        println("🔍 Received Authorization Header: $authorizationHeader")

        if (authorizationHeader == null) {
            println("🛑 No Authorization header provided")
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Missing or invalid Authorization header").build()
            )
            return
        }

        try {
            val claims: Claims = JWTUtil.parseToken(authorizationHeader.substring(7).trim())
            println("✅ Token successfully parsed: ${claims.subject}, Role: ${claims["role"]}")

            requestContext.securityContext = object : SecurityContext {
                override fun getUserPrincipal(): Principal = Principal { claims.subject }
                override fun isUserInRole(role: String?): Boolean {
                    println("🔍 Checking Role: Expected=$role, Found=${claims["role"]}")
                    return role == claims["role"]
                }
                override fun isSecure(): Boolean = requestContext.securityContext.isSecure
                override fun getAuthenticationScheme(): String = "Bearer"
            }
        } catch (e: Exception) {
            println("❌ Token rejected: ${e.message}")
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("""{"message": "Invalid or expired token"}""").type(MediaType.APPLICATION_JSON).build()
            )
        }
    }
}
