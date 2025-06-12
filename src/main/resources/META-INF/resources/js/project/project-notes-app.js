import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el   = document.getElementById('project-notes-app');
    const view = JSON.parse(el.dataset.notes || '{}');
    console.log('dataset.notes:', view.notes); // nur zum Testen
    console.log('dataset.id:', view.projectId); // nur zum Testen
    console.log('dataset.employees:', view.employees); // nur zum Testen
    console.log('dataset.categoreis:', view.categories); // nur zum Testen
    console.log('dataset.proejctname:', view.projectName); // nur zum Testen


    createApp({
        data() {
            // view ist nun dein Projekt-Notizen-DTO mit
            // projectId, projectName, categories, employees, notes
            return {
                projectId:   view.projectId,
                projectName: view.projectName,
                categories:  view.categories,
                employees:   view.employees,
                notes:       view.notes,
                newNote: {
                    title:       '',
                    category:    null,
                    content:     '',
                    tags:        '',
                    createdById: null,
                    pendingFile: null,
                    previewUrl:  null
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
                // URL für Vorschau erzeugen
                this.newNote.previewUrl = URL.createObjectURL(file);
            },
            async removeAttachment(noteIndex, attIndex) {
                const att = this.notes[noteIndex].attachments[attIndex];
                const res = await fetch(
                    `/projects/${this.projectId}/notes/attachment/${att.id}`,
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
                const payload = {
                    title:       this.newNote.title,
                    category:    this.newNote.category,
                    content:     this.newNote.content,
                    tags:        this.newNote.tags.split(',').map(t=>t.trim()).filter(Boolean),
                    createdById: this.newNote.createdById
                };
                const res = await fetch(`/projects/${this.projectId}/notes`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                if (!res.ok) {
                    alert('Fehler beim Speichern: ' + await res.text());
                    return;
                }
                const updatedNotes = await res.json();
                this.notes = updatedNotes.map(n => ({
                    ...n,
                    tags:        n.tags || [],
                    attachments: n.attachments || [],
                    pendingFile: null,
                    previewUrl:  null
                }));
                const lastNote = this.notes[this.notes.length - 1];
                if (this.newNote.pendingFile && lastNote) {
                    const form = new FormData();
                    form.append('file', this.newNote.pendingFile);
                    const up = await fetch(
                        `/projects/${this.projectId}/notes/${lastNote.id}/attachment`,
                        { method: 'POST', body: form }
                    );
                    if (up.ok) {
                        const attDto = await up.json();
                        lastNote.attachments = [attDto];
                    }
                }
                // Formular & Preview zurücksetzen
                Object.assign(this.newNote, {
                    title:       '',
                    category:    null,
                    content:     '',
                    tags:        '',
                    createdById: null,
                    pendingFile: null,
                    previewUrl:  null
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
                <div v-for="(att, ai) in note.attachments" :key="att.id" class="d-flex align-items-center mb-1">
                  <!-- bevorzugt: Content-Type, den der Server in att.contentType mitliefert -->
                  <template v-if="att.contentType && att.contentType.startsWith('image/')">
                    <img
                      :src="att.url"
                      class="img-fluid img-thumbnail"
                      style="max-width: 200px;"
                      alt="Anhangsbild"
                    />
                  </template>
                  <template v-else-if="/\\.(jpe?g|png|gif)$/i.test(att.filename)">
                    <img
                      :src="att.url"
                      class="img-fluid img-thumbnail"
                      style="max-width: 200px;"
                      alt="Anhangsbild"
                    />
                  </template>
                  <template v-else>
                    <a :href="att.url" target="_blank">{{ att.caption || 'Datei' }}</a>
                  </template>
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
          <!-- Bild-Vorschau für neue Notiz -->
            <div v-if="newNote.previewUrl" class="mb-3">
               <label class="form-label">Vorschau</label>
               <img
                     :src="newNote.previewUrl"
                     class="img-fluid img-thumbnail"
                     style="max-width: 200px;"
                     alt="Vorschau des Anhangs"
                   />
             </div>
          <button @click="saveNote" class="btn btn-primary">Notiz speichern</button>
        </div>
      </div>
    `
    }).mount(el);
});
