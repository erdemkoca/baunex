import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('project-detail-app');
    const project      = JSON.parse(el.dataset.project      || '{}');
    const customers    = JSON.parse(el.dataset.customers    || '[]');
    const catalogItems = JSON.parse(el.dataset.catalogItems || '[]');
    const billing      = JSON.parse(el.dataset.billing      || '{}');
    const contacts     = JSON.parse(el.dataset.contacts     || '[]');
    const categories   = JSON.parse(el.dataset.categories   || '[]');
    const employees    = JSON.parse(el.dataset.employees    || '[]');
    const currentDate  = el.dataset.currentDate;

    createApp({
        data() {
            return {
                project: {
                    ...project,
                    notes: project.notes || [],
                    catalogItems: project.catalogItems || []
                },
                customers,
                catalogItems,
                billing,
                contacts,
                categories,
                employees,
                currentDate,
                // für neues Note/Catalog-Item
                newNote:    { title:'',category:'',content:'',tags:'', pendingFile: null },
                newCatalog: { catalogItemId:'', itemName:'', quantity:1, unitPrice:0 }
            };
        },
        methods: {
            /** 1) Speichern Projekt via JSON-API **/
            async saveProject() {
                // baue Payload
                const payload = {
                    id: this.project.id,
                    name: this.project.name,
                    customerId: this.project.customerId,
                    budget: this.project.budget,
                    startDate: this.project.startDate,
                    endDate: this.project.endDate,
                    description: this.project.description,
                    status: this.project.status,
                    street: this.project.street,
                    city: this.project.city,
                    // Notizen ohne Attachments
                    notes: this.project.notes.map(n => ({
                        id: n.id,
                        title: n.title,
                        category: n.category,
                        content: n.content,
                        tags: n.tags.split(',').map(t=>t.trim()).filter(Boolean)
                    }))
                };
                const res = await fetch('/projects/api/save', {
                    method: 'POST',
                    headers: { 'Content-Type':'application/json' },
                    body: JSON.stringify(payload)
                });
                if (!res.ok) {
                    alert('Fehler beim Speichern: ' + await res.text());
                    return;
                }
                const { id } = await res.json();
                // nach Erstellen → Detail; nach Update → Liste
                window.location.href = id && !this.project.id
                    ? `/projects/${id}`
                    : '/projects';
            },

            /** Notizen-Handling **/
            addNote() {
                this.project.notes.push({
                    id: null, title:'', category:'', content:'', tags:'', pendingFile: null
                });
            },
            removeNote(idx) {
                this.project.notes.splice(idx, 1);
            },
            onFilePicked(idx, event) {
                this.project.notes[idx].pendingFile = event.target.files[0] || null;
            },

            /** Katalog‐Artikel hinzufügen/löschen **/
            async addCatalogItem() {
                const form = new FormData();
                form.append('catalogItemId', this.newCatalog.catalogItemId);
                form.append('itemName', this.newCatalog.itemName);
                form.append('quantity', this.newCatalog.quantity);
                form.append('unitPrice', this.newCatalog.unitPrice);
                const res = await fetch(`/projects/${this.project.id}/catalog/save`, {
                    method: 'POST', body: form
                });
                if (res.ok) {
                    const dto = await res.json();
                    this.project.catalogItems.push(dto);
                    // reset
                    this.newCatalog = { catalogItemId:'', itemName:'', quantity:1, unitPrice:0 };
                } else {
                    alert('Fehler beim Hinzufügen des Artikels');
                }
            },
            async removeCatalogItem(item) {
                const res = await fetch(
                    `/projects/${this.project.id}/catalog/${item.id}/delete`,
                    { method: 'POST' }
                );
                if (res.ok) {
                    this.project.catalogItems =
                        this.project.catalogItems.filter(ci => ci.id !== item.id);
                } else {
                    alert('Fehler beim Löschen des Artikels');
                }
            },

            formatDate(d) {
                return d
                    ? new Date(d).toLocaleDateString('de-CH')
                    : '—';
            }
        },
        template: `
    <div class="card mb-4">
      <div class="card-header">
        {{ project.id ? 'Projekt bearbeiten: ' + project.name : 'Neues Projekt erstellen' }}
      </div>
      <div class="card-body">
        <form @submit.prevent="saveProject">
          <!-- Name, Kunde, Budget, Status etc. -->
          <div class="mb-3">
            <label class="form-label">Projektname</label>
            <input v-model="project.name" type="text" class="form-control" required>
          </div>
          <div class="mb-3">
            <label class="form-label">Kunde</label>
            <select v-model.number="project.customerId" class="form-select" required>
              <option value="">-- auswählen --</option>
              <option v-for="c in customers" :key="c.id" :value="c.id">
                {{ c.companyName || (c.firstName + ' ' + c.lastName) }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-label">Budget (CHF)</label>
            <input v-model.number="project.budget" type="number" min="0" class="form-control" required>
          </div>
          <div class="mb-3">
            <label class="form-label">Status</label>
            <select v-model="project.status" class="form-select">
              <option v-for="st in ['PLANNED','IN_PROGRESS','COMPLETED','CANCELLED']" :key="st" :value="st">
                {{ st }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-label">Beschreibung</label>
            <textarea v-model="project.description" class="form-control" rows="3"></textarea>
          </div>
          <div class="row g-3 mb-4">
            <div class="col">
              <label class="form-label">Startdatum</label>
              <input v-model="project.startDate" type="date" class="form-control">
            </div>
            <div class="col">
              <label class="form-label">Enddatum</label>
              <input v-model="project.endDate" type="date" class="form-control">
            </div>
          </div>
          <div class="row g-3 mb-4">
            <div class="col">
              <label class="form-label">Strasse</label>
              <input v-model="project.street" type="text" class="form-control">
            </div>
            <div class="col">
              <label class="form-label">Stadt</label>
              <input v-model="project.city" type="text" class="form-control">
            </div>
          </div>

          <!-- Notizen -->
          <fieldset class="border rounded p-3 mb-4">
            <legend class="float-none w-auto px-2">Notizen</legend>
            <div v-for="(note, i) in project.notes" :key="i" class="note-block mb-3 border rounded p-2">
              <div class="mb-2"><label class="form-label">Titel</label>
                <input v-model="note.title" class="form-control"></div>
              <div class="mb-2"><label class="form-label">Kategorie</label>
                <select v-model="note.category" class="form-select">
                  <option v-for="cat in categories" :key="cat" :value="cat">{{cat}}</option>
                </select></div>
              <div class="mb-2"><label class="form-label">Inhalt</label>
                <textarea v-model="note.content" class="form-control" rows="2"></textarea></div>
              <div class="mb-2"><label class="form-label">Tags</label>
                <input v-model="note.tags" class="form-control" placeholder="Komma-getrennt"></div>
              <div class="mb-2">
                <label class="form-label">Datei</label>
                <input type="file" @change="onFilePicked(i,$event)" class="form-control">
              </div>
              <button type="button" class="btn btn-danger btn-sm" @click="removeNote(i)">
                Entfernen
              </button>
            </div>
            <button type="button" class="btn btn-outline-primary btn-sm" @click="addNote">
              <i class="bi bi-plus-circle me-1"></i>Notiz hinzufügen
            </button>
          </fieldset>

                    <!-- Katalogartikel -->
          <fieldset class="mb-4">
            <legend>Katalogartikel</legend>
            <div class="row g-2 align-items-end">
              <div class="col">
                <select v-model="newCatalog.catalogItemId" class="form-select">
                  <option value="">-- aus Katalog auswählen --</option>
                  <option v-for="ci in catalogItems" :key="ci.id" :value="ci.id">
                    {{ ci.name }} ({{ ci.unitPrice }} CHF)
                  </option>
                </select>
              </div>
              <div class="col">
                <input
                  v-model="newCatalog.itemName"
                  class="form-control"
                  placeholder="Oder neuen Namen"
                />
              </div>
              <div class="col">
                <input
                  v-model.number="newCatalog.quantity"
                  type="number"
                  class="form-control"
                  min="1"
                />
              </div>
              <div class="col">
                <input
                  v-model.number="newCatalog.unitPrice"
                  type="number"
                  class="form-control"
                  step="0.01"
                  placeholder="Preis"
                />
              </div>
              <div class="col-auto">
                <button
                  type="button"
                  class="btn btn-success"
                  @click="addCatalogItem"
                >
                  <i class="bi bi-plus-circle"></i>
                </button>
              </div>
            </div>
            <table
              class="table table-sm mt-3"
              v-if="project.catalogItems.length"
            >
              <thead>
                <tr>
                  <th>Artikel</th>
                  <th>Menge</th>
                  <th>Preis</th>
                  <th class="text-end">Aktion</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="ci in project.catalogItems" :key="ci.id">
                  <td>{{ ci.itemName }}</td>
                  <td>{{ ci.quantity }}</td>
                  <td>{{ ci.unitPrice }} CHF</td>
                  <td class="text-end">
                    <button
                      class="btn btn-sm btn-outline-danger"
                      @click="removeCatalogItem(ci)"
                    >
                      <i class="bi bi-trash"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <p v-else class="text-muted mt-3">
              Noch keine Artikel hinzugefügt.
            </p>
          </fieldset>

          <!-- Speichern / Abbrechen -->
          <div class="d-flex gap-2">
            <button type="submit" class="btn btn-primary">
              <i class="bi bi-save me-1"></i>
              {{ project.id ? 'Aktualisieren' : 'Erstellen' }}
            </button>
            <a href="/projects" class="btn btn-outline-secondary">
              Abbrechen
            </a>
          </div>
        </form>
      </div>
    </div>
    `
    }).mount(el);
});
