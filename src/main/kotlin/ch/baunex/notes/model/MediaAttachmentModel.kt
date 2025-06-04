package ch.baunex.notes.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*

enum class MediaType {
    IMAGE, PDF, VIDEO
}

@Entity
@Table(name = "media_attachment")
class MediaAttachmentModel : PanacheEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    lateinit var note: NoteModel

    @Column(name = "url", nullable = false, length = 1024)
    var url: String = ""

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: MediaType = MediaType.IMAGE

    @Column(name = "caption")
    var caption: String? = null
}
