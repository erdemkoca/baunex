package ch.baunex.security.interceptor

import ch.baunex.security.utils.JWTUtil

fun main() {
    val adminToken = JWTUtil.generateToken("admin@example.com", 1L, "ADMIN") //admin@baunex.ch
    println("Hardcoded Admin Token: $adminToken")
}
