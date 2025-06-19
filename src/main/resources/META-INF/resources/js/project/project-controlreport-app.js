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

            return { 
                draft: d, 
                clientTypes, 
                contractorTypes, 
                employees, 
                projectTypes,
                editingDefectPosition: null,
                newNormReference: '',
                expandedSections: {
                    client: true,
                    contractor: true,
                    installation: true,
                    control: true,
                    defects: true,
                    notes: true,
                    completion: false
                }
            };
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
            toggleSection(section) {
                this.expandedSections[section] = !this.expandedSections[section];
            },
            startEditingDefect(defect) {
                this.editingDefectPosition = { ...defect };
            },
            cancelEditingDefect() {
                this.editingDefectPosition = null;
            },
            saveEditingDefect() {
                const index = this.draft.defectPositions.findIndex(d => d.id === this.editingDefectPosition.id);
                if (index !== -1) {
                    this.draft.defectPositions[index] = { ...this.editingDefectPosition };
                }
                this.editingDefectPosition = null;
            },
            addNormReference(defect) {
                if (this.newNormReference.trim()) {
                    if (!defect.normReferences) defect.normReferences = [];
                    defect.normReferences.push(this.newNormReference.trim());
                    this.newNormReference = '';
                }
            },
            removeNormReference(defect, index) {
                defect.normReferences.splice(index, 1);
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
      <div class="card shadow-sm">
        <div class="card-header bg-primary text-white">
          <h4 class="mb-0">
            <i class="fas fa-clipboard-check me-2"></i>
            Kontrollbericht {{ draft.reportNumber || 'Neu' }}
          </h4>
        </div>
        <div class="card-body">
          
          <!-- Kunde -->
          <div class="section-card mb-4">
            <div class="section-header" @click="toggleSection('client')">
              <h5 class="mb-0">
                <i class="fas fa-user me-2"></i>
                Kunde
                <i :class="expandedSections.client ? 'fas fa-chevron-down' : 'fas fa-chevron-right'" class="float-end"></i>
              </h5>
            </div>
            <div v-if="expandedSections.client" class="section-content">
              <div class="row g-3 mb-3">
                <div class="col-md-4">
                  <label class="form-label fw-bold">Typ</label>
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
                  <label class="form-label fw-bold">Name</label>
                  <input v-model="draft.client.name" class="form-control" />
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-bold">Strasse</label>
                  <input v-model="draft.client.street" class="form-control" />
                </div>
              </div>
              <div class="row g-3">
                <div class="col-md-3">
                  <label class="form-label fw-bold">PLZ</label>
                  <input v-model="draft.client.postalCode" class="form-control" />
                </div>
                <div class="col-md-3">
                  <label class="form-label fw-bold">Ort</label>
                  <input v-model="draft.client.city" class="form-control" />
                </div>
              </div>
            </div>
          </div>

          <!-- Auftragnehmer -->
          <div class="section-card mb-4">
            <div class="section-header" @click="toggleSection('contractor')">
              <h5 class="mb-0">
                <i class="fas fa-building me-2"></i>
                Auftragnehmer
                <i :class="expandedSections.contractor ? 'fas fa-chevron-down' : 'fas fa-chevron-right'" class="float-end"></i>
              </h5>
            </div>
            <div v-if="expandedSections.contractor" class="section-content">
              <div class="row g-3 mb-3">
                <div class="col-md-4">
                  <label class="form-label fw-bold">Typ</label>
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
                  <label class="form-label fw-bold">Firma</label>
                  <input v-model="draft.contractor.company" class="form-control" />
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-bold">Strasse</label>
                  <input v-model="draft.contractor.street" class="form-control" />
                </div>
              </div>
              <div class="row g-3">
                <div class="col-md-2">
                  <label class="form-label fw-bold">PLZ</label>
                  <input v-model="draft.contractor.postalCode" class="form-control" />
                </div>
                <div class="col-md-2">
                  <label class="form-label fw-bold">Ort</label>
                  <input v-model="draft.contractor.city" class="form-control" />
                </div>
              </div>
            </div>
          </div>

          <!-- Installationsort -->
          <div class="section-card mb-4">
            <div class="section-header" @click="toggleSection('installation')">
              <h5 class="mb-0">
                <i class="fas fa-map-marker-alt me-2"></i>
                Installationsort
                <i :class="expandedSections.installation ? 'fas fa-chevron-down' : 'fas fa-chevron-right'" class="float-end"></i>
              </h5>
            </div>
            <div v-if="expandedSections.installation" class="section-content">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label fw-bold">Strasse</label>
                  <input v-model="draft.installationLocation.street" class="form-control" />
                </div>
                <div class="col-md-2">
                  <label class="form-label fw-bold">PLZ</label>
                  <input v-model="draft.installationLocation.postalCode" class="form-control" />
                </div>
                <div class="col-md-2">
                  <label class="form-label fw-bold">Ort</label>
                  <input v-model="draft.installationLocation.city" class="form-control" />
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-bold">Gebäudetyp</label>
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
                  <label class="form-label fw-bold">Parzelle</label>
                  <input v-model="draft.installationLocation.parcelNumber" class="form-control" />
                </div>
              </div>
            </div>
          </div>

          <!-- Kontrolldaten -->
          <div class="section-card mb-4">
            <div class="section-header" @click="toggleSection('control')">
              <h5 class="mb-0">
                <i class="fas fa-search me-2"></i>
                Kontrolldaten
                <i :class="expandedSections.control ? 'fas fa-chevron-down' : 'fas fa-chevron-right'" class="float-end"></i>
              </h5>
            </div>
            <div v-if="expandedSections.control" class="section-content">
              <div class="row g-3 mb-3">
                <div class="col-md-4">
                  <label class="form-label fw-bold">Datum</label>
                  <input v-model="draft.controlData.controlDate" type="date" class="form-control" />
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-bold">Kontrolleur</label>
                  <select v-model="draft.controllerId" class="form-select">
                    <option :value="null">– wählen –</option>
                    <option v-for="e in employees" :key="e.id" :value="e.id">
                      {{ e.firstName }} {{ e.lastName }}
                    </option>
                  </select>
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-bold">Telefon</label>
                  <input v-model="draft.controllerPhone" type="text" readonly class="form-control bg-light" />
                </div>
                <div class="col-md-4 form-check align-self-end">
                  <input v-model="draft.hasDefects" type="checkbox" class="form-check-input" id="hasDefects" />
                  <label class="form-check-label fw-bold" for="hasDefects">
                    <i class="fas fa-exclamation-triangle text-warning me-1"></i>
                    Mängel vorhanden
                  </label>
                </div>
              </div>
              <div class="row g-3">
                <div class="col-md-12">
                  <label class="form-label fw-bold">Umfang</label>
                  <textarea v-model="draft.controlScope" class="form-control" rows="3"></textarea>
                </div>
                <div class="col-md-12">
                  <label class="form-label fw-bold">Frist / Bemerkung</label>
                  <textarea v-model="draft.deadlineNote" class="form-control" rows="2"></textarea>
                </div>
              </div>
            </div>
          </div>

          <!-- Mängelpositionen -->
          <div class="section-card mb-4">
            <div class="section-header" @click="toggleSection('defects')">
              <h5 class="mb-0">
                <i class="fas fa-exclamation-triangle text-warning me-2"></i>
                Mängelpositionen ({{ draft.defectPositions.length }})
                <i :class="expandedSections.defects ? 'fas fa-chevron-down' : 'fas fa-chevron-right'" class="float-end"></i>
              </h5>
            </div>
            <div v-if="expandedSections.defects" class="section-content">
              <div v-if="draft.defectPositions.length" class="mb-4">
                <div v-for="(pos, index) in draft.defectPositions" :key="pos.id" class="defect-position-card mb-3">
                  <div class="defect-header">
                    <h6 class="mb-0">
                      <span class="badge bg-warning text-dark me-2">#{{ pos.positionNumber }}</span>
                      {{ pos.description.substring(0, 50) }}{{ pos.description.length > 50 ? '...' : '' }}
                    </h6>
                    <button @click="startEditingDefect(pos)" class="btn btn-sm btn-outline-primary">
                      <i class="fas fa-edit"></i> Bearbeiten
                    </button>
                  </div>
                  
                  <!-- View Mode -->
                  <div v-if="editingDefectPosition?.id !== pos.id" class="defect-content">
                    <p class="text-muted mb-2">{{ pos.description }}</p>
                    
                    <div v-if="pos.buildingLocation" class="mb-2">
                      <strong>Ort:</strong> {{ pos.buildingLocation }}
                    </div>
                    
                    <div v-if="pos.photoUrls && pos.photoUrls.length" class="mb-3">
                      <strong>Fotos:</strong>
                      <div class="photo-gallery">
                        <div v-for="photo in pos.photoUrls" :key="photo.id" class="photo-item">
                          <template v-if="isImageAttachment(photo)">
                            <img :src="photo.url" class="img-fluid img-thumbnail" style="max-width:150px; max-height:150px;" />
                          </template>
                          <template v-else>
                            <a :href="photo.url" target="_blank" class="btn btn-sm btn-outline-secondary">
                              <i class="fas fa-file"></i> {{ photo.caption || 'Foto' }}
                            </a>
                          </template>
                        </div>
                      </div>
                    </div>
                    
                    <div v-if="pos.normReferences && pos.normReferences.length" class="mb-2">
                      <strong>Norm-Referenzen:</strong>
                      <div class="norm-references">
                        <span v-for="(ref, refIndex) in pos.normReferences" :key="refIndex" class="badge bg-info me-1">
                          {{ ref }}
                        </span>
                      </div>
                    </div>
                  </div>
                  
                  <!-- Edit Mode -->
                  <div v-else class="defect-edit-form">
                    <div class="row g-3">
                      <div class="col-md-12">
                        <label class="form-label fw-bold">Beschreibung</label>
                        <textarea v-model="editingDefectPosition.description" class="form-control" rows="3"></textarea>
                      </div>
                      <div class="col-md-6">
                        <label class="form-label fw-bold">Ort im Gebäude</label>
                        <input v-model="editingDefectPosition.buildingLocation" class="form-control" placeholder="z.B. Küche, rechte Seite" />
                      </div>
                      <div class="col-md-6">
                        <label class="form-label fw-bold">Norm-Referenzen</label>
                        <div class="input-group">
                          <input v-model="newNormReference" @keyup.enter="addNormReference(editingDefectPosition)" class="form-control" placeholder="z.B. SIA 118" />
                          <button @click="addNormReference(editingDefectPosition)" class="btn btn-outline-secondary" type="button">
                            <i class="fas fa-plus"></i>
                          </button>
                        </div>
                        <div v-if="editingDefectPosition.normReferences && editingDefectPosition.normReferences.length" class="mt-2">
                          <span v-for="(ref, refIndex) in editingDefectPosition.normReferences" :key="refIndex" class="badge bg-info me-1">
                            {{ ref }}
                            <i @click="removeNormReference(editingDefectPosition, refIndex)" class="fas fa-times ms-1" style="cursor: pointer;"></i>
                          </span>
                        </div>
                      </div>
                    </div>
                    <div class="mt-3">
                      <button @click="saveEditingDefect()" class="btn btn-success me-2">
                        <i class="fas fa-save"></i> Speichern
                      </button>
                      <button @click="cancelEditingDefect()" class="btn btn-secondary">
                        <i class="fas fa-times"></i> Abbrechen
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="text-muted text-center py-4">
                <i class="fas fa-check-circle text-success fa-2x mb-2"></i>
                <p>Keine Mängelpositionen vorhanden.</p>
              </div>
            </div>
          </div>

          <!-- Allgemeine Hinweise -->
          <div class="section-card mb-4">
            <div class="section-header" @click="toggleSection('notes')">
              <h5 class="mb-0">
                <i class="fas fa-sticky-note me-2"></i>
                Allgemeine Hinweise
                <i :class="expandedSections.notes ? 'fas fa-chevron-down' : 'fas fa-chevron-right'" class="float-end"></i>
              </h5>
            </div>
            <div v-if="expandedSections.notes" class="section-content">
              <textarea v-model="draft.generalNotes" class="form-control" rows="4" placeholder="Allgemeine Bemerkungen zur Kontrolle..."></textarea>
            </div>
          </div>

          <!-- Abschlussbestätigung -->
          <div class="section-card mb-4">
            <div class="section-header" @click="toggleSection('completion')">
              <h5 class="mb-0">
                <i class="fas fa-flag-checkered me-2"></i>
                Abschlussbestätigung
                <i :class="expandedSections.completion ? 'fas fa-chevron-down' : 'fas fa-chevron-right'" class="float-end"></i>
              </h5>
            </div>
            <div v-if="expandedSections.completion" class="section-content">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label fw-bold">Abschlussdatum</label>
                  <input v-model="draft.completionDate" type="datetime-local" class="form-control" />
                </div>
                <div class="col-md-12">
                  <label class="form-label fw-bold">Mängelbeseitigung Bemerkung</label>
                  <textarea v-model="draft.defectResolverNote" class="form-control" rows="3" placeholder="Bemerkungen zur Mängelbeseitigung..."></textarea>
                </div>
              </div>
            </div>
          </div>

          <!-- Save Button -->
          <div class="text-center">
            <button @click="save" class="btn btn-primary btn-lg">
              <i class="fas fa-save me-2"></i>
              Kontrollbericht speichern
            </button>
          </div>
        </div>
      </div>

      <style>
        .section-card {
          border: 1px solid #dee2e6;
          border-radius: 8px;
          overflow: hidden;
        }
        
        .section-header {
          background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
          padding: 15px 20px;
          cursor: pointer;
          transition: all 0.3s ease;
          border-bottom: 1px solid #dee2e6;
        }
        
        .section-header:hover {
          background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
        }
        
        .section-content {
          padding: 20px;
          background: white;
        }
        
        .defect-position-card {
          border: 1px solid #dee2e6;
          border-radius: 8px;
          padding: 15px;
          background: #f8f9fa;
        }
        
        .defect-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 15px;
        }
        
        .defect-content {
          background: white;
          padding: 15px;
          border-radius: 6px;
          border: 1px solid #e9ecef;
        }
        
        .defect-edit-form {
          background: white;
          padding: 15px;
          border-radius: 6px;
          border: 2px solid #007bff;
        }
        
        .photo-gallery {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
          margin-top: 10px;
        }
        
        .photo-item {
          flex: 0 0 auto;
        }
        
        .norm-references {
          margin-top: 5px;
        }
        
        .form-label {
          color: #495057;
        }
        
        .card-header {
          background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
        }
      </style>
    `
    }).mount(el);
});