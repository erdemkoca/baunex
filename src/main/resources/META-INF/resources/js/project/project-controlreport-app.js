// project-controlreport-app.js
import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('project-controlreport-app');
    if (!el) return;

    // Parse initial report data
    let report;
    try {
        const raw = el.dataset.controlReport;
        report = raw ? JSON.parse(raw) : createEmpty();
    } catch (e) {
        console.error('Invalid controlReport JSON:', e);
        report = createEmpty();
    }

    // Parse enum lists injected via data-attributes
    const clientTypes = el.dataset.clientTypes
        ? JSON.parse(el.dataset.clientTypes)
        : [];
    const contractorTypes = el.dataset.contractorTypes
        ? JSON.parse(el.dataset.contractorTypes)
        : [];
    const employees = el.dataset.employees
        ? JSON.parse(el.dataset.employees)
        : [];

    function createEmpty() {
        return {
            id: null,
            reportNumber: '',
            client: {
                type: null,
                name: '',
                street: '',
                postalCode: '',
                city: ''
            },
            contractor: {
                type: null,
                company: '',
                street: '',
                postalCode: '',
                city: ''
            },
            installationLocation: {
                street: '',
                postalCode: '',
                city: '',
                buildingType: '',
                parcelNumber: ''
            },
            controlDate: '',
            controllerId: null,
            controlScope: '',
            hasDefects: false,
            deadlineNote: '',
            generalNotes: '',
            completionConfirmation: {
                completionDate: '',
                companyStamp: '',
                completionSignature: ''
            }
        };
    }

    createApp({
        data() {

            return {
                draft: JSON.parse(JSON.stringify(report)), // deep copy
                clientTypes,
                contractorTypes,
                employees
            };
        },
        methods: {
            async save() {
                const url = `/api/controlreport/${this.draft.id}`;
                const res = await fetch(url, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(this.draft)
                });
                if (!res.ok) {
                    alert('Fehler beim Speichern');
                    return;
                }
                this.draft = await res.json();
                alert('Gespeichert!');
            }
        },
        template: `
      <div class="card">
        <div class="card-body">
          <h5>Kontrollbericht {{ draft.reportNumber || 'Neu' }}</h5>
          
          <!-- Kunde -->
          <h6 class="mt-4">Kunde</h6>
          <div class="row g-3 mb-3">
            <div class="col-md-4">
              <label class="form-label">Typ</label>
              <select class="form-select" v-model="draft.client.type">
                <option :value="null">– wählen –</option>
                <option v-for="t in clientTypes" :key="t" :value="t">{{ t }}</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label">Name</label>
              <input v-model="draft.client.name" class="form-control" />
            </div>
            <div class="col-md-4">
              <label class="form-label">Strasse</label>
              <input v-model="draft.client.street" class="form-control" />
            </div>
          </div>
          <div class="row g-3 mb-4">
            <div class="col-md-3">
              <label class="form-label">PLZ</label>
              <input v-model="draft.client.postalCode" class="form-control" />
            </div>
            <div class="col-md-3">
              <label class="form-label">Ort</label>
              <input v-model="draft.client.city" class="form-control" />
            </div>
          </div>
          <hr />

          <!-- Auftragnehmer -->
          <h6 class="mt-4">Auftragnehmer</h6>
          <div class="row g-3 mb-3">
            <div class="col-md-4">
              <label class="form-label">Typ</label>
              <select class="form-select" v-model="draft.contractor.type">
                <option :value="null">– wählen –</option>
                <option v-for="t in contractorTypes" :key="t" :value="t">{{ t }}</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label">Firma</label>
              <input v-model="draft.contractor.company" class="form-control" />
            </div>
            <div class="col-md-4">
              <label class="form-label">Strasse</label>
              <input v-model="draft.contractor.street" class="form-control" />
            </div>
          </div>
          <div class="row g-3 mb-4">
            <div class="col-md-2">
              <label class="form-label">PLZ</label>
              <input v-model="draft.contractor.postalCode" class="form-control" />
            </div>
            <div class="col-md-2">
              <label class="form-label">Ort</label>
              <input v-model="draft.contractor.city" class="form-control" />
            </div>
          </div>
          <hr />

          <!-- Installationsort -->
          <h6 class="mt-4">Installationsort</h6>
          <div class="row g-3 mb-4">
            <div class="col-md-6">
              <label class="form-label">Strasse</label>
              <input v-model="draft.installationLocation.street" class="form-control" />
            </div>
            <div class="col-md-2">
              <label class="form-label">PLZ</label>
              <input v-model="draft.installationLocation.postalCode" class="form-control" />
            </div>
            <div class="col-md-2">
              <label class="form-label">Ort</label>
              <input v-model="draft.installationLocation.city" class="form-control" />
            </div>
            <div class="col-md-4">
              <label class="form-label">Gebäudetyp</label>
              <input v-model="draft.installationLocation.buildingType" class="form-control" />
            </div>
            <div class="col-md-4">
              <label class="form-label">Parzelle</label>
              <input v-model="draft.installationLocation.parcelNumber" class="form-control" />
            </div>
          </div>
          <hr />

          <!-- Kontrolldaten -->
          <h6 class="mt-4">Kontrolldaten</h6>
          <div class="row g-3 mb-3">
            <div class="col-md-4">
              <label class="form-label">Datum</label>
              <input v-model="draft.controlDate" type="datetime-local" class="form-control" />
            </div>
            <div class="col-md-4">
              <label class="form-label">Kontrolleur</label>
              <select v-model="draft.controllerId" class="form-select">
                <option :value="null">– wählen –</option>
                <option v-for="e in employees" :key="e.id" :value="e.id">
                  {{ e.firstName }} {{ e.lastName }}
                </option>
              </select>
            </div>
            <div class="col-md-4 form-check align-self-end">
              <input v-model="draft.hasDefects" type="checkbox" class="form-check-input" id="hasDefects" />
              <label class="form-check-label" for="hasDefects">Mängel vorhanden</label>
            </div>
          </div>
          <div class="row g-3 mb-4">
            <div class="col-md-12">
              <label class="form-label">Umfang</label>
              <textarea v-model="draft.controlScope" class="form-control"></textarea>
            </div>
            <div class="col-md-12">
              <label class="form-label">Frist / Bemerkung</label>
              <textarea v-model="draft.deadlineNote" class="form-control"></textarea>
            </div>
          </div>
          <hr />

          <!-- Allgemeine Hinweise -->
          <h6 class="mt-4">Allgemeine Hinweise</h6>
          <div class="mb-4">
            <textarea v-model="draft.generalNotes" class="form-control" rows="3"></textarea>
          </div>
          <hr />

          <!-- Abschlussbestätigung -->
          <h6 class="mt-4">Abschlussbestätigung</h6>
          <div class="row g-3 mb-4">
            <div class="col-md-4">
              <label class="form-label">Datum</label>
              <input v-model="draft.completionConfirmation.completionDate" type="datetime-local" class="form-control" />
            </div>
            <div class="col-md-4">
              <label class="form-label">Stempel</label>
              <input v-model="draft.completionConfirmation.companyStamp" class="form-control" />
            </div>
            <div class="col-md-4">
              <label class="form-label">Unterschrift</label>
              <input v-model="draft.completionConfirmation.completionSignature" class="form-control" />
            </div>
          </div>

          <button @click="save" class="btn btn-primary">Speichern</button>
        </div>
      </div>
    `
    }).mount(el);
});
