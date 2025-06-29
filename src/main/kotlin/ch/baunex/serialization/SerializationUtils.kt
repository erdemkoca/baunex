package ch.baunex.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object SerializationUtils {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    object LocalDateSerializer : KSerializer<LocalDate> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("java.time.LocalDate", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: LocalDate) =
            encoder.encodeString(value.format(dateFormatter))

        override fun deserialize(decoder: Decoder): LocalDate =
            LocalDate.parse(decoder.decodeString(), dateFormatter)
    }

    object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("java.time.LocalDateTime", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: LocalDateTime) =
            encoder.encodeString(value.format(dateTimeFormatter))

        override fun deserialize(decoder: Decoder): LocalDateTime =
            LocalDateTime.parse(decoder.decodeString(), dateTimeFormatter)
    }

    object LocalTimeSerializer : KSerializer<LocalTime> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("java.time.LocalTime", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: LocalTime) =
            encoder.encodeString(value.format(timeFormatter))

        override fun deserialize(decoder: Decoder): LocalTime =
            LocalTime.parse(decoder.decodeString(), timeFormatter)
    }

    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        serializersModule = SerializersModule {
            contextual(LocalDateSerializer)
            contextual(LocalDateTimeSerializer)
            contextual(LocalTimeSerializer)
        }
    }
}
