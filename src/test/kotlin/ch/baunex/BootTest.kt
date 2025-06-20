package ch.baunex

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test

@QuarkusTest
class BootTest {

    @Test
    fun testStartup() {
        // einfach nur prüfen, ob Quarkus ohne Fehler startet
        println("Quarkus Test gestartet!")
    }
}
