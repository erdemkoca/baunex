import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.prod.js';

// Remove global stickyBarStyle and icon helper, move into Vue app

// Execute immediately when script is loaded
function initializeForm() {
    console.log('initializeForm called');
    
    // Clean up any existing Vue app before creating a new one
    if (window.timeTrackingFormApp) {
        try {
            window.timeTrackingFormApp.unmount();
            window.timeTrackingFormApp = null;
        } catch (error) {
            console.log('Error unmounting existing app:', error);
        }
    }
    
    const el = document.getElementById('time-tracking-form-app');
    if (!el) {
        console.log('time-tracking-form-app element not found, retrying in 50ms...');
        // Limit retries to prevent infinite loop
        if (!window.formInitRetryCount) {
            window.formInitRetryCount = 0;
        }
        window.formInitRetryCount++;
        
        if (window.formInitRetryCount > 40) { // Max 2 seconds of retries (40 * 50ms)
            console.error('Failed to initialize form after 40 retries - form container may not be ready');
            return;
        }
        
        setTimeout(initializeForm, 50);
        return;
    }
    
    // Check if the element has been properly initialized by Vue
    if (!el.dataset.entry || !el.dataset.employees) {
        console.log('Form element found but not yet initialized by Vue, retrying...');
        setTimeout(initializeForm, 50);
        return;
    }
    
    // Check if the element already has a Vue app mounted
    if (el._vue_app) {
        console.log('Vue app already mounted on this element, skipping...');
        return;
    }
    
    // Get data from the element or use defaults for modal context
    const entry = JSON.parse(el.dataset.entry || '{}');
    const employees = JSON.parse(el.dataset.employees || '[]');
    const projects = JSON.parse(el.dataset.projects || '[]');
    const categories = JSON.parse(el.dataset.categories || '[]');
    const catalogItems = JSON.parse(el.dataset.catalogItems || '[]');
    const currentDate = el.dataset.currentDate || new Date().toISOString().split('T')[0];

    console.log('Parsed data:', { entry, employees, projects, categories, catalogItems, currentDate });
    
    // Check if we have valid entry data (not just empty object)
    const hasValidEntry = entry && entry.id && Object.keys(entry).length > 1;
    console.log('Has valid entry data:', hasValidEntry, entry);
    
    // If we have valid entry data, log it for debugging
    if (hasValidEntry) {
        console.log('Valid entry data found:', entry);
        console.log('Entry ID:', entry.id);
        console.log('Employee ID:', entry.employeeId);
        console.log('Project ID:', entry.projectId);
        console.log('Date:', entry.date);
        console.log('Start Time:', entry.startTime);
        console.log('End Time:', entry.endTime);
    }
    
    // If we have valid entry data, log it for debugging
    if (hasValidEntry) {
        console.log('Valid entry data found:', entry);
        console.log('Entry ID:', entry.id);
        console.log('Employee ID:', entry.employeeId);
        console.log('Project ID:', entry.projectId);
        console.log('Date:', entry.date);
        console.log('Start Time:', entry.startTime);
        console.log('End Time:', entry.endTime);
    }

    const app = createApp({
        data() {
            const entryData = {
                id: null,
                employeeId: null,
                projectId: null,
                date: currentDate,
                title: '',
                hoursWorked: 0,
                hourlyRate: 0,
                billable: true,
                invoiced: false,
                hasNightSurcharge: false,
                hasWeekendSurcharge: false,
                hasHolidaySurcharge: false,
                travelTimeMinutes: 0,
                disposalCost: 0,
                waitingTimeMinutes: 0,
                ...entry,
                // Ensure time fields are properly formatted
                startTime: entry.startTime ? entry.startTime.substring(0, 5) : '', // Convert "HH:MM:SS" to "HH:MM"
                endTime: entry.endTime ? entry.endTime.substring(0, 5) : '', // Convert "HH:MM:SS" to "HH:MM"
                breaks: (entry.breaks || []).map(breakItem => ({
                    start: breakItem.start ? breakItem.start.substring(0, 5) : '', // Convert "HH:MM:SS" to "HH:MM"
                    end: breakItem.end ? breakItem.end.substring(0, 5) : ''
                })),
                catalogItems: entry.catalogItems || [],
            };
            
            // Ensure billable is a boolean
            if (typeof entryData.billable !== 'boolean') {
                entryData.billable = Boolean(entryData.billable);
            }
            
            console.log('Vue app entry data initialized:', entryData);
            console.log('Entry billable value:', entryData.billable, typeof entryData.billable);
            console.log('Entry ID in Vue app:', entryData.id);
            console.log('Employee ID in Vue app:', entryData.employeeId);
            console.log('Project ID in Vue app:', entryData.projectId);
            console.log('Date in Vue app:', entryData.date);
            console.log('Start Time in Vue app:', entryData.startTime);
            console.log('End Time in Vue app:', entryData.endTime);
            
            return {
                entry: entryData,
                employees,
                projects,
                categories,
                catalogItems,
                currentDate,
                notes: (entry.notes || []).map(note => ({
                    ...note,
                    createdById: note.createdById || entry.employeeId,
                    tags: Array.isArray(note.tags) ? note.tags : (note.tags ? note.tags.split(',').map(t => t.trim()) : []),
                    attachments: note.attachments || []
                })),
                selectedCatalogItem: null,
                itemQuantity: 1,
                saving: false,
                activeTab: 0,
                breakStart: '',
                breakEnd: '',
                showBreaks: true,
                showMaterials: true,
                showNotes: true,
                stickyBarStyle: {
                    position: 'sticky',
                    bottom: '0',
                    background: '#fff',
                    zIndex: 100,
                    borderTop: '1px solid #dee2e6',
                    padding: '1rem 0'
                },
                showCustomTooltip: false, // New data property for tooltip
            };
        },
        computed: {
            totalBreakMinutes() {
                return this.entry.breaks.reduce((sum, br) => {
                    if (br.start && br.end) {
                        const [sh, sm] = br.start.split(':').map(Number);
                        const [eh, em] = br.end.split(':').map(Number);
                        let mins = (eh * 60 + em) - (sh * 60 + sm);
                        if (mins < 0) mins = 0;
                        return sum + mins;
                    }
                    return sum;
                }, 0);
            },
            autoHoursWorked() {
                if (!this.entry.startTime || !this.entry.endTime) return '';
                const [sh, sm] = this.entry.startTime.split(':').map(Number);
                const [eh, em] = this.entry.endTime.split(':').map(Number);
                let mins = (eh * 60 + em) - (sh * 60 + sm) - this.totalBreakMinutes;
                if (mins < 0) mins = 0;
                return (mins / 60).toFixed(2);
            },
            // Tab validation
            isErfassungTabValid() {
                return this.entry.employeeId && this.entry.projectId && this.entry.date && 
                       this.entry.startTime && this.entry.endTime && 
                       this.entry.title && this.entry.title.trim() !== '';
            },
            isOptionenTabValid() {
                // Optionen tab is optional, so always valid
                return true;
            },
            getTabIcon() {
                return (tabIndex) => {
                    const icons = ['person-badge', 'gear', 'box-seam', 'sticky', 'check2-circle'];
                    const isValid = tabIndex === 0 ? this.isErfassungTabValid : 
                                   tabIndex === 1 ? this.isOptionenTabValid : true;
                    
                    return isValid ? icons[tabIndex] : 'exclamation-triangle';
                };
            },
            getTabClass() {
                return (tabIndex) => {
                    const isValid = tabIndex === 0 ? this.isErfassungTabValid : 
                                   tabIndex === 1 ? this.isOptionenTabValid : true;
                    
                    return {
                        'nav-link': true,
                        'active': this.activeTab === tabIndex,
                        'text-warning': !isValid && this.activeTab !== tabIndex,
                        'text-danger': !isValid && this.activeTab === tabIndex
                    };
                };
            }
        },
        mounted() {
            console.log('Vue app mounted, entry.billable =', this.entry.billable);
        },
        watch: {
            'entry.billable': function(newVal, oldVal) {
                console.log('Billable changed from', oldVal, 'to', newVal);
            }
        },
        methods: {
            // Tab navigation
            setTab(i) { this.activeTab = i; },
            // Icon helper for template
            icon(name) {
                return `<i class='bi bi-${name}'></i>`;
            },
            // Tooltip helper
            getBillableTooltip() {
                const status = this.entry.billable ? 'EIN' : 'AUS';
                const description = 'Diese Zeit wird dem Kunden in Rechnung gestellt';
                return `Verrechenbar: ${status}\n${description}`;
            },
            // Breaks
            addBreak() {
                if (!this.breakStart || !this.breakEnd) return;
                this.entry.breaks.push({ start: this.breakStart, end: this.breakEnd });
                this.breakStart = '';
                this.breakEnd = '';
            },
            removeBreak(i) { this.entry.breaks.splice(i, 1); },
            // Notes
            addNote() {
                this.notes.push({
                    id: null,
                    title: '',
                    category: '',
                    content: '',
                    tags: [],
                    attachments: [],
                    pendingFile: null,
                    createdById: this.entry.employeeId,
                    createdAt: null,
                    updatedAt: null,
                    projectId: this.entry.projectId,
                    timeEntryId: this.entry.id || null,
                    documentId: null
                });
            },
            removeNote(i) { this.notes.splice(i, 1); },
            onFilePicked(noteIndex, event) {
                this.notes[noteIndex].pendingFile = event.target.files[0];
            },
            removeAttachment(i, ai) {
                this.notes[i].attachments.splice(ai, 1);
            },
            // Materials
            addCatalogItem() {
                if (!this.selectedCatalogItem) {
                    return alert('Bitte zuerst einen Artikel auswählen');
                }
                const item = this.catalogItems.find(ci => ci.id === this.selectedCatalogItem);
                if (!item) return;
                this.entry.catalogItems.push({
                    catalogItemId: item.id,
                    itemName:       item.name,
                    quantity:       this.itemQuantity,
                    unitPrice:      item.unitPrice,
                    totalPrice:     item.unitPrice * this.itemQuantity
                });
                this.selectedCatalogItem = null;
                this.itemQuantity = 1;
            },
            removeCatalogItem(i) {
                this.entry.catalogItems.splice(i, 1);
            },
            
            parseErrorMessage(error) {
                try {
                    // Versuche, die Fehlermeldung als JSON zu parsen
                    if (typeof error.message === 'string' && error.message.startsWith('{')) {
                        const errorData = JSON.parse(error.message);
                        return this.formatErrorResponse(errorData);
                    }
                    // Fallback für andere Fehlertypen
                    return error.message || 'Ein unbekannter Fehler ist aufgetreten';
                } catch (parseError) {
                    // Falls JSON-Parsing fehlschlägt, verwende die ursprüngliche Nachricht
                    return error.message || 'Ein Fehler ist aufgetreten';
                }
            },
            
            formatErrorResponse(errorData) {
                const { error, type, field, value } = errorData;
                
                // Benutzerfreundliche Fehlermeldungen basierend auf dem Fehlertyp
                switch (type) {
                    case 'DuplicateTimeEntryException':
                        return '⏰ Zeitüberschneidung: Es existiert bereits ein Zeiteintrag für diesen Zeitraum. Bitte wählen Sie einen anderen Zeitraum oder bearbeiten Sie den bestehenden Eintrag.';
                    
                    case 'MissingRequiredFieldException':
                        return `📝 **Pflichtfeld fehlt**: Das Feld "${field}" ist erforderlich. Bitte füllen Sie alle markierten Felder aus.`;
                    
                    case 'InvalidTimeRangeException':
                        return '⏰ **Ungültiger Zeitbereich**: Die Endzeit muss nach der Startzeit liegen.';
                    
                    case 'InvalidDateException':
                        return '📅 **Ungültiges Datum**: Das ausgewählte Datum ist nicht erlaubt (z.B. Wochenende, Feiertag oder Zukunft).';
                    
                    case 'EmployeeNotFoundException':
                        return '👤 **Mitarbeiter nicht gefunden**: Der ausgewählte Mitarbeiter existiert nicht mehr.';
                    
                    case 'ProjectNotFoundException':
                        return '📋 **Projekt nicht gefunden**: Das ausgewählte Projekt existiert nicht mehr.';
                    
                    case 'InvalidHoursException':
                        return '⏱️ **Ungültige Arbeitsstunden**: Die eingegebenen Stunden sind nicht zulässig.';
                    
                    case 'InvalidBreakException':
                        return '☕ **Ungültige Pause**: Die Pausenkonfiguration ist fehlerhaft.';
                    
                    case 'ValidationError':
                        return `⚠️ **Validierungsfehler**: ${error}`;
                    
                    case 'InternalError':
                    case 'UnexpectedError':
                        return '🔧 **Systemfehler**: Ein interner Fehler ist aufgetreten. Bitte versuchen Sie es später erneut oder kontaktieren Sie den Administrator.';
                    
                    default:
                        return `❌ **${type}**: ${error}`;
                }
            },
            
            showError(message) {
                // Erstelle eine schöne Fehlermeldung mit Bootstrap
                const errorHtml = `
                    <div class="alert alert-danger alert-dismissible fade show" role="alert" style="border-radius: 8px; border: none; box-shadow: 0 2px 8px rgba(220,53,69,0.2);">
                        <div style="display: flex; align-items: flex-start;">
                            <div style="flex-shrink: 0; margin-right: 12px; margin-top: 2px;">
                                <i class="bi bi-exclamation-triangle-fill" style="font-size: 1.2rem; color: #dc3545;"></i>
                            </div>
                            <div style="flex-grow: 1;">
                                <div style="font-weight: 600; margin-bottom: 4px; color: #721c24;">Fehler beim Speichern</div>
                                <div style="line-height: 1.5; color: #721c24;">${message}</div>
                            </div>
                        </div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close" style="position: absolute; top: 12px; right: 12px;"></button>
                    </div>
                `;
                
                // Füge die Fehlermeldung am Anfang des Formulars hinzu
                const formContainer = document.querySelector('#time-tracking-form-app');
                if (formContainer) {
                    // Entferne vorherige Fehlermeldungen
                    const existingAlerts = formContainer.querySelectorAll('.alert-danger');
                    existingAlerts.forEach(alert => alert.remove());
                    
                    // Füge neue Fehlermeldung hinzu
                    formContainer.insertAdjacentHTML('afterbegin', errorHtml);
                    
                    // Scroll zum Anfang des Formulars
                    formContainer.scrollIntoView({ behavior: 'smooth', block: 'start' });
                }
            },
            // Notes formatting
            formatNote(note) {
                let createdById = this.entry.employeeId;
                if (note.createdById && note.createdById !== 0 && note.createdById !== null && note.createdById !== undefined) {
                    createdById = note.createdById;
                }
                return {
                    id: note.id,
                    projectId: note.projectId || this.entry.projectId,
                    timeEntryId: note.timeEntryId || this.entry.id || null,
                    documentId: note.documentId || null,
                    title: note.title,
                    content: note.content,
                    category: note.category,
                    tags: Array.isArray(note.tags) ? note.tags : (note.tags ? note.tags.split(',').map(t => t.trim()) : []),
                    attachments: note.attachments || [],
                    createdById: createdById,
                    createdAt: note.createdAt || null,
                    updatedAt: note.updatedAt || null
                };
            },
            // Save
            async saveEntry() {
                if (this.saving) return;
                
                // Validate required fields
                const validationErrors = [];
                
                if (!this.entry.employeeId) {
                    validationErrors.push("Mitarbeiter");
                }
                if (!this.entry.projectId) {
                    validationErrors.push("Projekt");
                }
                if (!this.entry.date) {
                    validationErrors.push("Datum");
                }
                if (!this.entry.title || this.entry.title.trim() === '') {
                    validationErrors.push("Titel");
                }
                if (!this.entry.startTime) {
                    validationErrors.push("Startzeit");
                }
                if (!this.entry.endTime) {
                    validationErrors.push("Endzeit");
                }
                
                if (validationErrors.length > 0) {
                    const errorMessage = `📝 **Pflichtfelder fehlen**: Bitte füllen Sie folgende Felder aus: ${validationErrors.join(', ')}`;
                    this.showError(errorMessage);
                    return;
                }
                // Ensure every note has createdById
                for (const note of this.notes) {
                    if (!note.createdById || note.createdById === 0 || note.createdById === null || note.createdById === undefined) {
                        note.createdById = this.entry.employeeId;
                    }
                }
                // Use auto-calculated hours if start/end set
                if (this.entry.startTime && this.entry.endTime) {
                    this.entry.hoursWorked = Number(this.autoHoursWorked);
                }
                this.saving = true;
                try {
                    // Convert break times to LocalTime format
                    const formattedBreaks = this.entry.breaks.map(breakItem => ({
                        start: breakItem.start + ":00", // Add seconds to make it LocalTime format
                        end: breakItem.end + ":00"
                    }));

                    const payload = {
                        id: this.entry.id,
                        employeeId: this.entry.employeeId,
                        projectId: this.entry.projectId,
                        date: this.entry.date,
                        startTime: this.entry.startTime,
                        endTime: this.entry.endTime,
                        breaks: formattedBreaks,
                        hoursWorked: this.entry.hoursWorked,
                        title: this.entry.title,
                        notes: this.notes.map(n => this.formatNote(n)),
                        hourlyRate: this.entry.hourlyRate,
                        billable:   this.entry.billable,
                        invoiced:   this.entry.invoiced,
                        catalogItems: this.entry.catalogItems,
                        hasNightSurcharge:   this.entry.hasNightSurcharge,
                        hasWeekendSurcharge: this.entry.hasWeekendSurcharge,
                        hasHolidaySurcharge: this.entry.hasHolidaySurcharge,
                        travelTimeMinutes:   this.entry.travelTimeMinutes,
                        disposalCost:        this.entry.disposalCost,
                        waitingTimeMinutes:  this.entry.waitingTimeMinutes
                    };

                    // Determine if this is a create or update operation
                    const isUpdate = this.entry.id && this.entry.id !== 0;
                    const method = isUpdate ? 'PUT' : 'POST';
                    const url = isUpdate ? `/timetracking/api/${this.entry.id}` : '/timetracking/api';

                    const saveRes = await fetch(url, {
                        method,
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(payload)
                    });
                    if (!saveRes.ok) {
                        throw new Error(await saveRes.text());
                    }
                    const saved = await saveRes.json();
                    for (let i = 0; i < this.notes.length; i++) {
                        const note = this.notes[i];
                        if (note.pendingFile) {
                            const form = new FormData();
                            form.append('noteId', saved.notes[i].id);
                            form.append('file', note.pendingFile);
                            const res = await fetch('/timetracking/api/upload/note-attachment', {
                                method: 'POST',
                                body: form
                            });
                            if (res.ok) {
                                const dto = await res.json();
                                note.attachments.push(dto);
                            }
                        }
                    }

                    // Close modal or update parent if in a modal
                    if (el.dataset.modal) {
                        // Trigger event to close modal and refresh calendar
                        const event = new CustomEvent('entry-saved', { 
                            detail: { 
                                saved, 
                                action: 'close-modal',
                                refresh: true 
                            } 
                        });
                        document.dispatchEvent(event);
                    } else {
                    window.location.href = '/timetracking';
                    }
                } catch (e) {
                    console.error(e);
                    this.showError(this.parseErrorMessage(e));
                    this.saving = false;
                }
            },
        },
        template: `
        <style>
        /* No CSS needed - using inline styles */
        </style>
        
        <div class="container-fluid">
            <!-- Tab Navigation -->
            <ul class="nav nav-tabs mb-4" role="tablist" style="border-bottom: 2px solid #e9ecef;">
                <li class="nav-item" v-for="(tab, i) in ['Erfassung', 'Optionen', 'Material', 'Notizen', 'Übersicht']" :key="i">
                    <button :class="getTabClass(i)" @click="setTab(i)" style="border: none; border-radius: 6px 6px 0 0; margin-right: 0.25rem; font-weight: 500; transition: all 0.2s ease;">
                        <span v-html="icon(getTabIcon(i))"></span>
                        {{ tab }}
                    </button>
                </li>
            </ul>

                    <!-- Erfassung (Tab 1) - Alle Pflichtfelder + Verrechenbar -->
                    <div v-show="activeTab === 0">
                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label class="form-label">Mitarbeiter <span class="text-danger">*</span></label>
                            <select v-model="entry.employeeId" class="form-select" required>
                                    <option value="">-- auswählen --</option>
                                <option v-for="emp in employees" :key="emp.id" :value="emp.id">
                                    {{ emp.firstName }} {{ emp.lastName }}
                                </option>
                            </select>
                        </div>
                            <div class="col-md-6">
                                <label class="form-label">Projekt <span class="text-danger">*</span></label>
                            <select v-model="entry.projectId" class="form-select" required>
                                    <option value="">-- auswählen --</option>
                                <option v-for="proj in projects" :key="proj.id" :value="proj.id">
                                    {{ proj.name }}
                                </option>
                            </select>
                        </div>
                        </div>
                        <div class="row g-3 mb-3">
                            <div class="col-md-4">
                                <label class="form-label">Datum <span class="text-danger">*</span></label>
                                <input v-model="entry.date" type="date" class="form-control" required>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Startzeit <span class="text-danger">*</span></label>
                                <input v-model="entry.startTime" type="time" class="form-control" required>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Endzeit <span class="text-danger">*</span></label>
                                <input v-model="entry.endTime" type="time" class="form-control" required>
                            </div>
                        </div>
                        <div class="row g-3 mb-3">
                            <div class="col-md-8">
                                <label class="form-label">Titel / Beschreibung <span class="text-danger">*</span></label>
                                <input v-model="entry.title" type="text" class="form-control" placeholder="Kurze Beschreibung der Arbeit" required>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Gearbeitete Stunden</label>
                                <input :value="autoHoursWorked" type="text" class="form-control bg-light" readonly>
                            </div>
                        </div>
                        <div class="row g-3 mb-3">
                            <div class="col-md-12">
                                <!-- NEW: Simple Large Toggle Switch -->
                                <div style="text-align: left; padding: 15px; background: #f0f0f0; border-radius: 10px; margin: 20px 0;">
                                    
                                    <!-- Large Toggle Button -->
                                    <button 
                                        type="button"
                                        @click="entry.billable = !entry.billable"
                                        :title="entry.billable ? 'Verrechenbar: EIN - Diese Zeit wird dem Kunden in Rechnung gestellt' : 'Verrechenbar: AUS - Diese Zeit wird dem Kunden nicht in Rechnung gestellt'"
                                        :style="{
                                            width: '150px',
                                            height: '80px',
                                            backgroundColor: entry.billable ? '#007bff' : '#ccc',
                                            border: 'none',
                                            borderRadius: '40px',
                                            position: 'relative',
                                            cursor: 'pointer',
                                            transition: 'background-color 0.3s ease',
                                            outline: 'none',
                                            boxShadow: '0 4px 8px rgba(0,0,0,0.2)'
                                        }">
                                        
                                        <!-- Toggle Circle -->
                                        <div :style="{
                                            position: 'absolute',
                                            top: '4px',
                                            left: entry.billable ? '66px' : '4px',
                                            width: '72px',
                                            height: '72px',
                                            backgroundColor: 'white',
                                            borderRadius: '50%',
                                            transition: 'left 0.3s ease',
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            boxShadow: '0 2px 4px rgba(0,0,0,0.2)',
                                            fontSize: '24px',
                                            fontWeight: 'bold',
                                            color: entry.billable ? '#007bff' : '#666'
                                        }">
                                            €
                                        </div>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Optionen (Tab 2) - Alle optionalen Felder -->
                    <div v-show="activeTab === 1">
                        <div class="row g-3 mb-3">
                            <div class="col-md-4">
                                <label class="form-label">Stundensatz (CHF)</label>
                                <input v-model.number="entry.hourlyRate" type="number" step="0.01" class="form-control">
                            </div>
                            <div class="col-md-8">
                                <label class="form-label">Zuschläge</label>
                                <div class="form-check form-check-inline">
                                <input v-model="entry.hasNightSurcharge" type="checkbox" class="form-check-input" id="hasNightSurcharge">
                                <label class="form-check-label" for="hasNightSurcharge">Nachtzuschlag</label>
                            </div>
                                <div class="form-check form-check-inline">
                                <input v-model="entry.hasWeekendSurcharge" type="checkbox" class="form-check-input" id="hasWeekendSurcharge">
                                <label class="form-check-label" for="hasWeekendSurcharge">Wochenendzuschlag</label>
                            </div>
                                <div class="form-check form-check-inline">
                                <input v-model="entry.hasHolidaySurcharge" type="checkbox" class="form-check-input" id="hasHolidaySurcharge">
                                <label class="form-check-label" for="hasHolidaySurcharge">Feiertagszuschlag</label>
                                </div>
                            </div>
                        </div>
                        <div class="row g-3 mb-3">
                            <div class="col-md-4">
                                <label class="form-label">Reisezeit (Minuten)</label>
                                <input v-model.number="entry.travelTimeMinutes" type="number" min="0" class="form-control">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Entsorgungskosten (CHF)</label>
                                <input v-model.number="entry.disposalCost" type="number" step="0.01" min="0" class="form-control">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Wartezeit (Minuten)</label>
                                <input v-model.number="entry.waitingTimeMinutes" type="number" min="0" class="form-control">
                            </div>
                        </div>
                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <div class="form-check">
                                    <input v-model="entry.invoiced" type="checkbox" class="form-check-input" id="invoiced">
                                    <label class="form-check-label" for="invoiced">Fakturiert</label>
                                </div>
                            </div>
                        </div>
                        
                        <!-- Pausen -->
                        <div class="mb-3">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <h6 class="mb-0">Pausen</h6>
                                <button class="btn btn-outline-secondary btn-sm" @click="showBreaks = !showBreaks">
                                    {{ showBreaks ? 'Verbergen' : 'Anzeigen' }}
                                </button>
                            </div>
                            <div v-show="showBreaks">
                                <div class="card card-body bg-light mb-2">
                                    <div class="row g-2 align-items-end">
                                        <div class="col-auto">
                                            <label class="form-label">Pause von</label>
                                            <input v-model="breakStart" type="time" class="form-control">
                                        </div>
                                        <div class="col-auto">
                                            <label class="form-label">bis</label>
                                            <input v-model="breakEnd" type="time" class="form-control">
                                        </div>
                                        <div class="col-auto">
                                            <button class="btn btn-success" @click="addBreak" v-html="icon('plus-circle') + ' Hinzufügen'"></button>
                                        </div>
                                    </div>
                                    <div v-if="entry.breaks.length > 0" class="mt-2">
                                        <ul class="list-group">
                                            <li v-for="(br, i) in entry.breaks" :key="i" class="list-group-item d-flex justify-content-between align-items-center">
                                                {{ br.start }} - {{ br.end }}
                                                <button class="btn btn-danger btn-sm" @click="removeBreak(i)" v-html="icon('trash')"></button>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Material -->
                    <div v-show="activeTab === 2">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <h6 class="mb-0">Katalogartikel</h6>
                            <button class="btn btn-outline-secondary btn-sm" @click="showMaterials = !showMaterials">
                                {{ showMaterials ? 'Verbergen' : 'Anzeigen' }}
                            </button>
                        </div>
                        <div v-show="showMaterials">
                            <table class="table table-bordered table-sm" id="catalogItemsTable">
                                <thead class="table-light">
                                    <tr>
                                        <th>Name</th>
                                        <th>Menge</th>
                                        <th>Einzelpreis</th>
                                        <th>Gesamt</th>
                                        <th>Aktionen</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr v-for="(item, index) in entry.catalogItems" :key="index">
                                        <td>{{ item.itemName }}</td>
                                        <td>{{ item.quantity }}</td>
                                        <td>{{ item.unitPrice }} CHF</td>
                                        <td>{{ item.totalPrice }} CHF</td>
                                        <td>
                                            <button type="button" class="btn btn-danger btn-sm" @click="removeCatalogItem(index)" v-html="icon('trash')"></button>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                            <div class="row g-3 align-items-end mt-2">
                                <div class="col">
                                    <select v-model="selectedCatalogItem" class="form-select">
                                        <option value="">-- Artikel auswählen --</option>
                                        <option v-for="item in catalogItems" :key="item.id" :value="item.id">
                                            {{ item.name }} – {{ item.unitPrice }} CHF
                                        </option>
                                    </select>
                                </div>
                                <div class="col-auto">
                                    <input v-model.number="itemQuantity" type="number" class="form-control" min="1" value="1">
                                </div>
                                <div class="col-auto">
                                    <button type="button" class="btn btn-primary" @click="addCatalogItem" v-html="icon('plus-circle') + ' Hinzufügen'"></button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Notizen -->
                    <div v-show="activeTab === 3">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <h6 class="mb-0">Notizen</h6>
                            <button class="btn btn-outline-secondary btn-sm" @click="showNotes = !showNotes">
                                {{ showNotes ? 'Verbergen' : 'Anzeigen' }}
                            </button>
                        </div>
                        <div v-show="showNotes">
                            <div v-for="(note, index) in notes" :key="index" class="note-block mb-3 border rounded p-2 bg-light">
                                <div class="row g-2">
                                    <div class="col-md-4">
                                        <label class="form-label">Titel</label>
                                        <input v-model="note.title" type="text" class="form-control" placeholder="Optional">
                                    </div>
                                    <div class="col-md-4">
                                        <label class="form-label">Kategorie</label>
                                        <select v-model="note.category" class="form-select" required>
                                            <option value="">-- auswählen --</option>
                                            <option v-for="cat in categories" :key="cat" :value="cat">
                                                {{ cat }}
                                            </option>
                                        </select>
                                    </div>
                                    <div class="col-md-4">
                                        <label class="form-label">Tags (Komma-getrennt)</label>
                                        <input 
                                            :value="Array.isArray(note.tags) ? note.tags.join(', ') : note.tags" 
                                            @input="note.tags = $event.target.value.split(',').map(t => t.trim()).filter(t => t.length > 0)"
                                            type="text" 
                                            class="form-control" 
                                            placeholder="z. B. dringlich, Prüfung"
                                        />
                                    </div>
                                </div>
                                <div class="row g-2 mt-2">
                                    <div class="col-md-10">
                                        <label class="form-label">Inhalt</label>
                                        <textarea v-model="note.content" class="form-control" rows="2" required></textarea>
                                    </div>
                                    <div class="col-md-2">
                                        <label class="form-label">Anhänge</label>
                                        <input type="file" @change="onFilePicked(index, $event)" class="form-control" />
                                        <div v-if="note.attachments.length > 0" class="mt-1">
                                            <div v-for="(att, attIndex) in note.attachments" :key="att.id" class="d-flex align-items-center mb-1">
                                                <a :href="att.url" target="_blank" class="me-2">{{ att.caption }}</a>
                                                <button type="button" class="btn btn-sm btn-outline-danger" @click="removeAttachment(index, attIndex)" v-html="icon('x')"></button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="mt-2 text-end">
                                    <button type="button" class="btn btn-danger btn-sm" @click="removeNote(index)" v-html="icon('trash') + ' Entfernen'"></button>
                                </div>
                            </div>
                            <button type="button" class="btn btn-outline-primary btn-sm" @click="addNote" v-html="icon('plus-circle') + ' Notiz hinzufügen'"></button>
                            </div>
                        </div>

                    <!-- Übersicht -->
                    <div v-show="activeTab === 4">
                        <div class="card card-body bg-light mb-3">
                            <h6>Zusammenfassung</h6>
                            <ul class="list-group">
                                <li class="list-group-item"><b>Mitarbeiter:</b> {{ employees.find(e => e.id == entry.employeeId)?.firstName }} {{ employees.find(e => e.id == entry.employeeId)?.lastName }}</li>
                                <li class="list-group-item"><b>Projekt:</b> {{ projects.find(p => p.id == entry.projectId)?.name }}</li>
                                <li class="list-group-item"><b>Datum:</b> {{ entry.date }}</li>
                                <li class="list-group-item"><b>Startzeit:</b> {{ entry.startTime }} <b>Endzeit:</b> {{ entry.endTime }}</li>
                                <li class="list-group-item"><b>Gearbeitete Stunden:</b> {{ autoHoursWorked }}</li>
                                <li class="list-group-item"><b>Pausen:</b> {{ entry.breaks.map(b => b.start + '-' + b.end).join(', ') }}</li>
                                <li class="list-group-item"><b>Materialien:</b> {{ entry.catalogItems.length }}</li>
                                <li class="list-group-item"><b>Notizen:</b> {{ notes.length }}</li>
                            </ul>
                        </div>
                    </div>
                </div>
                <!-- Clean Footer with Action Buttons Only -->
                <div :style="stickyBarStyle">
                    <div class="container d-flex justify-content-end align-items-center" style="padding: 1rem 0;">
                        <button type="button" class="btn btn-outline-secondary me-3" @click="window.location.href='/timetracking'" style="border-radius: 6px; padding: 0.5rem 1.25rem; font-weight: 500;">
                            <i class="bi bi-x-circle me-1"></i>
                            Abbrechen
                        </button>
                        <button type="button" class="btn btn-success" :disabled="saving" @click="saveEntry" style="border-radius: 6px; padding: 0.5rem 1.5rem; font-weight: 500; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                            <i class="bi bi-check-circle me-1"></i>
                            {{ saving ? 'Speichern...' : 'Speichern' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `
    });
    
    // Store the app globally so it can be unmounted
    try {
        // Ensure the element is still in the DOM before mounting
        if (!document.contains(el)) {
            console.error('Form element no longer in DOM, cannot mount Vue app');
            return;
        }
        
        window.timeTrackingFormApp = app.mount(el);
        // Mark the element as mounted
        el._vue_app = true;
        console.log('Time tracking form Vue app mounted successfully');
        // Reset retry counter on successful initialization
        window.formInitRetryCount = 0;
    } catch (error) {
        console.error('Failed to mount time tracking form Vue app:', error);
        // If mounting fails, try again after a short delay
        if (window.formInitRetryCount < 10) {
            console.log('Retrying Vue app mount...');
            setTimeout(() => {
                initializeForm();
            }, 100);
        }
    }
}

// Make the function globally available
window.initializeTimeTrackingForm = initializeForm;

// Call the function to initialize the form
initializeForm();