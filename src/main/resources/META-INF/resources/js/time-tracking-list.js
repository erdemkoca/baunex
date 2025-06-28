import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('time-tracking-list-app');
    const timeEntries = JSON.parse(el.dataset.timeEntries || '[]');
    const holidays = JSON.parse(el.dataset.holidays || '[]');
    const employees = JSON.parse(el.dataset.employees || '[]');

    createApp({
        data() {
            return {
                timeEntries,
                holidays,
                employees,
                selectedEmployeeId: employees[0]?.id || null,
                selectedStatus: 'ALL',
                viewMode: 'list', // 'list', 'calendar', 'summary'
                currentWeek: this.getCurrentWeek(),
                currentYear: 2025, // Default to 2025 where we have sample data
                dailySummaries: [],
                weeklySummaries: [],
                loading: false,
                statuses: {
                    ALL: { label: 'Alle', color: 'secondary', icon: 'bi-list-ul' },
                    PENDING: { label: 'Ausstehend', color: 'warning', icon: 'bi-clock' },
                    APPROVED: { label: 'Genehmigt', color: 'success', icon: 'bi-check-circle' }
                }
            };
        },
        computed: {
            filteredEntries() {
                let filtered = this.timeEntries.filter(e => e.employeeId === this.selectedEmployeeId);
                if (this.selectedStatus === 'APPROVED') return filtered.filter(e => e.approval.approved);
                if (this.selectedStatus === 'PENDING') return filtered.filter(e => !e.approval.approved);
                return filtered;
            },
            filteredHolidays() {
                return this.holidays.filter(h => h.employeeId === this.selectedEmployeeId);
            },
            calendarDays() {
                const weekStart = this.getWeekStartDate(this.currentYear, this.currentWeek);
                const days = [];
                
                console.log('=== CALENDAR DEBUG ===');
                console.log('Week start:', weekStart);
                console.log('Daily summaries from backend:', this.dailySummaries);
                
                // Add all 7 days of the week
                for (let i = 0; i < 7; i++) {
                    const date = new Date(weekStart);
                    date.setDate(weekStart.getDate() + i);
                    const dateStr = this.toLocalDateString(date);
                    const summary = this.dailySummaries.find(s => s.date === dateStr);
                    
                    console.log(`Day ${i}: ${dateStr} - Found summary:`, summary);
                    
                    days.push({
                        date: date,
                        dateStr: dateStr,
                        day: date.getDate(),
                        dayName: this.getDayName(date.getDay()),
                        summary: summary
                    });
                }
                
                console.log('Final calendar days:', days);
                console.log('=== END CALENDAR DEBUG ===');
                
                return days;
            },
            weekDisplay() {
                const weekStart = this.getWeekStartDate(this.currentYear, this.currentWeek);
                const weekEnd = new Date(weekStart);
                weekEnd.setDate(weekStart.getDate() + 6);
                
                return {
                    weekNumber: this.currentWeek,
                    startDate: weekStart.toLocaleDateString('de-CH'),
                    endDate: weekEnd.toLocaleDateString('de-CH'),
                    fullRange: `${weekStart.toLocaleDateString('de-CH')} - ${weekEnd.toLocaleDateString('de-CH')}`
                };
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
            },
            getCurrentWeek() {
                // For testing, default to week 25 of 2025 where we have sample data
                return 25;
            },
            getWeekStartDate(year, week) {
                // ISO-8601: Woche 1 ist die mit dem ersten Donnerstag des Jahres
                const simple = new Date(year, 0, 1 + (week - 1) * 7);
                const dow = simple.getDay();
                const ISOweekStart = new Date(simple);
                if (dow <= 4)
                    ISOweekStart.setDate(simple.getDate() - simple.getDay() + 1);
                else
                    ISOweekStart.setDate(simple.getDate() + 8 - simple.getDay());
                return ISOweekStart;
            },
            getDayName(dayIndex) {
                const days = ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'];
                return days[dayIndex];
            },
            async loadDailySummaries() {
                if (!this.selectedEmployeeId) return;
                
                this.loading = true;
                try {
                    const weekStart = this.getWeekStartDate(this.currentYear, this.currentWeek);
                    const weekEnd = new Date(weekStart);
                    weekEnd.setDate(weekStart.getDate() + 6);
                    
                    const from = this.toLocalDateString(weekStart);
                    const to = this.toLocalDateString(weekEnd);
                    
                    console.log('=== LOAD DAILY SUMMARIES DEBUG ===');
                    console.log('Week start:', weekStart);
                    console.log('Week end:', weekEnd);
                    console.log('From:', from);
                    console.log('To:', to);
                    
                    const res = await fetch(`/timetracking/api/summary/daily?employeeId=${this.selectedEmployeeId}&from=${from}&to=${to}`);
                    if (res.ok) {
                        this.dailySummaries = await res.json();
                        console.log('Backend response:', this.dailySummaries);
                    }
                    console.log('=== END LOAD DAILY SUMMARIES DEBUG ===');
                } catch (error) {
                    console.error('Error loading daily summaries:', error);
                } finally {
                    this.loading = false;
                }
            },
            async loadWeeklySummaries() {
                if (!this.selectedEmployeeId) return;
                
                this.loading = true;
                try {
                    const res = await fetch(`/timetracking/api/summary/weekly?employeeId=${this.selectedEmployeeId}&year=${this.currentYear}&week=${this.currentWeek}`);
                    if (res.ok) {
                        this.weeklySummaries = await res.json();
                    }
                } catch (error) {
                    console.error('Error loading weekly summaries:', error);
                } finally {
                    this.loading = false;
                }
            },
            previousWeek() {
                if (this.currentWeek === 1) {
                    this.currentWeek = 52;
                    this.currentYear--;
                } else {
                    this.currentWeek--;
                }
                this.onWeekChange();
            },
            nextWeek() {
                if (this.currentWeek === 52) {
                    this.currentWeek = 1;
                    this.currentYear++;
                } else {
                    this.currentWeek++;
                }
                this.onWeekChange();
            },
            onWeekChange() {
                if (this.viewMode === 'calendar') {
                    this.loadDailySummaries();
                } else if (this.viewMode === 'summary') {
                    this.loadWeeklySummaries();
                }
            },
            getDayClass(day) {
                if (!day || !day.summary) return '';
                
                const summary = day.summary;
                
                if (summary.isWeekend) return 'calendar-weekend';
                if (summary.holidayType) return 'calendar-holiday';
                if (summary.workedHours === 0 && summary.expectedHours > 0) return 'calendar-missing';
                if (summary.delta > 0) return 'calendar-overtime';
                if (summary.delta < 0) return 'calendar-undertime';
                if (summary.delta === 0) return 'calendar-perfect';
                
                return '';
            },
            getDayTooltip(day) {
                if (!day || !day.summary) return '';
                
                const summary = day.summary;
                let tooltip = `${this.formatDate(day.date)} (${day.dayName})\n`;
                tooltip += `Gearbeitet: ${summary.workedHours}h\n`;
                tooltip += `Erwartet: ${summary.expectedHours}h\n`;
                
                if (summary.delta !== 0) {
                    tooltip += `Differenz: ${summary.delta > 0 ? '+' : ''}${summary.delta.toFixed(1)}h\n`;
                }
                
                if (summary.holidayType) {
                    tooltip += `Urlaub: ${summary.holidayType}\n`;
                    if (summary.holidayReason) tooltip += `Grund: ${summary.holidayReason}\n`;
                }
                
                if (summary.timeEntries.length > 0) {
                    tooltip += `\nEinträge:\n`;
                    summary.timeEntries.forEach(entry => {
                        tooltip += `• ${entry.title} (${entry.hoursWorked}h)\n`;
                    });
                }
                
                return tooltip;
            },
            onEmployeeChange() {
                this.onWeekChange();
            },
            toLocalDateString(date) {
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const day = String(date.getDate()).padStart(2, '0');
                return `${year}-${month}-${day}`;
            }
        },
        watch: {
            selectedEmployeeId() {
                this.onEmployeeChange();
            },
            viewMode() {
                this.onEmployeeChange();
            }
        },
        mounted() {
            this.loadDailySummaries();
        },
        template: `
        <div class="container-fluid">
            <div class="mb-3 d-flex justify-content-between align-items-center">
                <a href="/timetracking/0" class="btn btn-primary">
                    <i class="bi bi-plus-circle me-2"></i>Zeit erfassen
                </a>

                <div class="d-flex gap-2 align-items-center">
                    <select v-model="selectedEmployeeId" class="form-select w-auto" @change="onEmployeeChange">
                        <option v-for="emp in employees" :value="emp.id" :key="emp.id">
                            {{ emp.firstName }} {{ emp.lastName }}
                        </option>
                    </select>

                    <div class="btn-group" role="group">
                        <button type="button" 
                                :class="['btn', 'btn-outline-primary', { active: viewMode === 'list' }]"
                                @click="viewMode = 'list'">
                            <i class="bi bi-list-ul me-1"></i>Liste
                        </button>
                        <button type="button" 
                                :class="['btn', 'btn-outline-primary', { active: viewMode === 'calendar' }]"
                                @click="viewMode = 'calendar'">
                            <i class="bi bi-calendar3 me-1"></i>Kalender
                        </button>
                        <button type="button" 
                                :class="['btn', 'btn-outline-primary', { active: viewMode === 'summary' }]"
                                @click="viewMode = 'summary'">
                            <i class="bi bi-graph-up me-1"></i>Zusammenfassung
                        </button>
                    </div>
                </div>
            </div>

            <!-- Loading Indicator -->
            <div v-if="loading" class="text-center my-4">
                <div class="spinner-border" role="status">
                    <span class="visually-hidden">Laden...</span>
                </div>
            </div>

            <!-- Calendar View -->
            <div v-if="viewMode === 'calendar' && !loading">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">Kalenderwoche {{ weekDisplay.weekNumber }} - {{ weekDisplay.fullRange }}</h5>
                        <div class="btn-group">
                            <button @click="previousWeek" class="btn btn-outline-secondary btn-sm">
                                <i class="bi bi-chevron-left"></i>
                            </button>
                            <button @click="nextWeek" class="btn btn-outline-secondary btn-sm">
                                <i class="bi bi-chevron-right"></i>
                            </button>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="calendar-grid">
                            <div class="calendar-header">
                                <div class="calendar-cell">Mo</div>
                                <div class="calendar-cell">Di</div>
                                <div class="calendar-cell">Mi</div>
                                <div class="calendar-cell">Do</div>
                                <div class="calendar-cell">Fr</div>
                                <div class="calendar-cell">Sa</div>
                                <div class="calendar-cell">So</div>
                            </div>
                            <div class="calendar-body">
                                <div v-for="(day, index) in calendarDays" :key="index" 
                                     :class="['calendar-cell', getDayClass(day)]"
                                     :title="getDayTooltip(day)">
                                    <div class="calendar-day">
                                        <div class="calendar-date">{{ day.day }}</div>
                                        <div class="calendar-day-name">{{ day.dayName }}</div>
                                        <div v-if="day.summary" class="calendar-hours">
                                            <div class="worked-hours">{{ day.summary.workedHours }}h</div>
                                            <div v-if="day.summary.expectedHours > 0" class="expected-hours">/{{ day.summary.expectedHours }}h</div>
                                        </div>
                                        <div v-if="day.summary && day.summary.holidayType" class="calendar-holiday-indicator">
                                            <i class="bi bi-umbrella"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <!-- Legend -->
                        <div class="mt-3">
                            <h6>Legende:</h6>
                            <div class="d-flex flex-wrap gap-3">
                                <div class="d-flex align-items-center">
                                    <div class="calendar-legend-item calendar-perfect"></div>
                                    <span class="ms-1">Perfekt</span>
                                </div>
                                <div class="d-flex align-items-center">
                                    <div class="calendar-legend-item calendar-overtime"></div>
                                    <span class="ms-1">Überstunden</span>
                                </div>
                                <div class="d-flex align-items-center">
                                    <div class="calendar-legend-item calendar-undertime"></div>
                                    <span class="ms-1">Unterstunden</span>
                                </div>
                                <div class="d-flex align-items-center">
                                    <div class="calendar-legend-item calendar-missing"></div>
                                    <span class="ms-1">Fehlend</span>
                                </div>
                                <div class="d-flex align-items-center">
                                    <div class="calendar-legend-item calendar-holiday"></div>
                                    <span class="ms-1">Urlaub</span>
                                </div>
                                <div class="d-flex align-items-center">
                                    <div class="calendar-legend-item calendar-weekend"></div>
                                    <span class="ms-1">Wochenende</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Summary View -->
            <div v-if="viewMode === 'summary' && !loading">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">Kalenderwoche {{ weekDisplay.weekNumber }} - {{ weekDisplay.fullRange }}</h5>
                        <div class="btn-group">
                            <button @click="previousWeek" class="btn btn-outline-secondary btn-sm">
                                <i class="bi bi-chevron-left"></i>
                            </button>
                            <button @click="nextWeek" class="btn btn-outline-secondary btn-sm">
                                <i class="bi bi-chevron-right"></i>
                            </button>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div v-for="summary in weeklySummaries" :key="summary.employeeId" class="col-md-6 col-lg-4 mb-3">
                                <div class="card">
                                    <div class="card-header">
                                        <h6 class="mb-0">{{ summary.employeeName }}</h6>
                                        <small class="text-muted">KW {{ weekDisplay.weekNumber }} - {{ weekDisplay.fullRange }}</small>
                                    </div>
                                    <div class="card-body">
                                        <div class="row text-center">
                                            <div class="col-6">
                                                <div class="summary-stat">
                                                    <div class="summary-value">{{ summary.totalWorked.toFixed(1) }}h</div>
                                                    <div class="summary-label">Gearbeitet</div>
                                                </div>
                                            </div>
                                            <div class="col-6">
                                                <div class="summary-stat">
                                                    <div class="summary-value">{{ summary.totalExpected.toFixed(1) }}h</div>
                                                    <div class="summary-label">Erwartet</div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="row mt-3">
                                            <div class="col-6">
                                                <div class="summary-stat">
                                                    <div :class="['summary-value', summary.overtime > 0 ? 'text-success' : 'text-muted']">
                                                        +{{ summary.overtime.toFixed(1) }}h
                                                    </div>
                                                    <div class="summary-label">Überstunden</div>
                                                </div>
                                            </div>
                                            <div class="col-6">
                                                <div class="summary-stat">
                                                    <div :class="['summary-value', summary.undertime > 0 ? 'text-warning' : 'text-muted']">
                                                        -{{ summary.undertime.toFixed(1) }}h
                                                    </div>
                                                    <div class="summary-label">Unterstunden</div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="mt-3">
                                            <div class="d-flex justify-content-between">
                                                <span>Urlaubstage: {{ summary.holidayDays }}</span>
                                                <span v-if="summary.pendingHolidayRequests > 0" class="text-warning">
                                                    {{ summary.pendingHolidayRequests }} ausstehend
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- List View -->
            <div v-if="viewMode === 'list'">
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
                
                <!-- Holiday Info -->
                <div v-if="filteredHolidays.length > 0" class="alert alert-info">
                    <strong>Abwesenheiten:</strong>
                    <ul class="mb-0">
                        <li v-for="holiday in filteredHolidays" :key="holiday.id">
                            {{ formatDate(holiday.startDate) }} bis {{ formatDate(holiday.endDate) }} – {{ holiday.type }}
                            <span v-if="holiday.reason">({{ holiday.reason }})</span>
                        </li>
                    </ul>
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
        </div>
        `
    }).mount(el);
});