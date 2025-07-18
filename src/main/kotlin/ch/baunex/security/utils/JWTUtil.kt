package ch.baunex.security.utils

import io.jsonwebtoken.*
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

object JWTUtil {
    private val privateKey: PrivateKey = loadPrivateKey()
    private val publicKey: PublicKey = loadPublicKey()

    private const val EXPIRATION_TIME: Long = 1000 * 60 * 60 * 24 // 24 hours

    fun generateToken(email: String, userId: Long, role: String, expirationMillis: Long = EXPIRATION_TIME): String {
        val issuedAt = Date(System.currentTimeMillis())
        val expiration = Date(System.currentTimeMillis() + expirationMillis)

        val token = Jwts.builder()
            .setSubject(email)
            .claim("id", userId)
            .claim("role", role)
            .setIssuer("baunex")
            .setIssuedAt(issuedAt)
            .setExpiration(expiration)
            .signWith(privateKey, SignatureAlgorithm.RS256)
            .compact()

        println("🔑 Generated Token for $email (Role: $role, Expiration: $expirationMillis ms): $token")
        return token
    }

    fun parseToken(token: String): Claims {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .body

            println("✅ Parsed Token: email=${claims.subject}, id=${claims["id"]}, role=${claims["role"]}, exp=${claims.expiration}")
            claims
        } catch (e: ExpiredJwtException) {
            println("❌ Token Expired: ${e.message}")
            throw e
        } catch (e: SecurityException) {
            println("❌ Invalid Token Signature: ${e.message}")
            throw e
        } catch (e: Exception) {
            println("❌ JWT Parsing Failed: ${e.message}")
            throw e
        }
    }

    private fun loadPrivateKey(): PrivateKey {
        val keyBytes = Files.readAllBytes(Paths.get("src/main/resources/private.pem"))
        val keyString = String(keyBytes, Charsets.UTF_8)
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyString))
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }

    private fun loadPublicKey(): PublicKey {
        val keyBytes = Files.readAllBytes(Paths.get("src/main/resources/public.pem"))
        val keyString = String(keyBytes, Charsets.UTF_8)
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")

        val keySpec = X509EncodedKeySpec(Base64.getDecoder().decode(keyString))
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }
}
