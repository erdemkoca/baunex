import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('project-notes-app');
    const project    = JSON.parse(el.dataset.project      || '{}');
    const categories = JSON.parse(el.dataset.categories   || '[]');
    const employees  = JSON.parse(el.dataset.employees    || '[]');

    createApp({
        data() {
            // a) Projekt-Notizen vorbereiten
            const projNotes = project.notes.map(n => ({
                ...n,
                source: 'project'     // damit wir im Template unterscheiden können
            }));
            // b) TimeEntry-Notizen aus allen Einträgen zusammenziehen
            const teNotes = project.timeEntries
                .flatMap(entry =>
                    (entry.notes || []).map(n => ({
                        ...n,
                        source:    'timeEntry',
                        entryId:   entry.id,
                        entryDate: entry.date,
                        entryTitle: entry.title
                    }))
                );
            return {
                project,
                categories,
                employees,
                // Nutze nur dieses Array zum Rendern
                notes: [...projNotes, ...teNotes],
                newNote: {
                    title: '',
                    category: null,
                    content: '',
                    tags: '',
                    createdById: null,
                    pendingFile: null
                }
            };
        },
        methods: {
            formatDate(d) {
                return new Date(d).toLocaleDateString('de-CH');
            },
            onFilePickedForNew(event) {
                this.newNote.pendingFile = event.target.files[0];
            },
            async removeAttachment(noteIndex, attIndex) {
                const att = this.notes[noteIndex].attachments[attIndex];
                const res = await fetch(
                    `/projects/${this.project.id}/notes/attachment/${att.id}`,
                    { method: 'DELETE' }
                );
                if (res.ok) {
                    this.notes[noteIndex].attachments.splice(attIndex, 1);
                } else {
                    alert('Fehler beim Löschen des Anhangs');
                }
            },
            async saveNote() {
                if (!this.newNote.category) {
                    alert('Bitte eine Kategorie auswählen');
                    return;
                }
                // 1) Notiz anlegen
                const payload = {
                    title:       this.newNote.title,
                    category:    this.newNote.category,
                    content:     this.newNote.content,
                    tags:        this.newNote.tags.split(',').map(t=>t.trim()).filter(Boolean),
                    createdById: this.newNote.createdById
                };
                const res = await fetch(`/projects/${this.project.id}/notes`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                if (!res.ok) {
                    alert('Fehler beim Speichern: ' + await res.text());
                    return;
                }

                // 2) Gesamte Notizliste vom Server holen und in `notes` injizieren
                const updatedNotes = await res.json();
                this.notes = updatedNotes.map(n => ({
                    ...n,
                    tags: n.tags || [],
                    attachments: n.attachments || [],
                    pendingFile: null
                }));

                // 3) Upload des Anhangs für die neueste Notiz (letztes Element)
                const lastNote = this.notes[this.notes.length - 1];
                if (this.newNote.pendingFile && lastNote) {
                    const form = new FormData();
                    form.append('file', this.newNote.pendingFile);
                    const up = await fetch(
                        `/projects/${this.project.id}/notes/${lastNote.id}/attachment`,
                        { method: 'POST', body: form }
                    );
                    if (up.ok) {
                        const attDto = await up.json();
                        lastNote.attachments = [attDto];
                    }
                }

                // 4) Formular zurücksetzen
                Object.assign(this.newNote, {
                    title: '', category: null, content: '', tags: '', createdById: null, pendingFile: null
                });
            }
        },
        template: `
      <div>
        <div class="mb-4">
          <a :href="'/projects/' + project.id" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left me-1"></i>Zurück zur Projektübersicht
          </a>
        </div>

        <!-- Alle Notizen -->
        <div v-for="(note, i) in notes" :key="note.id" class="card mb-4 shadow-sm">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">
              <i class="bi bi-journal-text me-2"></i>{{ note.title || '–' }}
            </h5>
          </div>
          <div class="card-header">
            <span v-if="note.source==='timeEntry'">
              🕒 {{ formatDate(note.entryDate) }} – {{ note.entryTitle }}
            </span>
            <span v-else>📁 Projekt-Notiz</span>
            {{ note.title }}
          </div>
          <div class="card-body">
            <p>{{ note.content }}</p>
            <div v-if="note.tags?.length" class="mb-2">
              <small class="text-muted">Tags:</small>
              <span v-for="tag in note.tags" :key="tag" class="badge bg-info me-1">{{ tag }}</span>
            </div>
            <div v-if="note.attachments?.length" class="mb-2">
              <small class="text-muted">Anhänge:</small>
              <div v-for="(att, ai) in note.attachments || []" :key="att.id" class="d-flex align-items-center mb-1">
                <a :href="att.url" target="_blank">{{ att.caption || 'Datei' }}</a>
                <button type="button" class="btn btn-sm btn-outline-danger ms-2" @click="removeAttachment(i, ai)">
                  <i class="bi bi-x"></i>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Neue Notiz -->
        <div class="card p-3">
          <h5>Neue Notiz hinzufügen</h5>
          <div class="mb-2">
            <label class="form-label">Titel</label>
            <input v-model="newNote.title" class="form-control" />
          </div>
          <div class="mb-2">
            <label class="form-label">Kategorie</label>
            <select v-model="newNote.category" class="form-select" required>
              <option :value="null" disabled>– wählen –</option>
              <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
            </select>
          </div>
          <div class="mb-2">
            <label class="form-label">Inhalt</label>
            <textarea v-model="newNote.content" class="form-control" rows="3" required></textarea>
          </div>
          <div class="mb-2">
            <label class="form-label">Tags (Komma-getrennt)</label>
            <input v-model="newNote.tags" class="form-control" />
          </div>
          <div class="mb-2">
            <label class="form-label">Erstellt von</label>
            <select v-model.number="newNote.createdById" class="form-select" required>
              <option value="">– wählen –</option>
              <option v-for="e in employees" :key="e.id" :value="e.id">
                {{ e.firstName }} {{ e.lastName }}
              </option>
            </select>
          </div>
          <div class="mb-2">
            <label class="form-label">Anhang (optional)</label>
            <input type="file" @change="onFilePickedForNew($event)" class="form-control" />
          </div>
          <button @click="saveNote" class="btn btn-primary">Notiz speichern</button>
        </div>
      </div>
    `
    }).mount(el);
});
