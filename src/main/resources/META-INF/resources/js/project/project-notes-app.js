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
    } catch {
        view = {};
    }

    createApp({
        data() {
            return {
                projectId:     view.projectId || null,
                categories:    view.categories || [],
                employees:     view.employees || [],
                notes:         (view.notes || [])
                    .sort((a,b)=>new Date(b.createdAt)-new Date(a.createdAt)),
                newNote:       { title:'', category:null, content:'', tags:'', createdById:null, pendingFile:null, previewUrl:null },
                // ** editing state **
                editingNoteId: null,
                editCopy:      null  // will hold a shallow clone of the note being edited
            };
        },
        methods: {
            formatDate(d) {
                return new Date(d).toLocaleDateString('de-CH');
            },

            isImageAttachment(att) {
                if (!att) return false;
                if (att.type==='IMAGE') return true;
                if (att.contentType?.startsWith('image/')) return true;
                return false;
            },

            // --- NEW: start editing a note ---
            startEdit(note) {
                this.editingNoteId = note.id;
                // shallow clone fields
                this.editCopy = {
                    ...note,
                    tags: note.tags.join(', ')
                };
            },

            // --- NEW: cancel editing ---
            cancelEdit() {
                this.editingNoteId = null;
                this.editCopy = null;
            },

            // --- NEW: save the edited note ---
            async saveEdit() {
                const dto = {
                    projectId:   this.projectId,
                    title:       this.editCopy.title,
                    category:    this.editCopy.category,
                    content:     this.editCopy.content,
                    tags:        this.editCopy.tags.split(',').map(t=>t.trim()).filter(Boolean),
                    createdById: this.editCopy.createdById
                };
                const res = await fetch(
                    `/projects/${this.projectId}/notes/${this.editingNoteId}`, {
                        method:  'PUT',
                        headers: { 'Content-Type':'application/json' },
                        body:    JSON.stringify(dto)
                    }
                );
                if (!res.ok) {
                    const txt = await res.text();
                    return alert('Fehler beim Aktualisieren: ' + txt);
                }
                const updated = await res.json();
                // replace in notes[]
                const idx = this.notes.findIndex(n=>n.id===this.editingNoteId);
                if (idx!==-1) {
                    this.notes.splice(idx,1,{
                        ...updated,
                        tags: updated.tags,
                        attachments: this.notes[idx].attachments // preserve existing attachments
                    });
                }
                this.cancelEdit();
            },

            // --- existing handlers for create, delete, upload ---
            onFilePickedForNew(e) {
                const f = e.target.files[0];
                if (!f) return;
                this.newNote.pendingFile = f;
                this.newNote.previewUrl  = URL.createObjectURL(f);
            },

            async removeAttachment(noteIndex, attIndex) {
                const note = this.notes[noteIndex];
                const att  = note.attachments[attIndex];
                const res = await fetch(
                    `/projects/${this.projectId}/notes/${note.id}/attachments/${att.id}`,
                    { method:'DELETE' }
                );
                if (res.ok) note.attachments.splice(attIndex,1);
                else alert('Fehler beim Löschen des Anhangs');
            },

            async saveNote() {
                if (!this.newNote.category)   return alert('Kategorie fehlt');
                if (!this.newNote.createdById) return alert('Ersteller fehlt');

                // create
                const payload = {
                    projectId:   this.projectId,
                    title:       this.newNote.title,
                    category:    this.newNote.category,
                    content:     this.newNote.content,
                    tags:        this.newNote.tags.split(',').map(t=>t.trim()).filter(Boolean),
                    createdById: this.newNote.createdById
                };
                const cr = await fetch(
                    `/projects/${this.projectId}/notes`, {
                        method:'POST',
                        headers:{ 'Content-Type':'application/json' },
                        body: JSON.stringify(payload)
                    }
                );
                if (!cr.ok) {
                    const t = await cr.text();
                    return alert('Fehler beim Speichern: '+t);
                }
                const newDto = await cr.json();
                // prepend
                this.notes.unshift({
                    ...newDto,
                    tags: newDto.tags,
                    attachments: []
                });

                // upload attachment if any
                if (this.newNote.pendingFile) {
                    const form = new FormData();
                    form.append('file', this.newNote.pendingFile);
                    const upr = await fetch(
                        `/projects/${this.projectId}/notes/${newDto.id}/attachments`,
                        { method:'POST', body: form }
                    );
                    if (upr.ok) {
                        const att = await upr.json();
                        const idx = this.notes.findIndex(n=>n.id===newDto.id);
                        if (idx!==-1) this.notes[idx].attachments.push(att);
                    }
                }

                // reset
                Object.assign(this.newNote,{
                    title:'',category:null,content:'',tags:'',createdById:null,
                    pendingFile:null,previewUrl:null
                });
            }
        },
        template: `
      <div>
        <div class="mb-4">
          <a :href="'/projects/'+projectId" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left me-1"></i>Zurück
          </a>
        </div>

        <!-- note cards -->
        <div v-for="(note,i) in notes" :key="note.id" class="card mb-4 shadow-sm">
          <div class="card-header d-flex justify-content-between align-items-center"
               :class="editingNoteId===note.id ? 'bg-warning':'bg-primary text-white'">
            <h5 class="mb-0">
              <i class="bi bi-journal-text me-2"></i>
              <span v-if="editingNoteId!==note.id">{{ note.title || '–' }}</span>
              <input v-else v-model="editCopy.title" class="form-control" />
            </h5>
            <div>
              <button v-if="editingNoteId!==note.id" @click="startEdit(note)"
                      class="btn btn-sm btn-light">Edit</button>
              <button v-else @click="saveEdit" class="btn btn-sm btn-success me-1">Save</button>
              <button v-if="editingNoteId===note.id" @click="cancelEdit"
                      class="btn btn-sm btn-secondary">Cancel</button>
            </div>
          </div>

          <div class="card-body">
            <div class="mb-2">
              <label class="form-label">Kategorie:</label>
              <span v-if="editingNoteId!==note.id">{{ note.category }}</span>
              <select v-else v-model="editCopy.category" class="form-select form-select-sm">
                <option v-for="c in categories" :key="c" :value="c">{{c}}</option>
              </select>
            </div>

            <div class="mb-2">
              <label class="form-label">Inhalt:</label>
              <p v-if="editingNoteId!==note.id">{{ note.content }}</p>
              <textarea v-else v-model="editCopy.content" class="form-control" rows="3"></textarea>
            </div>

            <div class="mb-2">
              <label class="form-label">Tags:</label>
              <div v-if="editingNoteId!==note.id">
                <span v-for="t in note.tags" :key="t" class="badge bg-info me-1">{{t}}</span>
              </div>
              <input v-else v-model="editCopy.tags" class="form-control form-control-sm" />
            </div>

            <div class="mb-2">
              <small>Erstellt von:</small> {{ note.createdByName }}
            </div>

            <!-- attachments display unchanged -->
            <div v-if="note.attachments.length" class="mb-2">
              <small class="text-muted">Anhänge:</small>
              <div v-for="(att,ai) in note.attachments" :key="att.id"
                   class="d-flex align-items-center mb-1">
                <template v-if="isImageAttachment(att)">
                  <img :src="att.url" class="img-fluid img-thumbnail" style="max-width:200px"/>
                </template>
                <template v-else>
                  <a :href="att.url" target="_blank">{{att.caption||'Datei'}}</a>
                </template>
                <button @click="removeAttachment(i,ai)"
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
