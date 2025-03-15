package ch.baunex.security.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
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
    private val publicKey: PublicKey = loadPublicKey() // ✅ Load public key

    private const val EXPIRATION_TIME: Long = 1000 * 60 * 60 * 24

    fun generateToken(email: String, userId: Long, role: String): String {
        val claims = mapOf(
            "sub" to email,
            "id" to userId,
            "role" to role
        )

        return Jwts.builder()
            .setClaims(claims)
            .setIssuer("baunex")
            .setExpiration(Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .setIssuedAt(Date(System.currentTimeMillis()))
            .signWith(privateKey, io.jsonwebtoken.SignatureAlgorithm.RS256)
            .compact()
    }

    fun parseToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(publicKey)
            .build()
            .parseClaimsJws(token)
            .body
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
