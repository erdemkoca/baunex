package ch.baunex.user

import ch.baunex.user.dto.UpdateUserDTO
import ch.baunex.user.dto.UserDTO
import ch.baunex.user.model.Role
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.ws.rs.core.Response
import org.hamcrest.Matchers
import org.junit.jupiter.api.*

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserProfileUpdateTest {

    companion object {
        private const val BASE_URL = "/api/users"
        private lateinit var userToken: String
        private lateinit var anotherUserToken: String
        private var userId: Long = 0
        private var anotherUserId: Long = 0
    }

    @BeforeEach
    fun setup() {
        val userEmail = "testuser@example.com"

        println("Attempting to log in user: $userEmail")

        val loginResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("email" to userEmail, "password" to "password123"))
            .post("$BASE_URL/login")
            .then()
            .extract()
            .response()

        println("Login response status: ${loginResponse.statusCode}")
        println("Login response body: ${loginResponse.body.asString()}")

        if (loginResponse.statusCode == Response.Status.OK.statusCode) {
            println("User already exists, retrieving ID and token...")
            userToken = loginResponse.asString() // ✅ Fix: Extract JWT correctly
        } else {
            println("User not found, creating a new one...")
            val userDTO = UserDTO(userEmail, "password123", Role.USER)

            val createResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(userDTO)
                .post(BASE_URL)
                .then()
                .extract()
                .response()

            println("Create user response status: ${createResponse.statusCode}")
            println("Create user response body: ${createResponse.body.asString()}")

            if (createResponse.statusCode == Response.Status.CREATED.statusCode) {
                userId = createResponse.jsonPath().getLong("id")
                println("User created with ID: $userId")

                val newLoginResponse = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(mapOf("email" to userEmail, "password" to "password123"))
                    .post("$BASE_URL/login")
                    .then()
                    .statusCode(Response.Status.OK.statusCode)
                    .extract().response()

                println("New login response body: ${newLoginResponse.body.asString()}")

                userToken = newLoginResponse.asString() // ✅ Fix: Extract JWT correctly

                println("User logged in, token: $userToken")
            } else {
                throw RuntimeException("Failed to create user, response: ${createResponse.body.asString()}")
            }
        }

        if (userToken.isEmpty()) {
            throw RuntimeException("Failed to set up test user, userToken is empty")
        }
    }

    @Test
    fun testUserProfileUpdate() {
        println("User ID: $userId") // ✅ Debugging test
    }

    @Test
    @Order(1)
    fun `should register and login a user for testing updates`() {
        val userDTO = UserDTO("updateuser@example.com", "password123", Role.USER)

        // Register user
        userId = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(userDTO)
            .post(BASE_URL)
            .then()
            .statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath().getLong("id")

        // Login user to get JWT token
        userToken = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("email" to userDTO.email, "password" to userDTO.password))
            .post("$BASE_URL/login")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString() // ✅ Fix: Extract JWT correctly
    }

    @Test
    @Order(2)
    fun `should update user profile successfully`() {
        val updateDTO = UpdateUserDTO(phone = "123456789", street = "New Street 1")

        println("Sending update request with: $updateDTO") // Debugging output

        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $userToken")
            .body(updateDTO)
            .put("$BASE_URL/$userId")
            .then()
            .extract().response()

        println("Update Response Status: ${response.statusCode}")
        println("Update Response Body: ${response.body.asString()}")

        response.then()
            .statusCode(Response.Status.OK.statusCode)
            .body("phone", Matchers.equalTo(updateDTO.phone))
            .body("street", Matchers.equalTo(updateDTO.street))
    }


    @Test
    @Order(3)
    fun `should prevent updating email to an existing one`() {
        val anotherUserDTO = UserDTO("existinguser@example.com", "password123", Role.USER)

        anotherUserId = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(anotherUserDTO)
            .post(BASE_URL)
            .then()
            .statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath().getLong("id")

        anotherUserToken = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("email" to anotherUserDTO.email, "password" to anotherUserDTO.password))
            .post("$BASE_URL/login")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString() // ✅ Fix: Extract JWT correctly

        val updateDTO = UpdateUserDTO(email = anotherUserDTO.email)

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $userToken")
            .body(updateDTO)
            .put("$BASE_URL/$userId")
            .then()
            .statusCode(Response.Status.CONFLICT.statusCode)
    }

    @Test
    @Order(4)
    fun `should prevent unauthorized user from updating another user's profile`() {
        val updateDTO = UpdateUserDTO(phone = "987654321")

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $anotherUserToken")
            .body(updateDTO)
            .put("$BASE_URL/$userId")
            .then()
            .statusCode(Response.Status.FORBIDDEN.statusCode)
    }

    @Test
    @Order(5)
    fun `should reject update with invalid data`() {
        val updateDTO = UpdateUserDTO(email = "")

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $userToken")
            .body(updateDTO)
            .put("$BASE_URL/$userId")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @Order(6)
    fun `should allow partial profile updates`() {
        val updateDTO = UpdateUserDTO(street = "Updated Street 2")

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $userToken")
            .body(updateDTO)
            .put("$BASE_URL/$userId")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .body("street", Matchers.equalTo(updateDTO.street))
    }
}
