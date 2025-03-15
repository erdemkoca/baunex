package ch.baunex.user.test

import ch.baunex.user.dto.UserDTO
import ch.baunex.user.model.Role
import ch.baunex.user.repository.UserRepository
import ch.baunex.user.service.UserService
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*

@QuarkusTest
class UserRegistrationTest {

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var userRepository: UserRepository

    @BeforeEach
    @Transactional
    fun setup() {
        userRepository.deleteAll() // Clean DB before each test
    }

    @AfterEach
    @Transactional
    fun cleanup() {
        userRepository.deleteAll() // Ensure clean state after test
    }

    @Test
    fun `should create a new user successfully`() {
        val requestBody = """
            {
              "email": "testuser@example.com",
              "password": "securepassword",
              "role": "USER"
            }
        """.trimIndent()

        given()
            .contentType("application/json")
            .body(requestBody)
            .`when`()
            .post("/api/users")
            .then()
            .statusCode(201)
            .body("email", equalTo("testuser@example.com"))
    }

    @Test
    fun `should not allow duplicate email registration`() {
        val userDTO = UserDTO("duplicate@example.com", "password123", Role.USER)
        userService.registerUser(userDTO)  // First registration

        val requestBody = """
            {
              "email": "duplicate@example.com",
              "password": "anotherpassword",
              "role": "USER"
            }
        """.trimIndent()

        given()
            .contentType("application/json")
            .body(requestBody)
            .`when`()
            .post("/api/users")
            .then()
            .statusCode(409) // Expecting conflict error
    }

    @Test
    fun `should not allow registration with missing email`() {
        val requestBody = """
            {
              "password": "password123",
              "role": "USER"
            }
        """.trimIndent()

        given()
            .contentType("application/json")
            .body(requestBody)
            .`when`()
            .post("/api/users")
            .then()
            .statusCode(400) // Expecting Bad Request
    }

    @Test
    fun `should not allow registration with missing password`() {
        val requestBody = """
            {
              "email": "missingpassword@example.com",
              "role": "USER"
            }
        """.trimIndent()

        given()
            .contentType("application/json")
            .body(requestBody)
            .`when`()
            .post("/api/users")
            .then()
            .statusCode(400) // Expecting Bad Request
    }
}
