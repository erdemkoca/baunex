package ch.baunex.user.utils

import ch.baunex.user.model.UserModel
import io.smallrye.jwt.build.Jwt
import java.sql.Date
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

object JWTUtil {
    fun generateToken2(user: UserModel): String {
        return Jwt.issuer("baunex")
            .upn(user.email)
            .claim("role", user.role.toString())
            .expiresIn(Duration.ofHours(2))
            .sign()
    }

    fun generateToken(email: String, role: String): String {
        return Jwt.claims()
            .subject(email)
            .groups(setOf(role))  // Ensures role-based access
            .issuer("baunex")
            .expiresAt(System.currentTimeMillis() + Duration.ofHours(24).toMillis())  // Token expires in 24 hours
            .sign()  // Uses configured signing key
    }

}