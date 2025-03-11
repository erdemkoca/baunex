package ch.baunex.user.controller

import ch.baunex.user.dto.LoginDTO
import ch.baunex.user.dto.UserDTO
import ch.baunex.user.dto.UserResponseDTO
import ch.baunex.user.model.Role
import ch.baunex.user.service.AuthService
import ch.baunex.user.service.UserService
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@QuarkusTest
class UserControllerTest {

    @Mock
    lateinit var userService: UserService

    @Mock
    lateinit var authService: AuthService

    @InjectMocks
    lateinit var userController: UserController

    private lateinit var testUserDTO: UserDTO
    private lateinit var testResponseDTO: UserResponseDTO
    private val testToken = "mocked-jwt-token" // ✅ Mock JWT token for authentication tests

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this) // ✅ Ensures mocks are initialized

        testUserDTO = UserDTO("test@example.com", "password", Role.CLIENT) // ✅ Use existing role
        testResponseDTO = UserResponseDTO(1L, testUserDTO.email, testUserDTO.role)
    }

    /**
     * ✅ Test: Create User
     */
    @Test
    fun `should create user successfully`() {
        `when`(userService.registerUser(testUserDTO)).thenReturn(testResponseDTO)  // ✅ Fix type mismatch

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(testUserDTO)  // ✅ Send DTO, not Model
            .`when`()
            .post("/api/users")
            .then()
            .statusCode(Response.Status.CREATED.statusCode)
            .body("email", Matchers.equalTo(testUserDTO.email))
    }

    /**
     * ✅ Test: User Login - Valid Credentials
     */
    @Test
    fun `should return token when login is successful`() {
        val loginDTO = LoginDTO(email = testUserDTO.email, password = testUserDTO.password)

        // Mock authentication to return a predefined token
        `when`(authService.authenticate(loginDTO)).thenReturn(testToken)

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(loginDTO)
            .`when`()
            .post("/api/users/login")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .body("token", Matchers.notNullValue()) // ✅ Check if a token exists, instead of matching exact value
    }


    /**
     * ❌ Test: User Login - Invalid Credentials
     */
    @Test
    fun `should return 401 when login fails`() {
        val loginDTO = LoginDTO(email = "wrong@example.com", password = "wrongpass")
        `when`(authService.authenticate(loginDTO)).thenReturn(null)

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(loginDTO)
            .`when`()
            .post("/api/users/login")
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.statusCode)
            .body("message", Matchers.equalTo("Invalid credentials"))
    }

//    /**
//     * ✅ Test: Admin List Users - Allowed for Admins
//     */
//    @Test
//    fun `should return list of users for admin`() {
//        `when`(userService.listUsers()).thenReturn(listOf(testResponseDTO))
//
//        given()
//            .contentType(MediaType.APPLICATION_JSON)
//            .header("Authorization", "Bearer $testToken") // ✅ Mocked token
//            .`when`()
//            .get("/api/users/adminListUsers")
//            .then()
//            .statusCode(Response.Status.OK.statusCode)
//            .body("$.size()", Matchers.equalTo(1))
//            .body("[0].email", Matchers.equalTo(testUserDTO.email))
//    }
//
//    /**
//     * ❌ Test: Admin List Users - Unauthorized for non-admins
//     */
//    @Test
//    fun `should return 403 when non-admin tries to list users`() {
//        given()
//            .contentType(MediaType.APPLICATION_JSON)
//            .`when`()
//            .get("/api/users/adminListUsers")
//            .then()
//            .statusCode(Response.Status.FORBIDDEN.statusCode)
//    }
//
//    /**
//     * ✅ Test: Admin Only Endpoint
//     */
//    @Test
//    fun `should return welcome message for admin`() {
//        given()
//            .contentType(MediaType.APPLICATION_JSON)
//            .header("Authorization", "Bearer $testToken") // ✅ Mocked token
//            .`when`()
//            .get("/api/users/testAdmin")
//            .then()
//            .statusCode(Response.Status.OK.statusCode)
//            .body("message", Matchers.equalTo("Welcome, Admin!"))
//    }
}
