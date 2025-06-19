import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('project-controlreport-app');
    if (!el) return;

    let report;
    try {
        const raw = el.dataset.controlReport;
        report = raw ? JSON.parse(raw) : createEmpty();
    } catch (e) {
        console.error('Invalid controlReport JSON:', e);
        report = createEmpty();
    }

    function createEmpty() {
        const nowDate = new Date().toISOString().slice(0,10);
        return {
            id: null,
            reportNumber: '',
            pageCount: 1,
            currentPage: 1,

            client:    { type: '', name:'', street:'', postalCode:'', city:'' },
            contractor:{ type: '', company:'', street:'', postalCode:'', city:'' },
            installationLocation: {
                street:'', postalCode:'', city:'', buildingType:'', parcelNumber:''
            },

            controlScope: '',
            controlData: {
                controlDate: nowDate,
                controllerId: null,
                controllerFirstName: null,
                controllerLastName: null,
                phoneNumber: null,
                hasDefects: false,
                deadlineNote: ''
            },

            generalNotes: '',
            defectResolverNote: '',
            completionDate: null,
            defectPositions: [],
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
        };
    }

    const clientTypes     = JSON.parse(el.dataset.clientTypes      || '[]');
    const contractorTypes = JSON.parse(el.dataset.contractorTypes  || '[]');
    const employees       = JSON.parse(el.dataset.employees        || '[]');
    const projectTypes    = JSON.parse(el.dataset.projectTypes     || '[]');

    // 🧠 Fix initial Enum values if they were stored as displayName
    function normalizeEnumField(value, options) {
        const match = options.find(opt => opt.label === value || opt.code === value);
        return match ? match.code : null;
    }

    // Apply normalizations
    if (report) {
        if (report.client) {
            report.client.type = normalizeEnumField(report.client.type, clientTypes);
        }
        if (report.contractor) {
            report.contractor.type = normalizeEnumField(report.contractor.type, contractorTypes);
        }
        if (report.installationLocation) {
            report.installationLocation.buildingType = normalizeEnumField(report.installationLocation.buildingType, projectTypes);
        }

        report.client.type        = normalizeEnumField(report.client.type, clientTypes);
        report.contractor.type    = normalizeEnumField(report.contractor.type, contractorTypes);
        report.installationLocation.buildingType = normalizeEnumField(report.installationLocation.buildingType, projectTypes);
    }

    createApp({
        data() {
            const d = JSON.parse(JSON.stringify(report));
            if (!d.controlData) d.controlData = {};
            if (!d.controlData.controlDate) d.controlData.controlDate = new Date().toISOString().slice(0,10);
            if (!d.defectPositions) d.defectPositions = [];

            // Extract control data to top level for easier access
            if (d.controlData) {
                d.controllerId    = d.controlData.controllerId;
                d.controllerPhone = d.controlData.phoneNumber;
                d.hasDefects      = d.controlData.hasDefects;
                d.deadlineNote    = d.controlData.deadlineNote;
            }

            return { draft: d, clientTypes, contractorTypes, employees, projectTypes };
        },
        watch: {
            'draft.controllerId'(newId) {
                const emp = this.employees.find(e => e.id === newId);
                this.draft.controllerPhone = emp?.phone || '';
                // Update controlData as well
                if (this.draft.controlData) {
                    this.draft.controlData.controllerId = newId;
                    this.draft.controlData.controllerFirstName = emp?.firstName || null;
                    this.draft.controlData.controllerLastName = emp?.lastName || null;
                    this.draft.controlData.phoneNumber = emp?.phone || null;
                }
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
                return att && (att.type === 'IMAGE' || att.contentType?.startsWith('image/'));
            },
            async save() {
                try {
                    const projectId = Number(el.dataset.projectId);
                    if (isNaN(projectId)) throw new Error("projectId ist nicht gesetzt");

                    // Get selected employee data
                    const selectedEmployee = this.draft.controllerId ? 
                        this.employees.find(e => e.id === this.draft.controllerId) : null;

                    const updateDto = {
                        id: this.draft.id,
                        reportNumber:          this.draft.reportNumber,
                        pageCount:             this.draft.pageCount,
                        currentPage:           this.draft.currentPage,

                        client: {
                            type:       this.draft.client.type,
                            name:       this.draft.client.name,
                            street:     this.draft.client.street,
                            postalCode: this.draft.client.postalCode,
                            city:       this.draft.client.city,
                        },
                        contractor: {
                            type:       this.draft.contractor.type,
                            company:    this.draft.contractor.company,
                            street:     this.draft.contractor.street,
                            postalCode: this.draft.contractor.postalCode,
                            city:       this.draft.contractor.city,
                        },
                        installationLocation: {
                            street:       this.draft.installationLocation.street,
                            postalCode:   this.draft.installationLocation.postalCode,
                            city:         this.draft.installationLocation.city,
                            buildingType: this.draft.installationLocation.buildingType,
                            parcelNumber: this.draft.installationLocation.parcelNumber,
                        },
                        controlScope: this.draft.controlScope,
                        controlData: {
                            controlDate:         this.draft.controlData.controlDate,
                            controllerId:        this.draft.controllerId,
                            controllerFirstName: selectedEmployee?.firstName || null,
                            controllerLastName:  selectedEmployee?.lastName || null,
                            phoneNumber:         selectedEmployee?.phone || null,
                            hasDefects:          this.draft.hasDefects,
                            deadlineNote:        this.draft.deadlineNote,
                        },
                        generalNotes:          this.draft.generalNotes,
                        defectResolverNote:    this.draft.defectResolverNote,
                        completionDate:        this.draft.completionDate,
                        defectPositions: this.draft.defectPositions.map(pos => ({
                            id: pos.id ? Number(pos.id) : null,
                            positionNumber: pos.positionNumber,
                            description: pos.description ?? '',
                            buildingLocation: pos.buildingLocation ?? '',
                            noteId: pos.noteId ?? null,
                            noteContent: pos.noteContent ?? '',
                            photoUrls: pos.photoUrls || [],
                            normReferences: pos.normReferences || []
                        })),
                        createdAt: this.draft.createdAt,
                        updatedAt: this.draft.updatedAt
                    };

                    console.log('Sending DTO:', updateDto);

                    const res = await fetch(
                        `/projects/${projectId}/controlreport`, {
                            method: 'PUT',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify(updateDto)
                        });

                    if (!res.ok) {
                        const txt = await res.text();
                        console.error('Save failed —', res.status, txt);
                        throw new Error(`HTTP ${res.status}: ${txt}`);
                    }

                    const newDto = await res.json();
                    this.draft = newDto;
                    
                    // Update local control data
                    if (newDto.controlData) {
                        this.draft.controllerId    = newDto.controlData.controllerId;
                        this.draft.controllerPhone = newDto.controlData.phoneNumber;
                        this.draft.hasDefects      = newDto.controlData.hasDefects;
                        this.draft.deadlineNote    = newDto.controlData.deadlineNote;
                    }

                    alert('Gespeichert!');
                } catch (e) {
                    console.error('Fehler in save():', e);
                    alert('Fehler beim Speichern: ' + (e.message||e));
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
                  <option
                    v-for="opt in clientTypes"
                    :key="opt.code"
                    :value="opt.code"
                  >
                    {{ opt.label }}
                  </option>
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
                  <option
                    v-for="opt in contractorTypes"
                    :key="opt.code"
                    :value="opt.code"
                  >
                    {{ opt.label }}
                  </option>
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
              <select v-model="draft.installationLocation.buildingType" class="form-select">
                <option :value="null">– wählen –</option>
                <option
                  v-for="opt in projectTypes"
                  :key="opt.code"
                  :value="opt.code"
                >
                  {{ opt.label }}
                </option>
              </select>
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
              <div v-if="pos.photoUrls && pos.photoUrls.length" class="mb-2">
                <div v-for="photo in pos.photoUrls" :key="photo.id" class="d-inline-block me-2">
                  <template v-if="isImageAttachment(photo)">
                    <img :src="photo.url" class="img-fluid img-thumbnail" style="max-width:200px" />
                  </template>
                  <template v-else>
                    <a :href="photo.url" target="_blank">{{ photo.caption || 'Foto' }}</a>
                  </template>
                </div>
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
              <input v-model="draft.completionDate" type="datetime-local" class="form-control" />
            </div>
          </div>

          <button @click="save" class="btn btn-primary">Speichern</button>
        </div>
      </div>
    `
    }).mount(el);
});