package ch.baunex.notes.model

import kotlinx.serialization.Serializable

@Serializable
enum class NoteCategory {
    INFO,
    TODO,
    FEHLER,
    MATERIALBEDARF,
    NOTFALL,
    SKIZZE,
    VORBEREITUNG
}
