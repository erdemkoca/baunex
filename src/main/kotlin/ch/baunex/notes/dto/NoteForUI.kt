package ch.baunex.notes.dto

import ch.baunex.notes.model.NoteCategory
import ch.baunex.serialization.LocalDateSerializer
import java.time.LocalDate

@kotlinx.serialization.Serializable
data class NoteForUI(
    val id: Long,
    val title: String?,
    val content: String,
    val category: NoteCategory,
    val tags: List<String>,
    val createdById: Long?,

    @kotlinx.serialization.Serializable(with = LocalDateSerializer::class)
    val createdAt: LocalDate?,
    val attachments: List<AttachmentForUI>,
    val source: String,
    val entryId: Long? = null,

    @kotlinx.serialization.Serializable(with = LocalDateSerializer::class)
    val entryDate: LocalDate? = null,
    val entryTitle: String? = null
)
