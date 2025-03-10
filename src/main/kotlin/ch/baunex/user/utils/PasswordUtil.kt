package ch.baunex.user.utils

import java.security.MessageDigest
import java.util.Base64
import org.mindrot.jbcrypt.BCrypt

object PasswordUtil {
    fun hashPasswordOld(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(password.toByteArray())
        return Base64.getEncoder().encodeToString(hashedBytes)
    }

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12)) // 12 rounds of salting
    }

    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(password, hashedPassword)
    }
}
