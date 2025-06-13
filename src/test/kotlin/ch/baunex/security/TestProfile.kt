import io.quarkus.test.junit.QuarkusTestProfile

class TestProfile : QuarkusTestProfile {
    override fun getConfigOverrides(): Map<String, String> {
        return mapOf(
            "quarkus.http.auth.basic" to "true",
            "quarkus.security.users.embedded.enabled" to "true"
        )
    }
}
class SecurityEnabledTestProfile : QuarkusTestProfile {
    override fun getConfigOverrides(): Map<String, String> {
        return mapOf("quarkus.security.enabled" to "true")
    }
}
