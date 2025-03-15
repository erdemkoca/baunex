package ch.baunex.user.test

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.ValidatableResponse
import org.hamcrest.Matchers
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserAuthenticationTest {

    companion object {
        private const val BASE_URL = "/api/users"
        private const val TEST_EMAIL = "testuser@example.com"
        private const val TEST_PASSWORD = "testpassword"
        private var jwtToken: String? = null
    }

    @Test
    @Order(1)
    fun `should register a new user for authentication tests`() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "email": "$TEST_EMAIL",
                    "password": "$TEST_PASSWORD",
                    "role": "USER"
                }
                """.trimIndent()
            )
            .post("$BASE_URL")
            .then()
            .statusCode(201)
    }

    @Test
    @Order(2)
    fun `should authenticate user and return JWT token`() {
        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "email": "$TEST_EMAIL",
                    "password": "$TEST_PASSWORD"
                }
                """.trimIndent()
            )
            .post("$BASE_URL/login")
            .then()
            .statusCode(200)
            .body("token", Matchers.notNullValue())

        // Extract token for future requests
        jwtToken = response.extract().path("token")
    }

    @Test
    @Order(3)
    fun `should reject authentication with incorrect password`() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "email": "$TEST_EMAIL",
                    "password": "wrongpassword"
                }
                """.trimIndent()
            )
            .post("$BASE_URL/login")
            .then()
            .statusCode(401) // Unauthorized
            .body("message", Matchers.equalTo("Invalid credentials"))
    }

    @Test
    @Order(4)
    fun `should reject authentication for non-existent user`() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "email": "nonexistent@example.com",
                    "password": "randompassword"
                }
                """.trimIndent()
            )
            .post("$BASE_URL/login")
            .then()
            .statusCode(401) // Unauthorized
            .body("message", Matchers.equalTo("Invalid credentials"))
    }

    @Test
    @Order(5)
    fun `should access protected endpoint with valid JWT token`() {
        RestAssured.given()
            .header("Authorization", "Bearer $jwtToken")
            .get("$BASE_URL/testAdmin") // Replace with an actual protected endpoint
            .then()
            .statusCode(403) // This should return 403 because the user is not an admin
    }

    @Test
    @Order(6)
    fun `should reject protected endpoint access without JWT token`() {
        RestAssured.given()
            .get("$BASE_URL/testAdmin") // Replace with an actual protected endpoint
            .then()
            .statusCode(401) // Unauthorized (missing token)
    }
}
