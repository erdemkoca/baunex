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

    private val adminHardcodedToken = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlkIjoxLCJyb2xlIjoiQURNSU4iLCJpc3MiOiJiYXVuZXgiLCJleHAiOjE3NDIzNDEyMTcsImlhdCI6MTc0MjI1NDgxN30.rFIpNuZmZ7VWsMBzOROrbVV0n1XK_81ntwUsJrSyaLyVva0uDHpsTFurkZ6wnnk395NPh_pwSn49lOixcgXaMoj0npOGKmbBGv3bTwLMx7cwEkSKWY9aWn4E_hWJUsNd3ASKC79cyVmsweqmm0mqEMujhbUBGU2hUtdTn0w1Q1NfShz6HJ_k4sfdgDrh_lZ88g4uHNpOeYwvq8_QAa5LaSbP4lePUTPQE0WZB9Q7Q6gDGZXCbJG_lqB5tiJJjso_kH-vLaPUEp3ZpJZJRTFvL_vNNjq2coHxkqtAhjM7_wdZy5f4oMGyMlejOysCREGGe5tpzq3P_8iuVCA_0MZu-w"

    private fun isTestMode(): Boolean {
        return System.getProperty("quarkus.test.profile") != null
    }

    override fun filter(requestContext: ContainerRequestContext) {
        if (isTestMode()) {
            println("⚠️ SecurityInterceptor is disabled in test mode") // Debugging
            return
        }

        val requestUri = requestContext.uriInfo.path

        //TODO Allow Superadmin login without authentication and JWT hardcoded token
        //todo string compare
        if (requestUri == "/api/users/login") {
            return
        }

        val authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)

        // ✅ Check for the hardcoded admin token
        if (authorizationHeader == "Bearer $adminHardcodedToken") {
            requestContext.securityContext = object : SecurityContext {
                override fun getUserPrincipal(): Principal = Principal { "admin@example.com" } // admin@baunex.ch
                override fun isUserInRole(role: String?): Boolean = role == "ADMIN"
                override fun isSecure(): Boolean = requestContext.securityContext.isSecure
                override fun getAuthenticationScheme(): String = "Bearer"
            }
            return
        }

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Missing or invalid Authorization header").build()
                //TODO redirect to login
            )
            return
        }

        try {
            val claims: Claims = JWTUtil.parseToken(authorizationHeader.substring(7).trim())

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


