//import ch.baunex.security.dto.AuthResponse
//import ch.baunex.security.dto.RefreshTokenDTO
//import ch.baunex.security.utils.JWTUtil
//import ch.baunex.security.utils.PasswordUtil
//import ch.baunex.user.model.Role
//import ch.baunex.user.model.UserModel
//import io.quarkus.test.junit.QuarkusTest
//import io.restassured.RestAssured.given
//import jakarta.inject.Inject
//import jakarta.transaction.Transactional
//import org.hamcrest.Matchers.*
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//
//
//@QuarkusTest
//class AuthControllerTest {
//
//    @Inject
//    lateinit var userRepository: UserRepository
//
//    private lateinit var adminToken: String
//    private lateinit var userToken: String
//    private lateinit var refreshToken: String
//
//    @BeforeEach
//    @Transactional
//    fun setup() {
//        val adminUser = userRepository.findByEmail("admin@example.com")
//        println("🔍 Checking if admin user exists: $adminUser")  // Debugging output
//
//        if (adminUser == null) {
//            println("⚠️ Admin user does not exist! Creating one now...")
//            UserModel().apply {
//                email = "admin@example.com"
//                password = PasswordUtil.hashPassword("adminpassword")  // Ensure password hashing is working
//                role = Role.ADMIN
//            }.also { userRepository.persist(it) }
//        }
//
//        val user = userRepository.findByEmail("user@example.com")
//        if (user == null) {
//            println("⚠️ Normal user does not exist! Creating one now...")
//            UserModel().apply {
//                email = "user@example.com"
//                password = PasswordUtil.hashPassword("userpassword")
//                role = Role.USER
//            }.also { userRepository.persist(it) }
//        }
//    }
//
//
//    @Test
//    fun shouldAuthenticateUserAndReturnJwtToken() {
//        val response = given()
//            .contentType("application/json")
//            .body("""
//                {
//                    "email": "admin@example.com",
//                    "password": "adminpassword"
//                }
//            """.trimIndent())
//            .post("/api/auth/login")
//            .then()
//            .statusCode(200)
//            .body("accessToken", notNullValue())
//            .body("refreshToken", notNullValue())
//            .extract()
//            .`as`(AuthResponse::class.java)
//
//        refreshToken = response.refreshToken
//    }
//
////    @Test
////    fun shouldAllowAuthenticatedUserToAccessProtectedEndpoint() {
////        println("Using Admin Token: $adminToken")
////        given()
////            .header("Authorization", "Bearer $adminToken")
////            .get("/api/admin/users/all")
////            .then()
////            .statusCode(200)
////    }
//
////    @Test
////    fun shouldDenyAccessToAdminEndpointForNonAdminUser() {
////        given()
////            .header("Authorization", "Bearer $userToken")
////            .get("/api/admin/users/all")
////            .then()
////            .statusCode(403)
////    }
//
////    @Test
////    fun shouldRefreshTokenAndReturnNewAccessToken() {
////        val loginResponse = given()
////            .contentType("application/json")
////            .body("""
////                {
////                    "email": "admin@example.com",
////                    "password": "adminpassword"
////                }
////            """.trimIndent())
////            .post("/api/auth/login")
////            .then()
////            .statusCode(200)
////            .extract()
////            .`as`(AuthResponse::class.java)
////
////        val refreshTokenDTO = RefreshTokenDTO(loginResponse.refreshToken)
////
////        given()
////            .contentType("application/json")
////            .body(refreshTokenDTO)
////            .post("/api/auth/refresh-token")
////            .then()
////            .statusCode(200)
////            .body("accessToken", notNullValue())
////    }
//
//    @Test
//    fun shouldRejectRequestsWithExpiredToken() {
//        val expiredToken = JWTUtil.generateToken("admin@example.com", 1L, "ADMIN", expirationMillis = -1000) // ✅ Expired token
//
//        given()
//            .header("Authorization", "Bearer $expiredToken")
//            .get("/api/admin/users/all")
//            .then()
//            .statusCode(401)
//    }
//
//
//    @Test
//    fun shouldRejectInvalidRefreshToken() {
//        val invalidRefreshTokenDTO = RefreshTokenDTO("invalid-token")
//
//        given()
//            .contentType("application/json")
//            .body(invalidRefreshTokenDTO)
//            .post("/api/auth/refresh-token")
//            .then()
//            .statusCode(401)
//    }
//
////    @Test
////    fun shouldLogoutUserSuccessfully() {
////        given()
////            .post("/api/auth/logout")
////            .then()
////            .statusCode(200)
////            .body("message", equalTo("Logged out successfully"))
////    }
//}
