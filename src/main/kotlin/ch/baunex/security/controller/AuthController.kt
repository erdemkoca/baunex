package ch.baunex.user.controller

import ch.baunex.security.dto.AuthResponse
import ch.baunex.security.dto.RefreshTokenDTO
import ch.baunex.security.service.AuthService
import ch.baunex.user.dto.LoginDTO
import ch.baunex.user.facade.UserFacade
import jakarta.inject.Inject
import jakarta.resource.spi.work.SecurityContext
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import ch.baunex.user.dto.*
import jakarta.ws.rs.*
import jakarta.ws.rs.core.*

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AuthController @Inject constructor(
    private val userFacade: UserFacade,
    private val authService: AuthService
) {
    @POST
    @Path("/login")
    @Transactional
    fun login(loginDTO: LoginDTO): Response {
        val tokens = authService.authenticate(loginDTO.email, loginDTO.password)
        return if (tokens != null) {
            Response.ok(AuthResponse(tokens.first, tokens.second)).build()
        } else {
            Response.status(Response.Status.UNAUTHORIZED).entity(MessageResponse("Invalid credentials")).build()
        }
    }

    @POST
    @Path("/logout")
    fun logout(@Context securityContext: SecurityContext): Response {
        // Implement JWT invalidation logic if needed
        return Response.ok(MessageResponse("Logged out successfully")).build()
    }

    @POST
    @Path("/refresh-token")
    fun refreshToken(refreshTokenDTO: RefreshTokenDTO): Response {
        val tokens = authService.refreshToken(refreshTokenDTO)
        return if (tokens != null) {
            Response.ok(AuthResponse(tokens.first, tokens.second)).build()
        } else {
            Response.status(Response.Status.UNAUTHORIZED).entity(MessageResponse("Invalid refresh token")).build()
        }
    }

}
