package ch.baunex.user

import SecurityEnabledTestProfile
import ch.baunex.user.dto.LoginDTO
import ch.baunex.user.dto.UpdateUserDTO
import ch.baunex.user.dto.UserDTO
import ch.baunex.user.model.Role
import ch.baunex.user.test.TestConfig
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
        private lateinit var userToken: String
        private lateinit var anotherUserToken: String
    }
    @Inject
    lateinit var userFacade: UserFacade

    @BeforeEach
    @Transactional
    fun resetDatabase() {
        println("🧹 Resetting database before test execution...")

        // Step 1: Delete all users not superadmin
        userFacade.deleteAllUsersExceptSuperadmin()

        // Step 2: Check if Superadmin exists
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
        resetDatabase() // Ensure clean state

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
            userToken = loginResponse.jsonPath().getString("accessToken")
                ?: throw RuntimeException("🚨 Login response did not contain a valid access token!")

            println("✅ Superadmin login successful. Token: $userToken")
        } else {
            throw RuntimeException("🚨 Failed to log in as Superadmin. Response: ${loginResponse.body.asString()}")
        }

        setupAnotherUser() // Ensure existinguser@example.com is present **after reset**

        // 🔥 **Modify superadmin to verify DB access**
        val updateSuperadmin = UpdateUserDTO(street = "Test Street 123456")
        println("🔄 Updating Superadmin street to: ${updateSuperadmin.street}")

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $userToken")
            .body(updateSuperadmin)
            .put("$BASE_URL/me")
            .then()
            .statusCode(Response.Status.OK.statusCode)

        // 🔥 **Verify Superadmin's street before test**
        val superadminAfterUpdate = userFacade.getUserByMail(superAdminEmail)
        println("✅ Superadmin's street in DB before test: ${superadminAfterUpdate?.street}")
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



    private fun setupAnotherUser() {
        val anotherUserDTO = UserDTO(
            email = "existinguser@example.com",
            password = "password123",
            role = Role.USER
        )

        println("🛠 Ensuring test DB contains ${anotherUserDTO.email}")

        // Check if user already exists
        val existingUser = userFacade.getUserByMail(anotherUserDTO.email)

        if (existingUser == null) {
            println("✅ Creating test user: ${anotherUserDTO.email}")
            userFacade.registerUser(anotherUserDTO)
        } else {
            println("⚠️ Test user already exists, skipping creation.")
        }

        // **🔍 Fetch & Print ALL Users to Verify DB State**
        val allUsers = userFacade.getAllUsers()
        println("📋 Users in DB after inserting ${anotherUserDTO.email}:")
        allUsers.forEach { println("   - ${it.email} (Role: ${it.role}, Street: ${it.street})") }
    }









    @Test
    @Order(1)
    fun `should update user profile successfully`() {
        val updateDTO = UpdateUserDTO(phone = "123456789", street = "Updated Street 1")

        println("🔄 Sending update request: $updateDTO")

        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $userToken")
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
            .header("Authorization", "Bearer $userToken") // superadmin
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
    fun `should prevent unauthorized user from updating another user's profile`() {
        val anotherUserEmail = "anotheruser@example.com"
        val anotherUserPassword = "password123"

        val anotherUserToken = setupTestUser(anotherUserEmail, anotherUserPassword)

        val updateDTO = UpdateUserDTO(phone = "987654321")

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $anotherUserToken")
            .body(updateDTO)
            .put("$BASE_URL/me")
            .then()
            .statusCode(Response.Status.FORBIDDEN.statusCode) // ✅ Should now return 403
    }





    @Test
    @Order(4)
    fun `should reject update with invalid data`() {
        val updateDTO = UpdateUserDTO(email = "")

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $userToken")
            .body(updateDTO)
            .put("$BASE_URL/me")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @Order(5)
    fun `should allow partial profile updates`() {
        val updateDTO = UpdateUserDTO(street = "Updated Street 2")

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $userToken")
            .body(updateDTO)
            .put("$BASE_URL/me")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .body("street", Matchers.equalTo(updateDTO.street))
    }
}
