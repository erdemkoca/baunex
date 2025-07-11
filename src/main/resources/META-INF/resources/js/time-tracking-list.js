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
                projectColors: {}, // Cache for project colors
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
            },
            getTimeBlockStyle(start, end) {
                // Grid is 06:00 (0%) to 20:00 (100%)
                if (!start || !end) return {};
                const [sh, sm] = start.split(":").map(Number);
                const [eh, em] = end.split(":").map(Number);
                const startMins = (sh * 60 + sm) - 360; // 360 = 6*60
                const endMins = (eh * 60 + em) - 360;
                const totalMins = 14 * 60; // 14 hours
                const top = Math.max(0, (startMins / totalMins) * 100);
                const height = Math.max(8, ((endMins - startMins) / totalMins) * 100);
                return {
                    position: 'absolute',
                    left: '6px',
                    right: '6px',
                    top: top + '%',
                    height: height + '%',
                    background: '#0d6efd22',
                    border: '1px solid #0d6efd',
                    borderRadius: '4px',
                    color: '#222',
                    padding: '2px 4px',
                    cursor: 'pointer',
                    overflow: 'hidden',
                    zIndex: 2
                };
            },
            getProjectColor(projectName) {
                if (!projectName) return '#6c757d';
                
                if (!this.projectColors[projectName]) {
                    // Generate a consistent color based on project name
                    const colors = [
                        '#0d6efd', '#198754', '#ffc107', '#dc3545', '#6f42c1',
                        '#fd7e14', '#20c997', '#e83e8c', '#6c757d', '#0dcaf0',
                        '#6610f2', '#d63384', '#198754', '#fd7e14', '#20c997'
                    ];
                    const hash = projectName.split('').reduce((a, b) => {
                        a = ((a << 5) - a) + b.charCodeAt(0);
                        return a & a;
                    }, 0);
                    const index = Math.abs(hash) % colors.length;
                    this.projectColors[projectName] = colors[index];
                }
                
                return this.projectColors[projectName];
            },
            getTimeBlockStyleWithProject(start, end, projectName) {
                const baseStyle = this.getTimeBlockStyle(start, end);
                const projectColor = this.getProjectColor(projectName);
                
                return {
                    ...baseStyle,
                    background: projectColor + '22',
                    border: '1px solid ' + projectColor,
                    color: '#222'
                };
            },
            calculateTimeBlockLayout(timeEntries) {
                if (!timeEntries || timeEntries.length === 0) return [];
                
                // Sort entries by start time
                const sortedEntries = [...timeEntries].sort((a, b) => {
                    const aStart = this.timeToMinutes(a.startTime);
                    const bStart = this.timeToMinutes(b.startTime);
                    return aStart - bStart;
                });
                
                const layout = [];
                const lanes = []; // Track occupied lanes
                
                for (const entry of sortedEntries) {
                    const startMins = this.timeToMinutes(entry.startTime);
                    const endMins = this.timeToMinutes(entry.endTime);
                    
                    // Find the first available lane
                    let laneIndex = 0;
                    while (laneIndex < lanes.length) {
                        const laneEnd = lanes[laneIndex];
                        if (startMins >= laneEnd) {
                            break;
                        }
                        laneIndex++;
                    }
                    
                    // Update the lane
                    if (laneIndex < lanes.length) {
                        lanes[laneIndex] = endMins;
                    } else {
                        lanes.push(endMins);
                    }
                    
                    layout.push({
                        entry: entry,
                        laneIndex: laneIndex,
                        totalLanes: Math.max(lanes.length, 1)
                    });
                }
                
                return layout;
            },
            timeToMinutes(timeStr) {
                if (!timeStr) return 0;
                const [hours, minutes] = timeStr.split(':').map(Number);
                return hours * 60 + minutes;
            },
            getTimeBlockStyleWithLayout(start, end, projectName, laneIndex, totalLanes) {
                const baseStyle = this.getTimeBlockStyle(start, end);
                const projectColor = this.getProjectColor(projectName);
                
                // Calculate width and position based on lane
                const laneWidth = 100 / totalLanes;
                const left = (laneIndex * laneWidth) + 2; // 2% margin
                const right = ((totalLanes - laneIndex - 1) * laneWidth) + 2; // 2% margin
                
                return {
                    ...baseStyle,
                    left: left + '%',
                    right: right + '%',
                    background: projectColor + '22',
                    border: '1px solid ' + projectColor,
                    color: '#222'
                };
            },
            getDaySummaryClass(day) {
                if (!day || !day.summary) return '';
                
                const summary = day.summary;
                
                if (summary.isWeekend) return 'calendar-weekend';
                if (summary.holidayType) return 'calendar-holiday';
                if (summary.workedHours === 0 && summary.expectedHours > 0) return 'calendar-missing';
                if (summary.delta > 0) return 'calendar-overtime';
                if (summary.delta < 0) return 'calendar-undertime';
                if (summary.delta === 0) return 'calendar-perfect';
                
                return '';
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
                        <!-- Vertical Time Grid -->
                        <div class="calendar-vertical-grid" style="display:flex; flex-direction:column; overflow-x:auto;">
                            <!-- Header Row: Empty cell + day names -->
                            <div style="display:flex;">
                                <div style="width:48px;"></div>
                                <div v-for="(day, i) in calendarDays" :key="'header'+i" style="flex:1; min-width:110px; text-align:center; font-weight:bold; border-bottom:1px solid #dee2e6; padding-bottom:4px;">
                                    {{ day.dayName }}<br>{{ day.day }}
                                </div>
                            </div>
                            <div style="display:flex; height:420px;">
                                <!-- Time labels -->
                                <div style="width:48px; display:flex; flex-direction:column; align-items:flex-end; position:relative;">
                                    <div v-for="h in 15" :key="'label'+h" style="height:28px; font-size:11px; color:#888; position:relative;">
                                        <span style="position:absolute; right:2px; top:-7px;">{{ (h+5).toString().padStart(2,'0') }}:00</span>
                                    </div>
                                </div>
                                <!-- Day columns -->
                                <div v-for="(day, dayIdx) in calendarDays" :key="'col'+dayIdx" style="flex:1; min-width:110px; border-left:1px solid #eee; position:relative; background:#f8f9fa;">
                                    <!-- Hour lines -->
                                    <div v-for="h in 15" :key="'line'+h" style="position:absolute; left:0; right:0; top:calc((100%/14)*(h-1)); height:0; border-top:1px solid #eee;"></div>
                                    <!-- Time entry blocks -->
                                    <div v-for="layoutItem in calculateTimeBlockLayout(day.summary?.timeEntries || [])" :key="layoutItem.entry.id"
                                         :style="getTimeBlockStyleWithLayout(layoutItem.entry.startTime, layoutItem.entry.endTime, layoutItem.entry.projectName, layoutItem.laneIndex, layoutItem.totalLanes)"
                                         class="calendar-time-block"
                                         @click="navigateToEdit(layoutItem.entry.id)"
                                    >
                                        <div style="font-size:10px; font-weight:bold; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; color: #333;">
                                            {{ layoutItem.entry.projectName || 'Unbekanntes Projekt' }}
                                        </div>
                                        <div style="font-size:9px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; color: #666;">
                                            {{ layoutItem.entry.title || 'Arbeit' }}
                                        </div>
                                        <div style="font-size:9px; font-weight:bold; color: #333;">
                                            {{ layoutItem.entry.hoursWorked }}h
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!-- Daily Summary Row -->
                            <div style="display:flex; height:60px; border-top:2px solid #dee2e6;">
                                <div style="width:48px;"></div>
                                <div v-for="(day, dayIdx) in calendarDays" :key="'summary'+dayIdx" 
                                     :class="['calendar-day-summary', getDaySummaryClass(day)]"
                                     style="flex:1; min-width:110px; border-left:1px solid #eee; padding:8px; display:flex; flex-direction:column; justify-content:center; align-items:center;">
                                    <div v-if="day.summary" style="text-align:center;">
                                        <div style="font-size:14px; font-weight:bold; color: #333;">
                                            {{ day.summary.workedHours.toFixed(1) }}h
                                        </div>
                                        <div style="font-size:10px; color: #666;">
                                            von {{ day.summary.expectedHours.toFixed(1) }}h
                                        </div>
                                        <div v-if="day.summary.delta !== 0" 
                                             :style="{ fontSize: '9px', color: day.summary.delta > 0 ? '#198754' : '#dc3545' }">
                                            {{ day.summary.delta > 0 ? '+' : '' }}{{ day.summary.delta.toFixed(1) }}h
                                        </div>
                                        <div v-if="day.summary.holidayType" style="font-size:9px; color: #0dcaf0;">
                                            {{ day.summary.holidayType }}
                                        </div>
                                    </div>
                                    <div v-else style="text-align:center; color: #999; font-size:12px;">
                                        Keine Daten
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