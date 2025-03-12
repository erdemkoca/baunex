package ch.baunex.user.utils

import ch.baunex.user.model.UserModel
import io.smallrye.jwt.build.Jwt
import java.time.Duration

object JWTUtil {

    fun generateToken(email: String, role: String): String {
        return Jwt.claims()
            .subject(email)
            .groups(setOf(role))  // Ensures role-based access
            .issuer("baunex")
            .expiresAt(System.currentTimeMillis() + Duration.ofHours(24).toMillis())
            .sign()  // Uses configured signing key
    }

}