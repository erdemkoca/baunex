import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('project-notes-app');
    const project    = JSON.parse(el.dataset.project      || '{}');
    const categories = JSON.parse(el.dataset.categories   || '[]');
    const employees  = JSON.parse(el.dataset.employees    || '[]');

    createApp({
        data() {
            return {
                project,
                categories,
                employees,
                newNote: {
                    title: '',
                    category: '',
                    content: '',
                    tags: '',
                    createdById: null
                }
            };
        },
        methods: {
            formatDate(d) {
                return new Date(d).toLocaleDateString('de-CH');
            },
            /** Submit new note via JSON-API **/
            async saveNote() {
                const payload = {
                    projectId: this.project.id,
                    title: this.newNote.title,
                    category: this.newNote.category,
                    content: this.newNote.content,
                    tags: this.newNote.tags.split(',').map(t => t.trim()).filter(Boolean),
                    createdById: this.newNote.createdById
                };
                const res = await fetch(`/projects/${this.project.id}/notes/save`, {
                    method: 'POST',
                    headers: {'Content-Type':'application/json'},
                    body: JSON.stringify(payload)
                });
                if (!res.ok) {
                    alert('Fehler beim Speichern: ' + await res.text());
                    return;
                }
                const note = await res.json();
                this.project.notes.push(note);
                Object.assign(this.newNote, { title:'', category:'', content:'', tags:'', createdById:null });
            }
        },
        template: `
      <div>
        <div class="mb-4">
          <a :href="'/projects/' + project.id" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left me-1"></i>Zurück zur Projektübersicht
          </a>
        </div>

        <!-- Projekt-Notizen -->
        <div class="card mb-4 shadow-sm">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0"><i class="bi bi-journal-text me-2"></i>Projekt-Notizen</h5>
          </div>
          <div class="card-body p-0">
            <div v-if="!project.notes.length" class="text-center text-muted py-4">
              Keine Projekt-Notizen vorhanden.
            </div>
            <div v-else class="accordion" id="projectNotesAccordion">
              <div v-for="note in project.notes" :key="note.id" class="accordion-item mb-2 border-0">
                <h2 class="accordion-header" :id="'projNoteHeader' + note.id">
                  <button class="accordion-button collapsed shadow-sm" type="button"
                          data-bs-toggle="collapse"
                          :data-bs-target="'#projNote' + note.id"
                          aria-expanded="false"
                          :aria-controls="'projNote' + note.id">
                    <div class="d-flex justify-content-between w-100 align-items-center">
                      <div class="d-flex align-items-center">
                        <span class="badge bg-primary me-2">{{ note.category }}</span>
                        <div>
                          <small class="text-muted d-block">{{ formatDate(note.createdAt) }}</small>
                          <span class="text-primary">{{ note.createdByName }}</span>
                        </div>
                      </div>
                      <div class="text-end">
                        <h6 v-if="note.title" class="mb-0 text-dark">{{ note.title }}</h6>
                      </div>
                    </div>
                  </button>
                </h2>
                <div :id="'projNote' + note.id"
                     class="accordion-collapse collapse"
                     data-bs-parent="#projectNotesAccordion">
                  <div class="accordion-body bg-light p-3 rounded shadow-sm">
                    <p class="mb-3">{{ note.content }}</p>
                    <div v-if="note.tags.length" class="mb-2">
                      <small class="text-muted">Tags:</small>
                      <span v-for="tag in note.tags" :key="tag" class="badge bg-info me-1">{{ tag }}</span>
                    </div>
                    <div v-if="note.attachments.length">
                      <small class="text-muted">Anhänge:</small>
                      <a v-for="att in note.attachments" 
                         :key="att.id"
                         :href="att.url" 
                         target="_blank"
                         class="btn btn-sm btn-outline-secondary me-1">
                        <i class="bi bi-paperclip me-1"></i>{{ att.caption || 'Datei' }}
                      </a>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Neue Notiz -->
        <div class="card shadow-sm">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0"><i class="bi bi-plus-circle me-2"></i>Neue Notiz hinzufügen</h5>
          </div>
          <div class="card-body">
            <form @submit.prevent="saveNote">
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Titel (optional)</label>
                  <input v-model="newNote.title"
                         type="text"
                         class="form-control"
                         placeholder="Titel der Notiz" />
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">Kategorie</label>
                  <select v-model="newNote.category"
                          class="form-select"
                          required>
                    <option value="">-- wählen --</option>
                    <option v-for="cat in categories" :key="cat" :value="cat">
                      {{ cat }}
                    </option>
                  </select>
                </div>
              </div>
              <div class="mb-3">
                <label class="form-label">Inhalt</label>
                <textarea v-model="newNote.content"
                          class="form-control"
                          rows="3"
                          required></textarea>
              </div>
              <div class="row g-3 mb-3">
                <div class="col-md-6">
                  <label class="form-label">Tags (Komma-getrennt)</label>
                  <input v-model="newNote.tags"
                         type="text"
                         class="form-control"
                         placeholder="z. B. dringlich, Prüfung" />
                </div>
                <div class="col-md-6">
                  <label class="form-label">Erstellt von</label>
                  <select v-model.number="newNote.createdById"
                          class="form-select"
                          required>
                    <option value="">-- wählen --</option>
                    <option v-for="emp in employees" :key="emp.id" :value="emp.id">
                      {{ emp.firstName }} {{ emp.lastName }}
                    </option>
                  </select>
                </div>
              </div>
              <button type="submit" class="btn btn-primary">
                <i class="bi bi-plus-circle me-1"></i>Notiz speichern
              </button>
            </form>
          </div>
        </div>
      </div>
    `
    }).mount(el);
});
