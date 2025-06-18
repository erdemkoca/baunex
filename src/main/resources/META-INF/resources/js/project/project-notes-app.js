import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.prod.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('project-notes-app');
    if (!el) {
        console.error('Element with id "project-notes-app" not found');
        return;
    }

    let view;
    try {
        view = JSON.parse(el.dataset.project || '{}');
        console.log('Parsed view data:', view);
    } catch (error) {
        console.error('Error parsing project data:', error);
        view = {};
    }

    createApp({
        data() {
            return {
                projectId:   view.projectId || null,
                projectName: view.projectName || '',
                categories:  view.categories || [],
                employees:   view.employees || [],
                notes:       (view.notes || []).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)),
                newNote: {
                    title:        '',
                    category:     null,
                    content:      '',
                    tags:         '',
                    createdById:  null,
                    pendingFile:  null,
                    previewUrl:   null
                }
            };
        },
        methods: {
            formatDate(d) {
                return new Date(d).toLocaleDateString('de-CH');
            },
            onFilePickedForNew(event) {
                const file = event.target.files[0];
                if (!file) return;
                this.newNote.pendingFile = file;
                this.newNote.previewUrl   = URL.createObjectURL(file);
            },
            async removeAttachment(noteIndex, attIndex) {
                const note = this.notes[noteIndex];
                const att  = note.attachments[attIndex];
                const res  = await fetch(
                    `/projects/${this.projectId}/notes/${note.id}/attachments/${att.id}`,
                    { method: 'DELETE' }
                );
                if (res.ok) {
                    note.attachments.splice(attIndex, 1);
                } else {
                    alert('Fehler beim Löschen des Anhangs');
                }
            },
            isImageAttachment(att) {
                if (!att) return false;
                if (att.type === 'IMAGE') return true;
                if (att.contentType && att.contentType.startsWith('image/')) return true;
                return false;
            },
            async saveNote() {
                if (!this.newNote.category) {
                    return alert('Bitte eine Kategorie auswählen');
                }
                if (!this.newNote.createdById) {
                    return alert('Bitte einen Ersteller auswählen');
                }

                // 1) Create the note, get back a NoteDto with its id
                const payload = {
                    projectId:   this.projectId,
                    title:       this.newNote.title,
                    category:    this.newNote.category,
                    content:     this.newNote.content,
                    tags:        this.newNote.tags.split(',').map(t=>t.trim()).filter(Boolean),
                    createdById: this.newNote.createdById
                };
                console.log('Sending payload:', payload); // Debug log

                const createRes = await fetch(
                    `/projects/${this.projectId}/notes`, {
                        method:  'POST',
                        headers: { 'Content-Type':'application/json' },
                        body:    JSON.stringify(payload)
                    }
                );
                if (!createRes.ok) {
                    const txt = await createRes.text();
                    console.error('Create note failed:', txt);
                    return alert('Fehler beim Speichern der Notiz: ' + txt);
                }
                const newNoteDto = await createRes.json();
                console.log('Created note:', newNoteDto); // Debug log

                // 2) Push it immediately into your list at the beginning
                this.notes.unshift({
                    ...newNoteDto,
                    tags:        newNoteDto.tags || [],
                    attachments: []
                });

                // 3) If there's a file, upload it *to that exact ID*
                if (this.newNote.pendingFile) {
                    const form = new FormData();
                    form.append('file', this.newNote.pendingFile);

                    const upRes = await fetch(
                        `/projects/${this.projectId}/notes/${newNoteDto.id}/attachments`,
                        { method: 'POST', body: form }
                    );
                    if (upRes.ok) {
                        const attDto = await upRes.json();
                        console.log('Uploaded attachment:', attDto); // Debug log
                        // find the note we just added and append its attachment
                        const idx = this.notes.findIndex(n => n.id === newNoteDto.id);
                        if (idx !== -1) {
                            this.notes[idx].attachments.push(attDto);
                        }
                    } else {
                        console.error('Attachment upload failed:', await upRes.text());
                    }
                }

                // 4) reset the form
                Object.assign(this.newNote, {
                    title:        '',
                    category:     null,
                    content:      '',
                    tags:         '',
                    createdById:  null,
                    pendingFile:  null,
                    previewUrl:   null
                });
            }
        },
        template: `
      <div>
        <div class="mb-4">
          <a :href="'/projects/' + projectId" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left me-1"></i>Zurück zur Projektübersicht
          </a>
        </div>

        <!-- Notizen-Liste -->
        <div v-for="(note, i) in notes" :key="note.id" class="card mb-4 shadow-sm">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0"><i class="bi bi-journal-text me-2"></i>{{ note.title || '–' }}</h5>
          </div>
          <div class="card-header">
            <span v-if="note.source==='timeEntry'">
              🕒 {{ formatDate(note.entryDate) }} – {{ note.entryTitle }}
            </span>
            <span v-else>📁 Projekt-Notiz</span>
          </div>
          <div class="card-body">
            <p>{{ note.content }}</p>
            <div v-if="note.tags && note.tags.length" class="mb-2">
              <small class="text-muted">Tags:</small>
              <span v-for="tag in note.tags" :key="tag" class="badge bg-info me-1">{{ tag }}</span>
            </div>
            <div v-if="note.attachments && note.attachments.length" class="mb-2">
              <small class="text-muted">Anhänge:</small>
              <div v-for="(att, ai) in note.attachments" :key="att.id"
                   class="d-flex align-items-center mb-1">
                <template v-if="isImageAttachment(att)">
                  <img :src="att.url" class="img-fluid img-thumbnail" style="max-width:200px;" />
                </template>
                <template v-else>
                  <a :href="att.url" target="_blank">{{ att.caption || 'Datei' }}</a>
                </template>
                <button @click="removeAttachment(i, ai)"
                        class="btn btn-sm btn-outline-danger ms-2">✕</button>
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
            <input type="file" @change="onFilePickedForNew" class="form-control" />
          </div>
          <div v-if="newNote.previewUrl" class="mb-3">
            <label class="form-label">Vorschau</label>
            <img :src="newNote.previewUrl" class="img-fluid img-thumbnail" style="max-width:200px;" />
          </div>
          <button @click="saveNote" class="btn btn-primary">Notiz speichern</button>
        </div>
      </div>
    `
    }).mount(el);
});
