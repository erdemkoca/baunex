import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('time-tracking-list-app');
    const timeEntries = JSON.parse(el.dataset.timeEntries || '[]');

    createApp({
        data() {
            return {
                timeEntries,
                selectedStatus: 'ALL',
                statuses: {
                    ALL: { label: 'Alle', color: 'secondary', icon: 'bi-list-ul' },
                    PENDING: { label: 'Ausstehend', color: 'warning', icon: 'bi-clock' },
                    APPROVED: { label: 'Genehmigt', color: 'success', icon: 'bi-check-circle' }
                }
            };
        },
        computed: {
            filteredEntries() {
                if (this.selectedStatus === 'ALL') return this.timeEntries;
                return this.timeEntries.filter(entry =>
                    this.selectedStatus === 'APPROVED'
                        ? entry.approval.approved
                        : !entry.approval.approved
                );
            }
        },
        methods: {
            navigateToEdit(id) {
                window.location.href = `/timetracking/${id}`;
            },
            async deleteEntry(id) {
                if (!confirm('Eintrag wirklich löschen?')) return;
                const res = await fetch(`/timetracking/api/${id}`, { method: 'DELETE' });
                if (res.ok) window.location.reload();
                else alert('Fehler beim Löschen des Eintrags');
            },
            async approveEntry(id) {
                const res = await fetch(`/timetracking/api/${id}/approve`, { method: 'POST' });
                if (res.ok) window.location.reload();
                else alert('Fehler beim Genehmigen des Eintrags');
            },
            formatDate(date) {
                return new Date(date).toLocaleDateString('de-CH');
            }
        },
        template: `
        <div class="container-fluid">
            <div class="mb-3 d-flex justify-content-between align-items-center">
                <a href="/timetracking/0" class="btn btn-primary">
                    <i class="bi bi-plus-circle me-2"></i>Zeit erfassen
                </a>
            </div>

            <!-- Status Filter -->
            <div class="mb-3">
                <div class="btn-group" role="group">
                    <button v-for="(status, key) in statuses" 
                            :key="key"
                            type="button" 
                            :class="['btn', 'btn-outline-' + status.color, { active: selectedStatus === key }]"
                            @click="selectedStatus = key">
                        <i :class="['bi', status.icon, 'me-1']"></i>
                        {{ status.label }}
                    </button>
                </div>
            </div>

            <div class="card">
                <div class="card-header">Alle Zeiterfassungen</div>
                <div class="card-body">
                    <div v-if="filteredEntries.length === 0" class="text-muted">
                        Keine Zeiterfassungen gefunden.
                    </div>
                    <div v-else class="accordion" id="timeEntriesAccordion">
                        <div v-for="entry in filteredEntries" :key="entry.id" class="accordion-item mb-3">
                            <h2 class="accordion-header" :id="'heading' + entry.id">
                                <button class="accordion-button collapsed" type="button"
                                        data-bs-toggle="collapse"
                                        :data-bs-target="'#entry' + entry.id"
                                        aria-expanded="false"
                                        :aria-controls="'entry' + entry.id">
                                    <div class="d-flex flex-column w-100">
                                        <div class="d-flex justify-content-between w-100">
                                            <div>
                                                <strong>{{ entry.employeeFirstName }} {{ entry.employeeLastName }}</strong> – {{ entry.projectName }} - {{ formatDate(entry.date) }}
                                                <span class="badge bg-primary ms-2">{{ entry.hoursWorked }} Stunden</span>
                                                <span v-if="!entry.billable" class="badge bg-secondary ms-1">Nicht verrechenbar</span>
                                            </div>
                                            <div>
                                                <span v-if="entry.approval.approved" class="badge bg-success me-2">
                                                    <i class="bi bi-check-circle me-1"></i>Genehmigt von {{ entry.approval.approverName }}
                                                </span>
                                                <span v-else class="badge bg-warning me-2">
                                                    <i class="bi bi-clock me-1"></i>Ausstehend
                                                </span>
                                                <span class="badge bg-info">{{ entry.cost }} CHF</span>
                                            </div>
                                        </div>
                                        <div class="mt-1 ps-3">
                                            <em>Titel:</em> {{ entry.title }}
                                        </div>
                                    </div>
                                </button>
                            </h2>
                            <div :id="'entry' + entry.id" class="accordion-collapse collapse"
                                 data-bs-parent="#timeEntriesAccordion">
                                <div class="accordion-body">
                                    <!-- Grundinformationen -->
                                    <div class="row mb-3">
                                        <div class="col-md-6">
                                            <h6>Grundinformationen</h6>
                                            <p><strong>Projekt:</strong> {{ entry.projectName }}</p>
                                            
                                            <!-- Notizen-Liste -->
                                            <h6 class="mt-3">Notizen</h6>
                                            <div v-if="entry.notes.length === 0" class="text-muted">
                                                Keine Notizen vorhanden.
                                            </div>
                                            <div v-else class="notes-list">
                                                <div v-for="note in entry.notes" :key="note.id" class="card mb-2">
                                                    <div class="card-header small text-muted">
                                                        {{ note.createdAt }} – {{ note.createdByName }}
                                                        <span class="badge bg-secondary ms-2">{{ note.category }}</span>
                                                    </div>
                                                    <div class="card-body py-2">
                                                        <h6 v-if="note.title" class="card-title mb-1">{{ note.title }}</h6>
                                                        <p class="card-text">{{ note.content }}</p>
                                                        <p v-if="note.tags.length > 0" class="mb-0">
                                                            <strong>Tags:</strong>
                                                            <span v-for="tag in note.tags" :key="tag" class="badge bg-secondary me-1">{{ tag }}</span>
                                                        </p>
                                                        <p v-if="note.attachments.length > 0" class="mb-0 mt-1">
                                                            <strong>Anhänge:</strong>
                                                            <a v-for="(att, index) in note.attachments" 
                                                               :key="att.id"
                                                               :href="att.url" 
                                                               target="_blank">
                                                                {{ att.caption || "Datei" }}{{ index < note.attachments.length - 1 ? ', ' : '' }}
                                                            </a>
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-6">
                                            <h6>Status</h6>
                                            <p>
                                                <span :class="['badge', entry.invoiced ? 'bg-success' : 'bg-warning']">
                                                    {{ entry.invoiced ? 'Fakturiert' : 'Nicht fakturiert' }}
                                                </span>
                                            </p>
                                            <button v-if="!entry.approval.approved"
                                                    @click="approveEntry(entry.id)"
                                                    class="btn btn-success btn-sm mt-2">
                                                <i class="bi bi-check-circle me-1"></i>Genehmigen
                                            </button>
                                        </div>
                                    </div>

                                    <!-- Zuschläge -->
                                    <div v-if="entry.hasNightSurcharge || entry.hasWeekendSurcharge || entry.hasHolidaySurcharge"
                                         class="row mb-3">
                                        <div class="col-12">
                                            <h6>Zuschläge</h6>
                                            <div class="d-flex gap-2">
                                                <span v-if="entry.hasNightSurcharge" class="badge bg-dark">Nachtzuschlag</span>
                                                <span v-if="entry.hasWeekendSurcharge" class="badge bg-dark">Wochenendzuschlag</span>
                                                <span v-if="entry.hasHolidaySurcharge" class="badge bg-dark">Feiertagszuschlag</span>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Zusätzliche Kosten -->
                                    <div v-if="entry.travelTimeMinutes > 0 || entry.disposalCost > 0 || entry.hasWaitingTime"
                                         class="row mb-3">
                                        <div class="col-12">
                                            <h6>Zusätzliche Kosten</h6>
                                            <table class="table table-sm">
                                                <tbody>
                                                    <tr v-if="entry.travelTimeMinutes > 0">
                                                        <td>Reisezeit</td>
                                                        <td class="text-end">{{ entry.travelTimeMinutes }} min</td>
                                                    </tr>
                                                    <tr v-if="entry.hasWaitingTime">
                                                        <td>Wartezeit</td>
                                                        <td class="text-end">{{ entry.waitingTimeMinutes }} min</td>
                                                    </tr>
                                                    <tr v-if="entry.disposalCost > 0">
                                                        <td>Entsorgungskosten</td>
                                                        <td class="text-end">{{ entry.disposalCost }} CHF</td>
                                                    </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>

                                    <!-- Katalogartikel -->
                                    <div v-if="entry.catalogItems.length > 0" class="row mb-3">
                                        <div class="col-12">
                                            <h6>Verwendete Artikel</h6>
                                            <table class="table table-sm">
                                                <thead>
                                                    <tr>
                                                        <th>Artikel</th>
                                                        <th>Menge</th>
                                                        <th>Einzelpreis</th>
                                                        <th>Gesamt</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <tr v-for="item in entry.catalogItems" :key="item.id">
                                                        <td>{{ item.itemName }}</td>
                                                        <td>{{ item.quantity }}</td>
                                                        <td>{{ item.unitPrice }} CHF</td>
                                                        <td>{{ item.totalPrice }} CHF</td>
                                                    </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>

                                    <!-- Aktionen -->
                                    <div class="d-flex gap-2 mt-3">
                                        <button @click="navigateToEdit(entry.id)"
                                                class="btn btn-outline-primary btn-sm">
                                            <i class="bi bi-pencil me-1"></i>Bearbeiten
                                        </button>
                                        <button @click="deleteEntry(entry.id)"
                                                class="btn btn-outline-danger btn-sm">
                                            <i class="bi bi-trash me-1"></i>Löschen
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `
    }).mount(el);
});