import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('time-tracking-calendar-app');
    if (!el) {
        console.error('Element #time-tracking-calendar-app not found!');
        return;
    }

    const timeEntries = JSON.parse(el.dataset.timeEntries || '[]');
    const holidays = JSON.parse(el.dataset.holidays || '[]');
    const employees = JSON.parse(el.dataset.employees || '[]');
    const projects = JSON.parse(el.dataset.projects || '[]');

    const app = createApp({
        data() {
            return {
                timeEntries,
                holidays,
                employees,
                projects,
                selectedEmployeeId: employees[0]?.id || null,
                currentWeek: this.getCurrentWeek(),
                currentYear: 2025, // Default to 2025 where we have sample data
                dailySummaries: [],
                weeklySummaries: [],
                loading: false,
                projectColors: {}, // Cache for project colors
                successMessage: '',
                showSuccessMessageTimeout: null,
                weekPickerStart: 25, // Start week for week picker (matching currentWeek)
                weekPickerVisibleCount: 12, // Number of weeks to show at once
                weekPickerData: [] // Week picker data
            };
        },
        computed: {
            selectedEmployee() {
                return this.employees.find(emp => emp.id === this.selectedEmployeeId);
            },
            employeeStartDate() {
                if (!this.selectedEmployee?.startDate) return null;
                return new Date(this.selectedEmployee.startDate);
            },
            currentWeekStartDate() {
                return this.getWeekStartDate(this.currentYear, this.currentWeek);
            },
            isCurrentWeekValid() {
                if (!this.employeeStartDate) return true;
                return this.currentWeekStartDate >= this.employeeStartDate;
            },
            calendarDays() {
                const weekStart = this.getWeekStartDate(this.currentYear, this.currentWeek);
                const days = [];
                
                // Add all 7 days of the week
                for (let i = 0; i < 7; i++) {
                    const date = new Date(weekStart);
                    date.setDate(weekStart.getDate() + i);
                    const dateStr = this.toLocalDateString(date);
                    const summary = this.dailySummaries.find(s => s.date === dateStr);
                    
                    days.push({
                        date: date,
                        dateStr: dateStr,
                        day: date.getDate(),
                        dayName: this.getDayName(date.getDay()),
                        summary: summary
                    });
                }
                
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
                    
                    const res = await fetch(`/timetracking/api/summary/daily?employeeId=${this.selectedEmployeeId}&from=${from}&to=${to}`);
                    if (res.ok) {
                        this.dailySummaries = await res.json();
                    }
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
                
                // Check if we're going before the employee's start date
                if (this.employeeStartDate && this.currentWeekStartDate < this.employeeStartDate) {
                    // Reset to the week of the employee's start date
                    const startWeek = this.getWeekOfYear(this.employeeStartDate);
                    const startYear = this.employeeStartDate.getFullYear();
                    this.currentWeek = startWeek;
                    this.currentYear = startYear;
                }
                
                this.loadDailySummaries();
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
                this.loadDailySummaries();
                this.loadWeeklySummaries();
            },
            getDayClass(day) {
                if (!day || !day.summary) return '';
                
                const summary = day.summary;
                
                if (summary.isWeekend) return 'calendar-weekend';
                if (summary.holidayType || summary.isPublicHoliday) return 'calendar-holiday';
                
                // For approved holidays and public holidays, treat like weekends (0 expected hours)
                const effectiveExpectedHours = ((summary.holidayType && summary.holidayApproved) || summary.isPublicHoliday) ? 0 : summary.expectedHours;
                const effectiveDelta = summary.workedHours - effectiveExpectedHours;
                
                if (summary.workedHours === 0 && effectiveExpectedHours > 0) return 'calendar-missing';
                if (effectiveDelta > 0) return 'calendar-overtime';
                if (effectiveDelta < 0) return 'calendar-undertime';
                if (effectiveDelta === 0) return 'calendar-perfect';
                
                return '';
            },
            getDayTooltip(day) {
                if (!day || !day.summary) return '';
                
                const summary = day.summary;
                let tooltip = `${this.formatDate(day.date)} (${day.dayName})\n`;
                tooltip += `Gearbeitet: ${summary.workedHours}h\n`;
                
                // For approved holidays and public holidays, show 0 expected hours
                const effectiveExpectedHours = ((summary.holidayType && summary.holidayApproved) || summary.isPublicHoliday) ? 0 : summary.expectedHours;
                tooltip += `Erwartet: ${effectiveExpectedHours}h\n`;
                
                const effectiveDelta = summary.workedHours - effectiveExpectedHours;
                if (effectiveDelta !== 0) {
                    tooltip += `Differenz: ${effectiveDelta > 0 ? '+' : ''}${effectiveDelta.toFixed(1)}h\n`;
                }
                
                if (summary.holidayType) {
                    tooltip += `Urlaub: ${this.getHolidayTypeDisplayName(summary.holidayType)}\n`;
                    if (summary.holidayReason) tooltip += `Grund: ${summary.holidayReason}\n`;
                    if (summary.holidayApproved) tooltip += `Status: Genehmigt\n`;
                }
                
                if (summary.isPublicHoliday && summary.publicHolidayName) {
                    tooltip += `Feiertag: ${summary.publicHolidayName}\n`;
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
                this.loadDailySummaries();
                this.loadWeeklySummaries();
                this.loadWeekPickerData();
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
            calculateBreakPositions(entry) {
                if (!entry.breaks || entry.breaks.length === 0) return [];
                
                const totalDuration = this.timeToMinutes(entry.endTime) - this.timeToMinutes(entry.startTime);
                const breakPositions = [];
                
                for (const breakItem of entry.breaks) {
                    const breakStart = this.timeToMinutes(breakItem.start);
                    const breakEnd = this.timeToMinutes(breakItem.end);
                    const entryStart = this.timeToMinutes(entry.startTime);
                    
                    // Calculate relative position within the time block
                    const relativeStart = breakStart - entryStart;
                    const breakDuration = breakEnd - breakStart;
                    
                    if (relativeStart >= 0 && breakDuration > 0) {
                        const topPercent = (relativeStart / totalDuration) * 100;
                        const heightPercent = (breakDuration / totalDuration) * 100;
                        
                        breakPositions.push({
                            top: topPercent + '%',
                            height: heightPercent + '%',
                            duration: breakDuration,
                            start: breakItem.start,
                            end: breakItem.end
                        });
                    }
                }
                
                return breakPositions;
            },
            formatBreakDuration(minutes) {
                const hours = Math.floor(minutes / 60);
                const mins = minutes % 60;
                if (hours > 0) {
                    return `${hours}h${mins > 0 ? mins + 'm' : ''}`;
                }
                return `${mins}m`;
            },
            async approveTimeEntry(entryId) {
                try {
                    console.log('Approving time entry:', entryId);
                    const response = await fetch(`/timetracking/api/${entryId}/approve`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    });
                    
                    if (response.ok) {
                        console.log('Time entry approved successfully');
                        // Update data in place instead of reloading
                        await this.loadDailySummaries();
                        // Show success message
                        this.showSuccessMessage('Zeiteintrag erfolgreich genehmigt!');
                    } else {
                        const errorText = await response.text();
                        console.error('Approval failed:', errorText);
                        alert('Fehler beim Genehmigen des Eintrags: ' + errorText);
                    }
                } catch (error) {
                    console.error('Error approving time entry:', error);
                    alert('Fehler beim Genehmigen des Eintrags');
                }
            },
            async approveHoliday(holidayId) {
                if (!confirm('Möchten Sie diesen Urlaubsantrag genehmigen?')) {
                    return;
                }
                try {
                    console.log('Approving holiday:', holidayId);
                    const response = await fetch(`/timetracking/api/holidays/${holidayId}/approve`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            approval: {
                                status: 'APPROVED',
                                approverId: this.selectedEmployeeId,
                                approvedAt: new Date().toISOString().split('T')[0]
                            }
                        })
                    });
                    
                    if (response.ok) {
                        console.log('Holiday approved successfully');
                        // Update data in place instead of reloading
                        await this.loadDailySummaries();
                        // Show success message
                        this.showSuccessMessage('Urlaubsantrag erfolgreich genehmigt!');
                    } else {
                        const errorText = await response.text();
                        console.error('Holiday approval failed:', errorText);
                        alert('Fehler beim Genehmigen des Urlaubsantrags: ' + errorText);
                    }
                } catch (error) {
                    console.error('Error approving holiday:', error);
                    alert('Fehler beim Genehmigen des Urlaubsantrags');
                }
            },
            getHolidayTypeDisplayName(holidayType) {
                return holidayType || 'Nicht definiert';
            },
            formatDate(date) {
                return new Date(date).toLocaleDateString('de-CH');
            },
            showSuccessMessage(message) {
                this.successMessage = message;
                // Clear any existing timeout
                if (this.showSuccessMessageTimeout) {
                    clearTimeout(this.showSuccessMessageTimeout);
                }
                // Auto-hide after 3 seconds
                this.showSuccessMessageTimeout = setTimeout(() => {
                    this.successMessage = '';
                }, 3000);
            },
            // Week picker methods
            async loadWeekPickerData() {
                this.weekPickerData = await this.getVisibleWeeks();
            },
            async getVisibleWeeks() {
                const weeks = [];
                
                for (let i = 0; i < this.weekPickerVisibleCount; i++) {
                    const weekNumber = this.weekPickerStart + i;
                    const weekStart = this.getWeekStartDate(this.currentYear, weekNumber);
                    const weekEnd = new Date(weekStart);
                    weekEnd.setDate(weekStart.getDate() + 6);
                    
                    // Determine approval status
                    const today = new Date();
                    const isPast = weekEnd < today;
                    const isFuture = weekStart > today;
                    
                    let approvalStatus = 'empty';
                    
                    if (isFuture) {
                        approvalStatus = 'future';
                    } else {
                        // For past and current weeks, check actual approval status
                        try {
                            const weekStartStr = this.toLocalDateString(weekStart);
                            const weekEndStr = this.toLocalDateString(weekEnd);
                            
                            // Fetch time entries and vacation requests for this week
                            const [timeEntriesResponse, holidaysResponse] = await Promise.all([
                                fetch(`/timetracking/api/summary/daily?employeeId=${this.selectedEmployeeId}&from=${weekStartStr}&to=${weekEndStr}`),
                                fetch(`/timetracking/api/holidays/employee/${this.selectedEmployeeId}`)
                            ]);
                            
                            if (timeEntriesResponse.ok && holidaysResponse.ok) {
                                const dailySummaries = await timeEntriesResponse.json();
                                const holidays = await holidaysResponse.json();
                                
                                // Filter holidays for this week
                                const weekHolidays = holidays.filter(holiday => {
                                    const holidayStart = new Date(holiday.startDate);
                                    const holidayEnd = new Date(holiday.endDate);
                                    return holidayStart <= weekEnd && holidayEnd >= weekStart;
                                });
                                
                                // Check if there are any entries or holidays
                                const hasTimeEntries = dailySummaries.some(day => day.timeEntries && day.timeEntries.length > 0);
                                const hasHolidays = weekHolidays.length > 0;
                                
                                // Check approval status for time entries
                                let timeEntriesApproved = true;
                                if (hasTimeEntries) {
                                    timeEntriesApproved = dailySummaries.every(day => 
                                        !day.timeEntries || day.timeEntries.length === 0 || 
                                        day.timeEntries.every(entry => entry.approval?.approved === true)
                                    );
                                }
                                
                                // Check approval status for holidays
                                let holidaysApproved = true;
                                if (hasHolidays) {
                                    holidaysApproved = weekHolidays.every(holiday => 
                                        holiday.status === 'APPROVED' || holiday.approval?.approved === true
                                    );
                                }
                                
                                // Determine overall approval status
                                if (hasTimeEntries || hasHolidays) {
                                    if (timeEntriesApproved && holidaysApproved) {
                                        approvalStatus = 'approved';
                                    } else {
                                        approvalStatus = 'pending';
                                    }
                                } else {
                                    approvalStatus = 'empty';
                                }
                            }
                        } catch (error) {
                            console.error('Error fetching approval status for week:', weekNumber, error);
                            approvalStatus = 'pending'; // Default to pending on error
                        }
                    }
                    
                    weeks.push({
                        weekNumber: weekNumber,
                        year: this.currentYear,
                        weekStart: weekStart,
                        weekEnd: weekEnd,
                        shortRange: `${weekStart.getDate()}.${weekStart.getMonth() + 1} – ${weekEnd.getDate()}.${weekEnd.getMonth() + 1}`,
                        fullRange: `${weekStart.toLocaleDateString('de-CH')} – ${weekEnd.toLocaleDateString('de-CH')}`,
                        approvalStatus: approvalStatus
                    });
                }
                
                return weeks;
            },
            selectWeek(week) {
                this.currentWeek = week.weekNumber;
                this.currentYear = week.year;
                this.onWeekChange();
            },
            scrollWeekPicker(direction) {
                this.weekPickerStart += direction * this.weekPickerVisibleCount;
                // Ensure we don't go below week 1 or above week 52
                this.weekPickerStart = Math.max(1, Math.min(52 - this.weekPickerVisibleCount + 1, this.weekPickerStart));
                this.loadWeekPickerData();
            },
            getWeekPickerItemClass(week) {
                const isCurrentWeek = week.weekNumber === this.currentWeek && week.year === this.currentYear;
                
                let classes = ['week-picker-item'];
                
                // Mark current selected week
                if (isCurrentWeek) {
                    classes.push('active');
                }
                
                // Color coding based on approval status
                if (week.approvalStatus === 'future') {
                    classes.push('future-week');
                } else if (week.approvalStatus === 'approved') {
                    classes.push('approved');
                } else if (week.approvalStatus === 'pending') {
                    classes.push('pending');
                } else if (week.approvalStatus === 'rejected') {
                    classes.push('rejected');
                } else {
                    classes.push('empty');
                }
                
                return classes.join(' ');
            },
            getWeekOfYear(date) {
                const d = new Date(date.getTime());
                d.setHours(0, 0, 0, 0);
                // Thursday in current week decides the year
                d.setDate(d.getDate() + 3 - (d.getDay() + 6) % 7);
                // January 4 is always in week 1
                const week1 = new Date(d.getFullYear(), 0, 4);
                // Adjust to Thursday in week 1 and count number of weeks from date to week1
                return 1 + Math.round(((d.getTime() - week1.getTime()) / 86400000 - 3 + (week1.getDay() + 6) % 7) / 7);
            }
        },
        mounted() {
            this.loadDailySummaries();
            this.loadWeeklySummaries();
            // Initialize week picker to show current week
            this.weekPickerStart = Math.max(1, this.currentWeek - Math.floor(this.weekPickerVisibleCount / 2));
            this.loadWeekPickerData();
        },
        template: `
        <div class="container-fluid">
            <!-- Success Message -->
            <div v-if="successMessage" class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="bi bi-check-circle me-2"></i>{{ successMessage }}
                <button type="button" class="btn-close" @click="successMessage = ''"></button>
            </div>
            
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
                </div>
            </div>

            <!-- Employee Start Date Info -->
            <div v-if="employeeStartDate" class="alert alert-info mb-3">
                <i class="bi bi-info-circle me-2"></i>
                <strong>{{ selectedEmployee?.firstName }} {{ selectedEmployee?.lastName }}</strong> 
                arbeitet seit {{ new Date(employeeStartDate).toLocaleDateString('de-CH') }}. 
                Nur Wochen ab diesem Datum werden angezeigt.
            </div>

            <!-- Loading Indicator -->
            <div v-if="loading" class="text-center my-4">
                <div class="spinner-border" role="status">
                    <span class="visually-hidden">Laden...</span>
                </div>
            </div>

            <!-- Calendar View -->
            <div v-if="!loading">
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
                    
                    <!-- Week Picker -->
                    <div class="card-body border-bottom">
                        <div class="week-picker-container">
                            <button @click="scrollWeekPicker(-1)" class="btn btn-outline-secondary btn-sm week-picker-nav">
                                <i class="bi bi-chevron-left"></i>
                            </button>
                            
                            <div class="week-picker-scroll" ref="weekPickerScroll">
                                <div v-for="week in weekPickerData" 
                                     :key="week.weekNumber" 
                                     :class="['week-picker-item', getWeekPickerItemClass(week)]"
                                     @click="selectWeek(week)">
                                    <div class="week-number">{{ week.weekNumber }}</div>
                                    <div class="week-range">{{ week.shortRange }}</div>
                                </div>
                            </div>
                            
                            <button @click="scrollWeekPicker(1)" class="btn btn-outline-secondary btn-sm week-picker-nav">
                                <i class="bi bi-chevron-right"></i>
                            </button>
                        </div>
                        
                        <!-- Week Picker Legend -->
                        <div class="mt-2">
                            <small class="text-muted">
                                <span class="me-3"><span class="badge bg-secondary">⚪</span> Zukunft</span>
                                <span class="me-3"><span class="badge bg-success">🟢</span> Genehmigt</span>
                                <span class="me-3"><span class="badge bg-warning">🟡</span> Ausstehend</span>
                                <span class="me-3"><span class="badge bg-danger">🔴</span> Abgelehnt</span>
                            </small>
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
                                        <!-- Approval Status Indicator -->
                                        <div :class="['approval-indicator', layoutItem.entry.approval?.approved ? 'approved' : 'pending']"
                                             :title="layoutItem.entry.approval?.approved ? 'Genehmigt' : 'Ausstehend'">
                                            <i :class="['bi', layoutItem.entry.approval?.approved ? 'bi-check-circle-fill' : 'bi-clock']"></i>
                                        </div>
                                        <!-- Approval Button -->
                                        <div v-if="!layoutItem.entry.approval?.approved" 
                                             class="approval-button"
                                             @click.stop="approveTimeEntry(layoutItem.entry.id)"
                                             :title="'Genehmigen: ' + layoutItem.entry.title">
                                            <i class="bi bi-check-circle"></i>
                                        </div>
                                        <!-- Break overlays -->
                                        <div v-for="(breakItem, breakIndex) in calculateBreakPositions(layoutItem.entry)" 
                                             :key="'break-' + breakIndex"
                                             class="calendar-time-block-break"
                                             :style="{ top: breakItem.top, height: breakItem.height }"
                                             :title="'Pause: ' + breakItem.start + ' - ' + breakItem.end + ' (' + formatBreakDuration(breakItem.duration) + ')'"
                                        >
                                            <span class="calendar-time-block-break-label">{{ formatBreakDuration(breakItem.duration) }}</span>
                                        </div>
                                        
                                        <div style="font-size:10px; font-weight:bold; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; color: #333;">
                                            {{ layoutItem.entry.projectName || 'Unbekanntes Projekt' }}
                                        </div>
                                        <div style="font-size:9px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; color: #666;">
                                            {{ layoutItem.entry.title || 'Arbeit' }}
                                        </div>
                                        <div style="font-size:9px; font-weight:bold; color: #333;">
                                            {{ layoutItem.entry.hoursWorked }}h
                                        </div>
                                        <div v-if="layoutItem.entry.breaks && layoutItem.entry.breaks.length > 0" class="calendar-break-indicator">
                                            {{ layoutItem.entry.breaks.length }} Pause{{ layoutItem.entry.breaks.length > 1 ? 'n' : '' }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!-- Daily Summary Row -->
                            <div style="display:flex; height:60px; border-top:2px solid #dee2e6;">
                                <div style="width:48px;"></div>
                                <div v-for="(day, dayIdx) in calendarDays" :key="'summary'+dayIdx" 
                                     :class="['calendar-day-summary', getDayClass(day)]"
                                     style="flex:1; min-width:110px; border-left:1px solid #eee; padding:8px; display:flex; flex-direction:column; justify-content:center; align-items:center;">
                                    <div v-if="day.summary" style="text-align:center;">
                                        <div style="font-size:14px; font-weight:bold; color: #333;">
                                            {{ day.summary.workedHours.toFixed(1) }}h
                                        </div>
                                        <div style="font-size:10px; color: #666;">
                                            von {{ ((day.summary.holidayType && day.summary.holidayApproved) || day.summary.isPublicHoliday ? 0 : day.summary.expectedHours).toFixed(1) }}h
                                        </div>
                                        <div v-if="(day.summary.workedHours - ((day.summary.holidayType && day.summary.holidayApproved) || day.summary.isPublicHoliday ? 0 : day.summary.expectedHours)) !== 0" 
                                             :style="{ fontSize: '9px', color: (day.summary.workedHours - ((day.summary.holidayType && day.summary.holidayApproved) || day.summary.isPublicHoliday ? 0 : day.summary.expectedHours)) > 0 ? '#198754' : '#dc3545' }">
                                            {{ (day.summary.workedHours - ((day.summary.holidayType && day.summary.holidayApproved) || day.summary.isPublicHoliday ? 0 : day.summary.expectedHours)) > 0 ? '+' : '' }}{{ (day.summary.workedHours - ((day.summary.holidayType && day.summary.holidayApproved) || day.summary.isPublicHoliday ? 0 : day.summary.expectedHours)).toFixed(1) }}h
                                        </div>
                                        <div v-if="day.summary.holidayType" style="font-size:9px; color: #0dcaf0;">
                                            {{ getHolidayTypeDisplayName(day.summary.holidayType) }}
                                            <!-- Holiday approval button -->
                                            <button v-if="!day.summary.holidayApproved" 
                                                    @click.stop="approveHoliday(day.summary.holidayId)"
                                                    class="btn btn-success btn-sm ms-1"
                                                    style="font-size: 8px; padding: 1px 4px;">
                                                <i class="bi bi-check-circle"></i>
                                            </button>
                                        </div>
                                        <div v-if="day.summary.isPublicHoliday && day.summary.publicHolidayName" style="font-size:9px; color: #0dcaf0;">
                                            {{ day.summary.publicHolidayName }}
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
                                <div class="d-flex align-items-center">
                                    <div style="width:20px; height:20px; border-radius:0.25rem; border:1px dashed #666; background-color:rgba(255,255,255,0.7); position:relative;">
                                        <span style="position:absolute; left:2px; top:50%; transform:translateY(-50%); font-size:8px; color:#666;">☕</span>
                                    </div>
                                    <span class="ms-1">Pausen</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Weekly Summary -->
                <div class="card mt-4">
                    <div class="card-header">
                        <h5 class="mb-0">Wochenzusammenfassung - {{ weekDisplay.fullRange }}</h5>
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
                                            <div class="d-flex justify-content-between mb-1">
                                                <span>Urlaubstage: {{ summary.holidayDays }}</span>
                                                <span v-if="summary.pendingHolidayRequests > 0" class="text-warning">
                                                    {{ summary.pendingHolidayRequests }} ausstehend
                                                </span>
                                            </div>
                                            <div class="d-flex justify-content-between small text-muted">
                                                <span>Verbraucht: {{ summary.usedVacationDays }}/{{ summary.totalVacationDays }}</span>
                                                <span :class="summary.remainingVacationDays < 5 ? 'text-warning' : 'text-success'">
                                                    {{ summary.remainingVacationDays }} verbleibend
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
        </div>
        `
    });

    app.mount('#time-tracking-calendar-app');
}); 