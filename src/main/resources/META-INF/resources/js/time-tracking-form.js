import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('time-tracking-form-app');
    const entry = JSON.parse(el.dataset.entry || '{}');
    const employees = JSON.parse(el.dataset.employees || '[]');
    const projects = JSON.parse(el.dataset.projects || '[]');
    const categories = JSON.parse(el.dataset.categories || '[]');
    const catalogItems = JSON.parse(el.dataset.catalogItems || '[]');
    const currentDate = el.dataset.currentDate;

    createApp({
        data() {
            return {
                entry,
                employees,
                projects,
                categories,
                catalogItems,
                currentDate,
                notes: entry.notes || [],
                selectedCatalogItem: null,
                itemQuantity: 1,
                saving: false
            };
        },
        methods: {
            addNote() {
                this.notes.push({
                    id: null,
                    title: '',
                    category: '',
                    content: '',
                    tags: [],
                    attachments: [],
                    pendingFile: null
                });
            },
            removeNote(i) {
                this.notes.splice(i, 1);
            },
            // <-- this will now actually be called
            onFilePicked(noteIndex, event) {
                this.notes[noteIndex].pendingFile = event.target.files[0];
            },
            removeAttachment(i, ai) {
                this.notes[i].attachments.splice(ai, 1);
            },
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
            removeCatalogItem(i) { this.entry.catalogItems.splice(i, 1); },

            async saveEntry() {
                this.saving = true;
                try {
                    // 1) Persist entry + notes (no attachments yet)
                    const saveRes = await fetch('/timetracking/api/save', {
                        method: 'POST',
                        headers: { 'Content-Type':'application/json' },
                        body: JSON.stringify({
                            id: this.entry.id,
                            employeeId: this.entry.employeeId,
                            projectId: this.entry.projectId,
                            date: this.entry.date,
                            hoursWorked: this.entry.hoursWorked,
                            title: this.entry.title,
                            notes: this.notes.map(n => ({
                                id: n.id,
                                title: n.title,
                                category: n.category,
                                content: n.content,
                                tags: n.tags,
                                attachments: []      // attachments handled next
                            })),
                            hourlyRate: this.entry.hourlyRate,
                            billable: this.entry.billable,
                            invoiced: this.entry.invoiced,
                            catalogItems: this.entry.catalogItems,
                            hasNightSurcharge: this.entry.hasNightSurcharge,
                            hasWeekendSurcharge: this.entry.hasWeekendSurcharge,
                            hasHolidaySurcharge: this.entry.hasHolidaySurcharge,
                            travelTimeMinutes: this.entry.travelTimeMinutes,
                            disposalCost: this.entry.disposalCost,
                            hasWaitingTime: this.entry.hasWaitingTime,
                            waitingTimeMinutes: this.entry.waitingTimeMinutes
                        })
                    });
                    if (!saveRes.ok) {
                        throw new Error(await saveRes.text());
                    }
                    const saved = await saveRes.json();

                    // 2) Now upload each note’s pending file, if any, using real note IDs
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

                    window.location.href = '/timetracking';
                } catch (e) {
                    console.error(e);
                    alert('Fehler beim Speichern: ' + e.message);
                    this.saving = false;
                }
            }
        },
        template: `
        <div class="container-fluid">
            <div class="card">
                <div class="card-header">
                    {{ entry.id ? 'Eintrag bearbeiten' : 'Neuer Eintrag' }}
                </div>
                <div class="card-body">
                    <form @submit.prevent="saveEntry">
                        <!-- Titel -->
                        <div class="mb-3">
                            <label class="form-label">Titel (optional)</label>
                            <input v-model="entry.title" type="text" class="form-control" placeholder="Kurze Beschreibung">
                        </div>

                        <!-- Mitarbeiter -->
                        <div class="mb-3">
                            <label class="form-label">Mitarbeiter</label>
                            <select v-model="entry.employeeId" class="form-select" required>
                                <option v-for="emp in employees" :key="emp.id" :value="emp.id">
                                    {{ emp.firstName }} {{ emp.lastName }}
                                </option>
                            </select>
                        </div>

                        <!-- Projekt -->
                        <div class="mb-3">
                            <label class="form-label">Projekt</label>
                            <select v-model="entry.projectId" class="form-select" required>
                                <option v-for="proj in projects" :key="proj.id" :value="proj.id">
                                    {{ proj.name }}
                                </option>
                            </select>
                        </div>

                        <!-- Datum -->
                        <div class="mb-3">
                            <label class="form-label">Datum</label>
                            <input v-model="entry.date" type="date" class="form-control" required>
                        </div>

                        <!-- Gearbeitete Stunden -->
                        <div class="mb-3">
                            <label class="form-label">Gearbeitete Stunden</label>
                            <input v-model.number="entry.hoursWorked" type="number" step="0.1" min="0" class="form-control" required>
                        </div>

                        <!-- Notizen -->
                        <fieldset class="border rounded p-3 mb-3">
                            <legend class="float-none w-auto px-2">Notizen</legend>
                            <div v-for="(note, index) in notes" :key="index" class="note-block mb-3 border rounded p-2">
                                <!-- Titel -->
                                <div class="mb-2">
                                    <label class="form-label">Titel</label>
                                    <input v-model="note.title" type="text" class="form-control" placeholder="Optional">
                                </div>

                                <!-- Kategorie -->
                                <div class="mb-2">
                                    <label class="form-label">Kategorie</label>
                                    <select v-model="note.category" class="form-select" required>
                                        <option value="">-- auswählen --</option>
                                        <option v-for="cat in categories" :key="cat" :value="cat">
                                            {{ cat }}
                                        </option>
                                    </select>
                                </div>

                                <!-- Inhalt -->
                                <div class="mb-2">
                                    <label class="form-label">Inhalt</label>
                                    <textarea v-model="note.content" class="form-control" rows="2" required></textarea>
                                </div>

                                <!-- Tags -->
                                <div class="mb-2">
                                    <label class="form-label">Tags (Komma-getrennt)</label>
                                    <input v-model="note.tags" type="text" class="form-control" placeholder="z. B. dringlich, Prüfung">
                                </div>

                                <!-- Anhänge -->
                                <div class="mb-2">
                                    <label class="form-label">Anhänge</label>
                                    <div v-if="note.attachments.length > 0" class="mb-2">
                                        <div v-for="(att, attIndex) in note.attachments" :key="att.id" class="d-flex align-items-center mb-1">
                                            <a :href="att.url" target="_blank" class="me-2">{{ att.caption }}</a>
                                            <button type="button" class="btn btn-sm btn-outline-danger" @click="removeAttachment(index, attIndex)">
                                                <i class="bi bi-x"></i>
                                            </button>
                                        </div>
                                    </div>
                                    <input 
                                      type="file" 
                                      @change="onFilePicked(index, $event)" 
                                      class="form-control" 
                                    />
                                </div>

                                <button type="button" class="btn btn-danger btn-sm mt-2" @click="removeNote(index)">
                                    Entfernen
                                </button>
                            </div>

                            <button type="button" class="btn btn-outline-primary btn-sm" @click="addNote">
                                <i class="bi bi-plus-circle me-1"></i>Notiz hinzufügen
                            </button>
                        </fieldset>

                        <!-- Stundensatz -->
                        <div class="mb-3">
                            <label class="form-label">Stundensatz (CHF)</label>
                            <input v-model.number="entry.hourlyRate" type="number" step="0.01" class="form-control">
                        </div>

                        <!-- Zuschläge -->
                        <div class="mb-3">
                            <label class="form-label">Zuschläge</label>
                            <div class="form-check">
                                <input v-model="entry.hasNightSurcharge" type="checkbox" class="form-check-input" id="hasNightSurcharge">
                                <label class="form-check-label" for="hasNightSurcharge">Nachtzuschlag</label>
                            </div>
                            <div class="form-check">
                                <input v-model="entry.hasWeekendSurcharge" type="checkbox" class="form-check-input" id="hasWeekendSurcharge">
                                <label class="form-check-label" for="hasWeekendSurcharge">Wochenendzuschlag</label>
                            </div>
                            <div class="form-check">
                                <input v-model="entry.hasHolidaySurcharge" type="checkbox" class="form-check-input" id="hasHolidaySurcharge">
                                <label class="form-check-label" for="hasHolidaySurcharge">Feiertagszuschlag</label>
                            </div>
                        </div>

                        <!-- Zusätzliche Kosten -->
                        <div class="mb-3">
                            <label class="form-label">Zusätzliche Kosten</label>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Reisezeit (Minuten)</label>
                                    <input v-model.number="entry.travelTimeMinutes" type="number" min="0" class="form-control">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Entsorgungskosten (CHF)</label>
                                    <input v-model.number="entry.disposalCost" type="number" step="0.01" min="0" class="form-control">
                                </div>
                            </div>
                            <div class="row g-3 mt-2">
                                <div class="col-md-6">
                                    <div class="form-check">
                                        <input v-model="entry.hasWaitingTime" type="checkbox" class="form-check-input" id="hasWaitingTime">
                                        <label class="form-check-label" for="hasWaitingTime">Wartezeit</label>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Wartezeit (Minuten)</label>
                                    <input v-model.number="entry.waitingTimeMinutes" type="number" min="0" class="form-control">
                                </div>
                            </div>
                        </div>

                        <!-- Nicht verrechenbar & Fakturiert -->
                        <div class="mb-3 form-check">
                            <input v-model="entry.notBillable" type="checkbox" class="form-check-input" id="notBillable">
                            <label class="form-check-label" for="notBillable">Nicht verrechenbar</label>
                        </div>
                        <div class="mb-3 form-check">
                            <input v-model="entry.invoiced" type="checkbox" class="form-check-input" id="invoiced">
                            <label class="form-check-label" for="invoiced">Fakturiert</label>
                        </div>

                        <!-- Katalogartikel -->
                        <div class="mb-3">
                            <label class="form-label">Katalogartikel</label>
                            <table class="table" id="catalogItemsTable">
                                <thead>
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
                                            <button type="button" class="btn btn-danger btn-sm" @click="removeCatalogItem(index)">x</button>
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
                                    <button type="button" class="btn btn-primary" @click="addCatalogItem">
                                        <i class="bi bi-plus-circle me-1"></i>Hinzufügen
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- Speichern/Abbrechen -->
                        <div class="mt-3">
                            <button type="submit" class="btn btn-primary" :disabled="saving">
                                {{ saving ? 'Speichern...' : 'Speichern' }}
                            </button>
                            <a href="/timetracking" class="btn btn-outline-secondary">Abbrechen</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    `
    }).mount(el);
});