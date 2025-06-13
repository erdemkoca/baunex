package ch.baunex.documentGenerator.model

import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "delivery_note_documents")
class DeliveryNoteDocumentModel : DocumentModel() {
    // delivery-note-only fields
}
