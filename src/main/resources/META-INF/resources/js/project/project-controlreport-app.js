// /js/project/project-controlreport-app.js
import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('project-controlreport-app');
    if (!el) return;

    const reportData = JSON.parse(el.dataset.report || '{}');

    createApp({
        data() {
            return {
                report: reportData,
                // falls du ein Bearbeitungs-Formular brauchst:
                editMode: false,
                draft: JSON.parse(JSON.stringify(reportData))
            };
        },
        methods: {
            async saveReport() {
                // Beispiel: PUT /api/controlreport/{id}
                const res = await fetch(`/api/controlreport/${this.report.id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(this.draft)
                });
                if (res.ok) {
                    this.report = await res.json();
                    this.editMode = false;
                } else {
                    alert('Fehler beim Speichern');
                }
            },
            toggleEdit() {
                this.editMode = !this.editMode;
                this.draft = JSON.parse(JSON.stringify(this.report));
            }
        },
        template: `
      <div class="card">
        <div class="card-body">
          <h5>Kontrollbericht {{ report.reportNumber }}</h5>
          <button class="btn btn-sm btn-outline-primary mb-3" @click="toggleEdit">
            {{ editMode ? 'Abbrechen' : 'Bearbeiten' }}
          </button>

          <div v-if="!editMode">
            <p><strong>Kontrolldatum:</strong> {{ report.controlData.controlDate }}</p>
            <p><strong>Kontrolleur:</strong> {{ report.controlData.controllerName }}</p>
            <p><strong>Mängel:</strong> {{ report.controlData.hasDefects ? 'Ja' : 'Nein' }}</p>
            <p><strong>Frist:</strong> {{ report.controlData.deadlineNote }}</p>
            <hr>
            <p><strong>Allgemeine Hinweise:</strong> {{ report.generalNotes }}</p>
            <!-- mehr Felder nach Bedarf… -->
          </div>

          <form v-else @submit.prevent="saveReport">
            <div class="mb-3">
              <label class="form-label">Kontrolldatum</label>
              <input v-model="draft.controlData.controlDate" type="datetime-local" class="form-control">
            </div>
            <div class="mb-3">
              <label class="form-label">Kontrolleur</label>
              <input v-model="draft.controlData.controllerName" class="form-control">
            </div>
            <div class="mb-3 form-check">
              <input v-model="draft.controlData.hasDefects" type="checkbox" class="form-check-input" id="hasDefects">
              <label class="form-check-label" for="hasDefects">Mängel vorhanden</label>
            </div>
            <div class="mb-3">
              <label class="form-label">Frist / Bemerkung</label>
              <textarea v-model="draft.controlData.deadlineNote" class="form-control"></textarea>
            </div>
            <div class="mb-3">
              <label class="form-label">Allgemeine Hinweise</label>
              <textarea v-model="draft.generalNotes" class="form-control"></textarea>
            </div>
            <button type="submit" class="btn btn-primary">Speichern</button>
          </form>
        </div>
      </div>
    `
    }).mount(el);
});
