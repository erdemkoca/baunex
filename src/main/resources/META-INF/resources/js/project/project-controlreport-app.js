// project-controlreport-app.js
import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('project-controlreport-app');
    if (!el) return;

    // parse initial JSON or create empty
    let report;
    try {
        const raw = el.dataset.controlReport;
        report = raw ? JSON.parse(raw) : createEmpty();
    } catch (e) {
        console.error('Invalid controlReport JSON:', e);
        report = createEmpty();
    }

    function createEmpty() {
        const nowDate     = new Date().toISOString().slice(0,10)  // "YYYY-MM-DD"
        const nowDateTime = new Date().toISOString().slice(0,16)  // "YYYY-MM-DDThh:mm"
        return {
            id: null,
            reportNumber: '',
            client:    { type: null, name:'', street:'', postalCode:'', city:'' },
            contractor:{ type: null, company:'', street:'', postalCode:'', city:'' },
            installationLocation: {
                street:'', postalCode:'', city:'', buildingType:'', parcelNumber:''
            },
            controlDate: nowDate,
            controllerId: null,
            controllerPhone: '',
            controlScope: '',
            hasDefects: false,
            deadlineNote: '',
            generalNotes: '',
            defectPositions: [],
            completionConfirmation: {
                completionDate: nowDateTime,
                companyStamp: '',
                completionSignature: ''
            }
        };
    }

    const clientTypes     = JSON.parse(el.dataset.clientTypes      || '[]');
    const contractorTypes = JSON.parse(el.dataset.contractorTypes  || '[]');
    const employees       = JSON.parse(el.dataset.employees        || '[]');

    createApp({
        data() {
            const d = JSON.parse(JSON.stringify(report));
            if (!d.controlDate) d.controlDate = new Date().toISOString().slice(0,16);
            if (!d.defectPositions) d.defectPositions = [];
            if (!d.completionConfirmation) {
                d.completionConfirmation = { completionDate:'', companyStamp:'', completionSignature:'' };
            }
            return { draft: d, clientTypes, contractorTypes, employees };
        },
        watch: {
            'draft.controllerId'(newId) {
                if (!newId) {
                    this.draft.controllerPhone = '';
                    return;
                }
                const emp = this.employees.find(e => e.id === newId);
                this.draft.controllerPhone = emp?.phone || '';
            }
        },
        methods: {
            formatDate(d) {
                return new Date(d).toLocaleDateString('de-CH');
            },
            formatDateTime(d) {
                return new Date(d).toLocaleString('de-CH');
            },
            isImageAttachment(att) {
                if (!att) return false;
                if (att.type === 'IMAGE') return true;
                if (att.contentType && att.contentType.startsWith('image/')) return true;
                return false;
            },
            async save() {
                try {
                    const res = await fetch(`/projects/${this.draft.id}/controlreport`, {
                        method: 'PUT',
                        headers: { 'Content-Type':'application/json' },
                        body: JSON.stringify(this.draft)
                    });
                    if (!res.ok) throw new Error('HTTP ' + res.status);
                    this.draft = await res.json();
                    alert('Gespeichert!');
                } catch (e) {
                    console.error(e);
                    alert('Fehler beim Speichern');
                }
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
              <input v-model="draft.controlData.controlDate" type="date" class="form-control" />
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
            <div class="col-md-4">
              <label class="form-label">Telefon</label>
              <input v-model="draft.controllerPhone" type="text" readonly class="form-control" />
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

          <!-- Mängelpositionen -->
          <h6 class="mt-4">Mängelpositionen</h6>
          <div v-if="draft.defectPositions.length" class="mb-4">
            <div v-for="pos in draft.defectPositions" :key="pos.id" class="border rounded p-2 mb-2">
              <strong>#{{ pos.positionNumber }}</strong>
              <p class="mb-0">{{ pos.description }}</p>
              <div v-if="pos.photoUrl" class="mb-2">
                <template v-if="isImageAttachment(pos.photoUrl)">
                  <img :src="pos.photoUrl.url" class="img-fluid img-thumbnail" style="max-width:200px" />
                </template>
                <template v-else>
                  <a :href="pos.photoUrl.url" target="_blank">{{ pos.photoUrl.caption || 'Foto' }}</a>
                </template>
              </div>
              <div v-if="pos.normReferences && pos.normReferences.length">
                <small>Norm-Referenzen:</small>
                <ul class="mb-0">
                  <li v-for="ref in pos.normReferences" :key="ref">{{ ref }}</li>
                </ul>
              </div>
            </div>
          </div>
          <div v-else class="text-muted mb-4">Keine Mängelpositionen vorhanden.</div>
          <hr />

          <!-- Allgemeine Hinweise -->
          <h6 class="mt-4">Allgemeine Hinweise</h6>
          <textarea v-model="draft.generalNotes" class="form-control mb-4" rows="3"></textarea>
          <hr />

          <!-- Abschlussbestätigung -->
          <h6 class="mt-4">Abschlussbestätigung</h6>
          <div class="row g-3 mb-4">
            <div class="col-md-4">
              <label class="form-label">Datum</label>
              <input v-model="draft.completionConfirmation.completionDate"
                     type="datetime-local" class="form-control" />
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