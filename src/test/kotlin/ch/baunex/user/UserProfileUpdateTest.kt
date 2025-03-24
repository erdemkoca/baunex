package ch.baunex.user

import SecurityEnabledTestProfile
import ch.baunex.user.dto.LoginDTO
import ch.baunex.user.dto.UpdateUserDTO
import ch.baunex.user.dto.UserDTO
import ch.baunex.user.model.Role
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import ch.baunex.user.facade.UserFacade
import jakarta.inject.Inject

@QuarkusTest
//@TestProfile(TestConfig::class)
@TestProfile(SecurityEnabledTestProfile::class) // enable security for this test class
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserProfileUpdateTest {

    companion object {
        private const val BASE_URL = "/api/users"
        private lateinit var superAdminToken: String
    }
    @Inject
    lateinit var userFacade: UserFacade

    @Transactional
    fun resetDatabase() {
        //userFacade.deleteAllUsersExceptSuperadmin()

        val existingSuperadmin = userFacade.getUserByMail("superadmin@example.com")
        if (existingSuperadmin == null) {
            println("🔍 Superadmin does not exist. Creating...")

            val superadminDTO = UserDTO(
                email = "superadmin@example.com",
                password = "SuperSecurePassword",
                role = Role.ADMIN,
                phone = "123456789",
                street = "Admin Street"
            )

            userFacade.registerUser(superadminDTO)
            println("✅ Superadmin created successfully")
        } else {
            println("✅ Superadmin already exists")
        }
    }


    @BeforeEach
    fun setup() {
        resetDatabase()

        val superAdminEmail = "superadmin@example.com"
        val superAdminPassword = "superadminpassword"

        println("🟢 Attempting to log in as Superadmin: $superAdminEmail")

        val loginResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("email" to superAdminEmail, "password" to superAdminPassword))
            .post("/api/auth/login")
            .then()
            .extract()
            .response()

        if (loginResponse.statusCode == Response.Status.OK.statusCode) {
            superAdminToken = loginResponse.jsonPath().getString("accessToken")
                ?: throw RuntimeException("🚨 Login response did not contain a valid access token!")

            println("✅ Superadmin login successful. Token: $superAdminToken")
        } else {
            throw RuntimeException("🚨 Failed to log in as Superadmin. Response: ${loginResponse.body.asString()}")
        }
    }

    fun loginAndGetToken(email: String, password: String): String {
        val loginDTO = LoginDTO(email, password)

        return RestAssured.given()
            .contentType(ContentType.JSON)
            .body(loginDTO)
            .post("/api/auth/login")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract()
            .path("accessToken") // Assuming the API returns the token as "token"
    }


    fun setupTestUser(email: String, password: String): String {
        val userDTO = UserDTO(
            email = email,
            password = password,
            role = Role.USER
        )

        val existing = userFacade.getUserByMail(email)
        if (existing == null) {
            println("✅ Creating user: $email")
            userFacade.registerUser(userDTO)
        } else {
            println("⚠️ User already exists: $email")
        }

        return loginAndGetToken(email, password)
    }

    @Test
    @Order(1)
    fun `should update user profile successfully`() {
        val updateDTO = UpdateUserDTO(email= "superadmin@example.com", phone = "123456789", street = "Updated Street 1")

        println("🔄 Sending update request: $updateDTO")

        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $superAdminToken")
            .body(updateDTO)
            .put("$BASE_URL/me")
            .then()
            .extract().response()

        println("✅ Update Response Status: ${response.statusCode}")
        println("✅ Update Response Body: ${response.body.asString()}")

        response.then()
            .statusCode(Response.Status.OK.statusCode)
            .body("phone", Matchers.equalTo(updateDTO.phone))
            .body("street", Matchers.equalTo(updateDTO.street))
    }

    @Test
    @Order(2)
    fun `should prevent updating email to an existing one`() {
        // 🔹 Create a test user dynamically
        setupTestUser("existinguser2@example.com", "password123")

        // ✅ Print users before update to ensure the user exists
        val allUsers = userFacade.getAllUsers()
        println("📋 Current users in test DB before email update:")
        allUsers.forEach { println("   - ${it.email} (Role: ${it.role}, Street: ${it.street})") }

        // 🔍 Attempting to update superadmin's email to an existing one
        val updateDTO = UpdateUserDTO(email = "existinguser2@example.com")
        println("🔍 Attempting to update superadmin's email to: ${updateDTO.email}")

        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $superAdminToken") // superadmin
            .body(updateDTO)
            .put("$BASE_URL/me")
            .then()
            .extract().response()

        println("🔄 Update Response Status: ${response.statusCode}")
        println("🔄 Update Response Body: ${response.body.asString()}")

        // ✅ Expected behavior: superadmin should not be able to use a taken email
        response.then()
            .statusCode(Response.Status.CONFLICT.statusCode)
    }

    @Test
    @Order(3)
    fun `should allow partial profile updates`() {
        val updateDTO = UpdateUserDTO(street = "Updated Street 2")

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $superAdminToken")
            .body(updateDTO)
            .put("$BASE_URL/me")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .body("street", Matchers.equalTo(updateDTO.street))
    }
}
