//package ch.baunex.security
//
//import ch.baunex.user.dto.LoginDTO
//import ch.baunex.user.dto.UserDTO
//import ch.baunex.user.model.Role
//import io.quarkus.test.junit.QuarkusTest
//import io.restassured.RestAssured
//import io.restassured.RestAssured.given
//import io.restassured.http.ContentType
//import jakarta.ws.rs.core.Response
//import org.hamcrest.Matchers
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//
//@QuarkusTest
//class SecurityTest {
//
//    @BeforeEach
//    fun setupAdminUser() {
//        val adminLoginDTO = LoginDTO(
//            email = "superadmin@example.com",
//            password = "superadminpassword"
//        )
//
//
//        val response = given()
//            .contentType(ContentType.JSON)
//            .body(adminLoginDTO)
//            .post("/api/users/login")
//            .then()
//            .statusCode(200)
//            .extract()
//            .response()
//
//
//
//        println("🔑 Login Response Status: ${response.statusCode}")
//        println("🔑 Login Response Body: ${response.body.asString()}")
//
//        if (response.statusCode != 200) {
//            throw RuntimeException("Failed to obtain token: ${response.body.asString()}")
//        }
//    }
//
//
//
//
//    @Test
//    fun `should reject requests without a token`() {
//        RestAssured.given()
//            .contentType(ContentType.JSON)
//            .get("/api/users/allUsers")  // Protected endpoint
//            .then()
//            .statusCode(Response.Status.UNAUTHORIZED.statusCode)
//            .body(Matchers.containsString("Missing or invalid Authorization header"))
//    }
//
//    @Test
//    fun `should reject requests with an invalid token`() {
//        RestAssured.given()
//            .contentType(ContentType.JSON)
//            .header("Authorization", "Bearer invalid_token")
//            .get("/api/users/allUsers")
//            .then()
//            .statusCode(Response.Status.UNAUTHORIZED.statusCode)
//            .body(Matchers.containsString("Invalid or expired token"))
//    }
//
//    @Test
//    fun shouldAllowAccessWithValidAdminToken() {
//        val token = obtainToken("admin@example.com", "password123")
//
//        RestAssured.given()
//            .header("Authorization", "Bearer $token")
//            .get("/api/admin/protected-endpoint")
//            .then()
//            .statusCode(200)
//    }
//
//
//    private fun obtainToken(email: String, password: String): String {
//        val response = RestAssured.given()
//            .contentType(ContentType.JSON)
//            .body(mapOf("email" to email, "password" to password))
//            .post("/api/users/login")
//            .then()
//            .extract().response()
//
//        println("🔑 Login Response Status: ${response.statusCode}")
//        println("🔑 Login Response Body: ${response.body.asString()}")
//
//        if (response.statusCode != 200) {
//            throw RuntimeException("Failed to obtain token for $email: ${response.body.asString()}")
//        }
//
//        return response.jsonPath().getString("token") ?: throw RuntimeException("Token retrieval failed")
//    }
//
//
//}
