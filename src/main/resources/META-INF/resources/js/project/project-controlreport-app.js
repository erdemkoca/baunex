// project-controlreport-app.js
import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('project-controlreport-app');
    if (!el) return;

    let report;
    try {
        const rawData = el.dataset.controlReport;
        if (!rawData) {
            console.error('No data-control-report attribute found');
            report = createEmptyReport();
        } else {
            report = JSON.parse(rawData);
            if (report === null) {
                report = createEmptyReport();
            }
        }
    } catch (e) {
        console.error('Error parsing control report data:', e);
        report = createEmptyReport();
    }

    function createEmptyReport() {
        return {
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
                houseNumber: '',
                postalCode: '',
                city: ''
            },
            installationStreet: '',
            installationHouseNumber: '',
            installationPostalCode: '',
            installationCity: '',
            buildingType: '',
            parcelNumber: '',
            controlDate: '',
            controllerName: '',
            controllerPhone: '',
            controlScope: '',
            hasDefects: false,
            deadlineNote: '',
            generalNotes: '',
            completionDate: '',
            companyStamp: '',
            completionSignature: '',
            reportNumber: ''
        };
    }

    createApp({
        data() {
            return {
                draft: JSON.parse(JSON.stringify(report)) // deep copy
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
            
            <h6>Kunde</h6>
            <div class="row">
              <div class="col-md-4 mb-3">
                <label class="form-label">Typ</label>
                <select class="form-select" v-model="draft.client.type">
                  <option :value="null">– wählen –</option>
                  <option v-for="t in Object.values(draft.client.type?.constructor || {})" :key="t" :value="t">
                    {{ t }}
                  </option>
                </select>
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">Name</label>
                <input v-model="draft.client.name" class="form-control" />
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">Straße</label>
                <input v-model="draft.client.street" class="form-control" />
              </div>
              <div class="col-md-3 mb-3">
                <label class="form-label">PLZ</label>
                <input v-model="draft.client.postalCode" class="form-control" />
              </div>
              <div class="col-md-3 mb-3">
                <label class="form-label">Ort</label>
                <input v-model="draft.client.city" class="form-control" />
              </div>
            </div>
            <hr>
            
            <h6>Auftragnehmer</h6>
            <div class="row">
              <div class="col-md-4 mb-3">
                <label class="form-label">Typ</label>
                <select class="form-select" v-model="draft.contractor.type">
                  <option :value="null">– wählen –</option>
                  <option v-for="t in Object.values(draft.contractor.type?.constructor || {})" :key="t" :value="t">
                    {{ t }}
                  </option>
                </select>
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">Firma</label>
                <input v-model="draft.contractor.company" class="form-control" />
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">Strasse</label>
                <input v-model="draft.contractor.street" class="form-control" />
              </div>
              <div class="col-md-2 mb-3">
                <label class="form-label">PLZ</label>
                <input v-model="draft.contractor.postalCode" class="form-control" />
              </div>
              <div class="col-md-2 mb-3">
                <label class="form-label">Ort</label>
                <input v-model="draft.contractor.city" class="form-control" />
              </div>
            </div>
            <hr>

            <h6>Installationsort</h6>
            <div class="row">
              <div class="col-md-6 mb-3">
                <label class="form-label">Strasse</label>
                <input v-model="draft.installationLocation.street" class="form-control" />
              </div>
              <div class="col-md-2 mb-3">
                <label class="form-label">PLZ</label>
                <input v-model="draft.installationLocation.postalCode" class="form-control" />
              </div>
              <div class="col-md-2 mb-3">
                <label class="form-label">Ort</label>
                <input v-model="draft.installationLocation.city" class="form-control" />
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">GebäudetypTODO</label>
                <input v-model="draft.installationLocation.buildingType" class="form-control" />
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">Parzelle</label>
                <input v-model="draft.parcelNumber" class="form-control" />
              </div>
            </div>
            <hr>

            <h6>Kontrolldaten</h6>
            <div class="row">
              <div class="col-md-4 mb-3">
                <label class="form-label">Datum</label>
                <input v-model="draft.controlDate" type="datetime-local" class="form-control" />
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">Kontrolleur</label>
                <input v-model="draft.controllerName" class="form-control" />
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">Telefon</label>
                <input v-model="draft.controllerPhone" class="form-control" />
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label">Umfang</label>
                <textarea v-model="draft.controlScope" class="form-control"></textarea>
              </div>
              <div class="col-md-6 mb-3 form-check">
                <input v-model="draft.hasDefects" type="checkbox" class="form-check-input" id="hasDefects" />
                <label class="form-check-label" for="hasDefects">Mängel vorhanden</label>
              </div>
              <div class="col-md-12 mb-3">
                <label class="form-label">Frist / Bemerkung</label>
                <textarea v-model="draft.deadlineNote" class="form-control"></textarea>
              </div>
            </div>
            <hr>

            <h6>Allgemeine Hinweise</h6>
            <div class="mb-3">
              <textarea v-model="draft.generalNotes" class="form-control" rows="3"></textarea>
            </div>
            <hr>

            <h6>Abschlussbestätigung</h6>
            <div class="row">
              <div class="col-md-4 mb-3">
                <label class="form-label">Datum</label>
                <input v-model="draft.completionDate" type="datetime-local" class="form-control" />
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">Stempel</label>
                <input v-model="draft.companyStamp" class="form-control" />
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">Unterschrift</label>
                <input v-model="draft.completionSignature" class="form-control" />
              </div>
            </div>

            <button @click="save" class="btn btn-primary">Speichern</button>
          </div>
        </div>
        `
    }).mount(el);
});
