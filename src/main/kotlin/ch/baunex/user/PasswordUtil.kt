package ch.baunex.user

import java.security.MessageDigest
import java.util.Base64

object PasswordUtil {
    fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(password.toByteArray())
        return Base64.getEncoder().encodeToString(hashedBytes)
    }
}