import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.prod.js';

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
    const monthlyAccount = JSON.parse(el.dataset.monthlyAccount || 'null');

    const app = createApp({
        data() {
            // Load saved values from localStorage
            const savedWeek = localStorage.getItem('calendar-current-week');
            const savedYear = localStorage.getItem('calendar-current-year');
            const savedEmployeeId = localStorage.getItem('calendar-selected-employee');
            
            return {
                timeEntries,
                holidays,
                employees,
                projects,
                monthlyAccount,
                selectedEmployeeId: savedEmployeeId ? parseInt(savedEmployeeId) : (employees[0]?.id || null),
                currentWeek: savedWeek ? parseInt(savedWeek) : this.getCurrentWeek(),
                currentYear: savedYear ? parseInt(savedYear) : 2025, // Default to 2025 where we have sample data
                dailySummaries: [],
                weeklySummaries: [],
                loading: false,
                projectColors: {}, // Cache for project colors
                successMessage: '',
                showSuccessMessageTimeout: null,
                errorMessage: '',
                errorTimeout: null,

                showWeekPicker: false, // New state for popup visibility
                timeRangeStart: 8, // Start hour (08:00)
                timeRangeEnd: 17, // End hour (17:00)
                showFormModal: false, // New state for form modal
                formLoaded: false, // Track if form is loaded
                currentEntryData: null, // Store current entry data for modal
                cumulativeHoursAccount: {
                    weeklyBalance: 0,
                    weeklyWorkedHours: 0,
                    weeklyExpectedHours: 0,
                    cumulativeBalance: 0,
                    cumulativeWorkedHours: 0,
                    cumulativeExpectedHours: 0,
                    cumulativeBalanceFormatted: '+0.0',
                    totalVacationDays: 0,
                    usedVacationDays: 0,
                    remainingVacationDays: 0,
                    holidayDays: 0,
                    pendingHolidayRequests: 0
                }, // Store cumulative hours account data
                
                // Holiday types
                holidayTypes: [],
                defaultWorkdayHours: 8.0, // Will be updated from API
                employeeDefaultWorkdayHours: {},
                
                // Company settings for expected hours calculation
                companySettings: {
                    plannedWeeklyHours: 40.0,
                    defaultWorkdaysPerWeek: 5
                }
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
            currentMonthYear() {
                const weekStart = this.getWeekStartDate(this.currentYear, this.currentWeek);
                const monthNames = [
                    'Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
                    'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'
                ];
                const month = monthNames[weekStart.getMonth()];
                const year = weekStart.getFullYear();
                return `${month} ${year}`;
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
                    
                    // Check if this day is before employee start date
                    const isBeforeStartDate = this.employeeStartDate && date < this.employeeStartDate;
                    
                    days.push({
                        date: date,
                        dateStr: dateStr,
                        day: date.getDate(),
                        dayName: this.getDayName(date.getDay()),
                        summary: summary,
                        isBeforeStartDate: isBeforeStartDate
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
            },
            visibleHours() {
                return this.timeRangeEnd - this.timeRangeStart + 1;
            },
            allWeeks() {
                const weeks = [];
                const year = this.currentYear;
                
                // Get employee start year if available
                const employeeStartYear = this.employeeStartDate ? this.employeeStartDate.getFullYear() : null;
                
                // If employee start year is in the past relative to current year, show all weeks
                // Otherwise, only show weeks from the employee's start year
                let startWeek;
                if (employeeStartYear && employeeStartYear > year) {
                    // Employee starts in a future year, show no weeks
                    startWeek = 53; // This will result in no weeks being shown
                } else if (employeeStartYear && employeeStartYear === year) {
                    // Employee starts this year, find the week of their start date
                    const startDate = this.employeeStartDate;
                    startWeek = this.getWeekOfYear(startDate);
                } else {
                    // Employee started in past years or no start date, show all weeks
                    startWeek = 1;
                }
                
                // Generate weeks from start week to 52
                for (let weekNumber = startWeek; weekNumber <= 52; weekNumber++) {
                    const weekStart = this.getWeekStartDate(year, weekNumber);
                    const weekEnd = new Date(weekStart);
                    weekEnd.setDate(weekStart.getDate() + 6);
                    
                    // Check if this week is before employee start date
                    const isBeforeStartDate = this.employeeStartDate && weekEnd < this.employeeStartDate;
                    
                    // Determine approval status based on actual data
                    const today = new Date();
                    const isPast = weekEnd < today;
                    const isFuture = weekStart > today;
                    
                    let approvalStatus = 'empty';
                    
                    if (isBeforeStartDate) {
                        approvalStatus = 'before-start';
                    } else if (isFuture) {
                        approvalStatus = 'future';
                    } else {
                        // For past and current weeks, we'll set a default status
                        // The actual status will be loaded asynchronously
                        approvalStatus = 'pending';
                    }
                    
                    weeks.push({
                        weekNumber: weekNumber,
                        year: year,
                        weekStart: weekStart,
                        weekEnd: weekEnd,
                        shortRange: `${weekStart.getDate()}.${weekStart.getMonth() + 1} – ${weekEnd.getDate()}.${weekEnd.getMonth() + 1}`,
                        fullRange: `${weekStart.toLocaleDateString('de-CH')} – ${weekEnd.toLocaleDateString('de-CH')}`,
                        approvalStatus: approvalStatus,
                        isBeforeStartDate: isBeforeStartDate
                    });
                }
                
                return weeks;
            }
        },
        methods: {
            /**
             * Lädt die Holiday-Types vom Server
             */
            async loadHolidayTypes() {
                try {
                    const response = await fetch('/api/holiday-types');
                    if (response.ok) {
                        this.holidayTypes = await response.json();
                        console.log('Holiday types loaded:', this.holidayTypes);
                    } else {
                        console.warn('Failed to load holiday types, using defaults');
                        this.useDefaultHolidayTypes();
                    }
                } catch (error) {
                    console.warn('Error loading holiday types, using defaults:', error);
                    this.useDefaultHolidayTypes();
                }
            },
            
            /**
             * Lädt die Unternehmenseinstellungen vom Server
             */
            async loadCompanySettings() {
                try {
                    const response = await fetch('/api/company-settings');
                    if (response.ok) {
                        const settings = await response.json();
                        this.companySettings = {
                            plannedWeeklyHours: settings.plannedWeeklyHours || 40.0,
                            defaultWorkdaysPerWeek: settings.defaultWorkdaysPerWeek || 5
                        };
                        console.log('Company settings loaded:', this.companySettings);
                    } else {
                        console.warn('Failed to load company settings, using defaults');
                    }
                } catch (error) {
                    console.warn('Error loading company settings, using defaults:', error);
                }
            },
            
            /**
             * Verwendet die Standard-Holiday-Types falls der Server nicht erreichbar ist
             */
            useDefaultHolidayTypes() {
                this.holidayTypes = [
                    { code: 'PAID_VACATION', displayName: 'Bezahlter Urlaub', factor: 0.0 },
                    { code: 'UNPAID_LEAVE', displayName: 'Unbezahlter Urlaub', factor: 1.0 },
                    { code: 'SICK_LEAVE', displayName: 'Krankheit', factor: 0.0 },
                    { code: 'SPECIAL_LEAVE', displayName: 'Sonderurlaub', factor: 0.0 },
                    { code: 'COMPENSATORY_TIME', displayName: 'Zeitausgleich', factor: 0.0 },
                    { code: 'MATERNITY_LEAVE', displayName: 'Mutterschaftsurlaub', factor: 0.0 },
                    { code: 'PATERNITY_LEAVE', displayName: 'Vaterschaftsurlaub', factor: 0.0 },
                    { code: 'PUBLIC_HOLIDAY', displayName: 'Öffentlicher Feiertag', factor: 0.0 },
                    { code: 'HALF_DAY', displayName: 'Halbtag', factor: 0.5 }
                ];
            },
            
            /**
             * Gibt die erwarteten Arbeitsstunden für einen gegebenen HolidayType zurück.
             * Berechnet die erwarteten Stunden basierend auf dem Faktor und den geplanten Wochenstunden.
             * Falls der Typ nicht konfiguriert ist, wird der Standard-Arbeitstag zurückgegeben.
             */
            getExpectedHoursForHolidayType(holidayTypeCode) {
                if (!holidayTypeCode) {
                    return this.defaultWorkdayHours;
                }
                
                const holidayType = this.holidayTypes.find(ht => ht.code === holidayTypeCode);
                if (!holidayType) {
                    return this.defaultWorkdayHours;
                }
                
                // Calculate expected hours using factor and company planned weekly hours
                const plannedWeeklyHours = this.companySettings.plannedWeeklyHours;
                const workdaysPerWeek = this.companySettings.defaultWorkdaysPerWeek;
                const defaultWorkdayHours = plannedWeeklyHours / workdaysPerWeek;
                const expectedHours = defaultWorkdayHours * holidayType.factor;
                
                return expectedHours;
            },
            
            /**
             * Gibt die Beschreibung für einen HolidayType zurück.
             */
            getHolidayTypeDescription(holidayTypeCode) {
                if (!holidayTypeCode) {
                    return null;
                }
                
                const holidayType = this.holidayTypes.find(ht => ht.code === holidayTypeCode);
                return holidayType ? holidayType.description : null;
            },
            
            /**
             * Gibt den Display-Namen für einen HolidayType zurück.
             */
            getHolidayTypeDisplayName(holidayTypeCode) {
                if (!holidayTypeCode) {
                    return 'Unbekannt';
                }
                
                const holidayType = this.holidayTypes.find(ht => ht.code === holidayTypeCode);
                return holidayType ? holidayType.displayName : holidayTypeCode;
            },
            
            // Save current state to localStorage
            saveState() {
                localStorage.setItem('calendar-current-week', this.currentWeek.toString());
                localStorage.setItem('calendar-current-year', this.currentYear.toString());
                localStorage.setItem('calendar-selected-employee', this.selectedEmployeeId?.toString() || '');
            },
            
            navigateToEdit(id) {
                // Use the new edit method instead of the general method
                this.openEditTimeEntryForm(id);
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
            
            async loadCumulativeSaldo() {
                if (!this.selectedEmployeeId) return;
                
                try {
                    // Use the new endpoint for cumulative calculation up to the selected week
                    const res = await fetch(`/timetracking/api/summary/cumulative-up-to-week?employeeId=${this.selectedEmployeeId}&year=${this.currentYear}&week=${this.currentWeek}`);
                    if (res.ok) {
                        const cumulativeAccount = await res.json();
                        // Store the cumulative balance up to the selected week
                        this.cumulativeHoursAccount = {
                            // Weekly data for the selected week
                            weeklyBalance: cumulativeAccount.currentWeekBalance,
                            weeklyWorkedHours: cumulativeAccount.currentWeekWorkedHours,
                            weeklyExpectedHours: cumulativeAccount.currentWeekExpectedHours,
                            
                            // Cumulative data up to the selected week
                            cumulativeBalance: cumulativeAccount.cumulativeBalance,
                            cumulativeWorkedHours: cumulativeAccount.cumulativeWorkedHours,
                            cumulativeExpectedHours: cumulativeAccount.cumulativeExpectedHours,
                            cumulativeBalanceFormatted: cumulativeAccount.cumulativeBalanceFormatted,
                            
                            // Vacation data (we need to get this from weekly summary)
                            totalVacationDays: 0,
                            usedVacationDays: 0,
                            remainingVacationDays: 0,
                            holidayDays: 0,
                            pendingHolidayRequests: 0
                        };
                        
                        // Load weekly summary to get vacation data
                        const weeklyRes = await fetch(`/timetracking/api/summary/weekly?employeeId=${this.selectedEmployeeId}&year=${this.currentYear}&week=${this.currentWeek}`);
                        if (weeklyRes.ok) {
                            const weeklySummary = await weeklyRes.json();
                            if (weeklySummary && weeklySummary.length > 0) {
                                const summary = weeklySummary[0];
                                this.cumulativeHoursAccount.totalVacationDays = summary.totalVacationDays;
                                this.cumulativeHoursAccount.usedVacationDays = summary.usedVacationDays;
                                this.cumulativeHoursAccount.remainingVacationDays = summary.remainingVacationDays;
                                this.cumulativeHoursAccount.holidayDays = summary.holidayDays;
                                this.cumulativeHoursAccount.pendingHolidayRequests = summary.pendingHolidayRequests;
                            }
                        }
                    }
                } catch (error) {
                    console.error('Error loading cumulative account for saldo calculation:', error);
                    // Set default values to prevent undefined errors
                    this.cumulativeHoursAccount = {
                        weeklyBalance: 0,
                        weeklyWorkedHours: 0,
                        weeklyExpectedHours: 0,
                        cumulativeBalance: 0,
                        cumulativeWorkedHours: 0,
                        cumulativeExpectedHours: 0,
                        cumulativeBalanceFormatted: '+0.0',
                        totalVacationDays: 0,
                        usedVacationDays: 0,
                        remainingVacationDays: 0,
                        holidayDays: 0,
                        pendingHolidayRequests: 0
                    };
                }
            },
            
            async loadMonthlyAccount() {
                if (!this.selectedEmployeeId) return;
                
                try {
                    const res = await fetch(`/timetracking/api/summary/monthly-account?employeeId=${this.selectedEmployeeId}&year=${this.currentYear}`);
                    if (res.ok) {
                        this.monthlyAccount = await res.json();
                    }
                } catch (error) {
                    console.error('Error loading monthly account:', error);
                }
            },
            previousWeek() {
                // Calculate what the previous week would be
                let newWeek = this.currentWeek;
                let newYear = this.currentYear;
                
                if (this.currentWeek === 1) {
                    newWeek = 52;
                    newYear = this.currentYear - 1;
                } else {
                    newWeek = this.currentWeek - 1;
                }
                
                // Check if the new week would be before employee start date
                if (this.selectedEmployeeId && this.employeeStartDate) {
                    const newWeekStart = this.getWeekStartDate(newYear, newWeek);
                    const employeeStartWeek = this.getWeekOfYear(this.employeeStartDate);
                    const employeeStartYear = this.employeeStartDate.getFullYear();
                    
                    if (newYear < employeeStartYear || 
                        (newYear === employeeStartYear && newWeek < employeeStartWeek)) {
                        // Show error message and don't navigate
                        this.showErrorMessage('Diese Woche ist vor dem Eintrittsdatum!');
                        return;
                    }
                }
                
                // If we get here, the navigation is valid
                this.currentWeek = newWeek;
                this.currentYear = newYear;
                this.onWeekChange();
                this.saveState(); // Save state after changing week
            },
            nextWeek() {
                if (this.currentWeek === 52) {
                    this.currentWeek = 1;
                    this.currentYear++;
                } else {
                    this.currentWeek++;
                }
                
                this.onWeekChange();
                this.saveState(); // Save state after changing week
            },
            onWeekChange() {
                this.loadDailySummaries();
                this.loadWeeklySummaries();
                this.loadCumulativeSaldo();
                this.loadMonthlyAccount(); // Load monthly account for consistent saldo
            },
            getDayClass(day) {
                if (!day) return '';
                
                // Check if day is before employee start date
                if (day.isBeforeStartDate) return 'calendar-before-start';
                
                if (!day.summary) return '';
                
                const summary = day.summary;
                
                if (summary.isWeekend) return 'calendar-weekend';
                if (summary.holidayTypeCode || summary.isPublicHoliday) return 'calendar-holiday';
                
                // For approved holidays and public holidays, treat like weekends (0 expected hours)
                const effectiveExpectedHours = ((summary.holidayTypeCode && summary.holidayApproved) || summary.isPublicHoliday) ? 0 : summary.expectedHours;
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
                
                if (summary.holidayTypeCode) {
                    tooltip += `Urlaub: ${this.getHolidayTypeDisplayName(summary.holidayTypeCode)}\n`;
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
                console.log('Employee changed to:', this.selectedEmployeeId);
                localStorage.setItem('calendar-selected-employee', this.selectedEmployeeId);
                
                // Load employee's default workday hours
                if (this.selectedEmployeeId) {
                    this.loadEmployeeDefaultWorkdayHours(this.selectedEmployeeId);
                }
                
                this.loadDailySummaries();
                this.loadWeeklySummaries();
                this.loadCumulativeSaldo();
                this.loadMonthlyAccount(); // Load fresh monthly account data
                this.saveState(); // Save state after changing employee
            },
            loadTimeEntries() {
                this.loadDailySummaries();
                this.loadWeeklySummaries();
            },
            toLocalDateString(date) {
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const day = String(date.getDate()).padStart(2, '0');
                return `${year}-${month}-${day}`;
            },
            getTimeBlockStyle(start, end) {
                // Grid is now dynamic based on timeRangeStart and timeRangeEnd
                if (!start || !end) return {};
                const [sh, sm] = start.split(":").map(Number);
                const [eh, em] = end.split(":").map(Number);
                const startMins = (sh * 60 + sm) - (this.timeRangeStart * 60); // Adjust to current range
                const endMins = (eh * 60 + em) - (this.timeRangeStart * 60);
                const totalMins = this.visibleHours * 60; // Dynamic total hours
                const top = Math.max(0, (startMins / totalMins) * 100);
                const height = Math.max(12, ((endMins - startMins) / totalMins) * 100); // Increased minimum height
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
                    padding: '3px 6px', // Increased padding
                    cursor: 'pointer',
                    overflow: 'hidden',
                    zIndex: 2
                };
            },
            getProjectColor(projectName) {
                if (!projectName) return '#8DA0CB'; // Pastell-Blau als Standard
                
                if (!this.projectColors[projectName]) {
                    // Harmonische Pastelltöne
                    const colors = [
                        '#66C2A5', // Mint-Grün
                        '#FC8D62', // Korallen-Orange
                        '#8DA0CB', // Pastell-Blau
                        '#E78AC3', // Zart-Rosa
                        '#A6D854', // Hell-Gelb-Grün
                        '#FFD92F'  // Creme-Gelb
                    ];
                    
                    // Hash-basierte Farbzuweisung für Konsistenz
                    const hash = projectName.split('').reduce((a, b) => {
                        a = ((a << 5) - a) + b.charCodeAt(0);
                        return a & a;
                    }, 0);
                    const index = Math.abs(hash) % colors.length;
                    
                    // Wenn mehr als 6 Projekte, verwende eine Variation der Grundfarben
                    if (Object.keys(this.projectColors).length >= 6) {
                        // Verwende eine hellere oder dunklere Variation der Grundfarbe
                        const baseColor = colors[index];
                        const variation = Math.floor(Object.keys(this.projectColors).length / 6) % 3;
                        
                        if (variation === 1) {
                            // Hellere Variation (20% heller)
                            this.projectColors[projectName] = this.lightenColor(baseColor, 0.2);
                        } else if (variation === 2) {
                            // Dunklere Variation (20% dunkler)
                            this.projectColors[projectName] = this.darkenColor(baseColor, 0.2);
                        } else {
                            this.projectColors[projectName] = baseColor;
                        }
                    } else {
                        this.projectColors[projectName] = colors[index];
                    }
                }
                
                return this.projectColors[projectName];
            },
            lightenColor(color, amount) {
                const num = parseInt(color.replace("#", ""), 16);
                const amt = Math.round(2.55 * amount * 100);
                const R = (num >> 16) + amt;
                const G = (num >> 8 & 0x00FF) + amt;
                const B = (num & 0x0000FF) + amt;
                return "#" + (0x1000000 + (R < 255 ? R < 1 ? 0 : R : 255) * 0x10000 +
                    (G < 255 ? G < 1 ? 0 : G : 255) * 0x100 +
                    (B < 255 ? B < 1 ? 0 : B : 255)).toString(16).slice(1);
            },
            darkenColor(color, amount) {
                const num = parseInt(color.replace("#", ""), 16);
                const amt = Math.round(2.55 * amount * 100);
                const R = (num >> 16) - amt;
                const G = (num >> 8 & 0x00FF) - amt;
                const B = (num & 0x0000FF) - amt;
                return "#" + (0x1000000 + (R > 255 ? 255 : R < 0 ? 0 : R) * 0x10000 +
                    (G > 255 ? 255 : G < 0 ? 0 : G) * 0x100 +
                    (B > 255 ? 255 : B < 0 ? 0 : B)).toString(16).slice(1);
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
                if (!timeEntries || timeEntries.length === 0) {
                    console.log('No time entries to layout');
                    return [];
                }
                
                // Sort entries by start time
                const sortedEntries = [...timeEntries].sort((a, b) => {
                    const aStart = this.timeToMinutes(a.startTime);
                    const bStart = this.timeToMinutes(b.startTime);
                    return aStart - bStart;
                });
                
                const layout = [];
                const lanes = []; // Track occupied lanes
                
                for (const entry of sortedEntries) {
                    if (!entry || !entry.startTime || !entry.endTime) {
                        console.log('Skipping invalid entry:', entry);
                        continue; // Skip invalid entries
                    }
                    
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
                    
                    const layoutItem = {
                        entry: entry,
                        laneIndex: laneIndex,
                        totalLanes: Math.max(lanes.length, 1)
                    };

                    layout.push(layoutItem);
                }

                return layout;
            },
            timeToMinutes(timeStr) {
                if (!timeStr) return 0;
                const [hours, minutes] = timeStr.split(':').map(Number);
                return hours * 60 + minutes;
            },
            getTimeBlockStyleWithLayout(start, end, projectName, laneIndex, totalLanes) {
                if (!start || !end) return {};
                
                const baseStyle = this.getTimeBlockStyle(start, end);
                const projectColor = this.getProjectColor(projectName);
                
                // Calculate width and position based on lane
                const laneWidth = 100 / Math.max(totalLanes || 1, 1);
                const left = ((laneIndex || 0) * laneWidth) + 2; // 2% margin
                const right = (((totalLanes || 1) - (laneIndex || 0) - 1) * laneWidth) + 2; // 2% margin
                
                return {
                    ...baseStyle,
                    left: left + '%',
                    right: right + '%',
                    '--project-color': projectColor, // Set CSS custom property for color
                    background: projectColor + 'E6', // 90% opacity background
                    border: '1px solid ' + projectColor,
                    color: '#222',
                    padding: '4px 8px', // Increased padding for better readability
                    boxShadow: '0 2px 4px rgba(0,0,0,0.15)', // Add shadow for better visibility
                    zIndex: '10 !important' // High z-index but not extreme
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
            showErrorMessage(message) {
                this.errorMessage = message;
                if (this.errorTimeout) clearTimeout(this.errorTimeout);
                this.errorTimeout = setTimeout(() => {
                    this.errorMessage = '';
                }, 2000);
            },

            selectWeek(week) {
                this.currentWeek = week.weekNumber;
                this.currentYear = week.year;
                this.onWeekChange();
            },
            selectWeekFromPopup(week) {
                // Check if the selected week is before employee start date
                if (this.selectedEmployeeId && this.employeeStartDate && week.isBeforeStartDate) {
                    this.showErrorMessage('Diese Woche ist vor dem Eintrittsdatum!');
                    return;
                }
                
                this.currentWeek = week.weekNumber;
                this.currentYear = week.year;
                this.onWeekChange();
                this.showWeekPicker = false; // Close popup after selection
                this.saveState(); // Save state after changing week
            },
            
            scrollToCurrentWeekInPopup() {
                this.$nextTick(() => {
                    setTimeout(() => {
                        const currentWeekElement = document.querySelector('.week-picker-item.active');
                        if (currentWeekElement) {
                            currentWeekElement.scrollIntoView({ 
                                behavior: 'smooth', 
                                block: 'center' 
                            });
                        }
                    }, 100);
                });
            },

            getWeekPickerItemClass(week) {
                const isCurrentWeek = week.weekNumber === this.currentWeek && week.year === this.currentYear;
                
                let classes = ['week-picker-item'];
                
                // Mark current selected week
                if (isCurrentWeek) {
                    classes.push('active');
                }
                
                // Color coding based on approval status
                if (week.approvalStatus === 'before-start') {
                    classes.push('before-start-week'); // Special styling for weeks before employee start date
                } else if (week.approvalStatus === 'future') {
                    classes.push('future-week'); // Gray for future weeks
                } else if (week.approvalStatus === 'approved') {
                    classes.push('approved'); // Green for approved weeks
                } else if (week.approvalStatus === 'pending') {
                    classes.push('pending'); // Yellow for pending weeks
                } else if (week.approvalStatus === 'rejected') {
                    classes.push('rejected'); // Red for rejected weeks
                } else {
                    classes.push('empty'); // Default for weeks with no data
                }
                
                return classes.join(' ');
            },
            async getWeekData(year, weekNumber) {
                // Get the week start and end dates
                const weekStart = this.getWeekStartDate(year, weekNumber);
                const weekEnd = new Date(weekStart);
                weekEnd.setDate(weekStart.getDate() + 6);
                
                const from = this.toLocalDateString(weekStart);
                const to = this.toLocalDateString(weekEnd);
                
                try {
                    // Fetch daily summaries for this week
                    const response = await fetch(`/timetracking/api/summary/daily?employeeId=${this.selectedEmployeeId}&from=${from}&to=${to}`);
                    if (!response.ok) {
                        return { approvalStatus: 'empty', hasData: false };
                    }
                    
                    const dailySummaries = await response.json();
                    
                    // Check if there are any time entries or holidays in this week
                    let hasTimeEntries = false;
                    let hasHolidays = false;
                    let allTimeEntriesApproved = true;
                    let allHolidaysApproved = true;
                    
                    for (const daySummary of dailySummaries) {
                        // Check for time entries
                        if (daySummary.timeEntries && daySummary.timeEntries.length > 0) {
                            hasTimeEntries = true;
                            // Check if all time entries are approved
                            for (const entry of daySummary.timeEntries) {
                                if (!entry.approval?.approved) {
                                    allTimeEntriesApproved = false;
                                }
                            }
                        }
                        
                        // Check for holidays
                        if (daySummary.holidayType && !daySummary.isPublicHoliday) {
                            hasHolidays = true;
                            if (!daySummary.holidayApproved) {
                                allHolidaysApproved = false;
                            }
                        }
                    }
                    
                    // Determine approval status
                    if (!hasTimeEntries && !hasHolidays) {
                        return { approvalStatus: 'empty', hasData: false };
                    } else if (hasTimeEntries && allTimeEntriesApproved && (!hasHolidays || allHolidaysApproved)) {
                        return { approvalStatus: 'approved', hasData: true };
                    } else {
                        return { approvalStatus: 'pending', hasData: true };
                    }
                    
                } catch (error) {
                    console.error('Error fetching week data:', error);
                    return { approvalStatus: 'empty', hasData: false };
                }
            },
            async loadWeekData() {
                // Load actual data for all weeks
                const weeks = this.allWeeks;
                const today = new Date();
                
                for (let i = 0; i < weeks.length; i++) {
                    const week = weeks[i];
                    const weekEnd = new Date(week.weekEnd);
                    const weekStart = new Date(week.weekStart);
                    
                    // Skip future weeks
                    if (weekStart > today) {
                        continue;
                    }
                    
                    try {
                        const weekData = await this.getWeekData(week.year, week.weekNumber);
                        if (weekData) {
                            // Update the week's approval status
                            week.approvalStatus = weekData.approvalStatus;
                        }
                    } catch (error) {
                        console.error(`Error loading data for week ${week.weekNumber}:`, error);
                    }
                }
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
            },
            goToToday() {
                const today = new Date();
                this.currentYear = today.getFullYear();
                this.currentWeek = this.getWeekOfYear(today);
                this.onWeekChange();
                this.saveState(); // Save state after going to today
            },
            openTimeEntryForm() {
                console.log('Opening NEW time entry form...');
                this.showFormModal = true;
                this.currentEntryData = null; // Clear any existing entry data
                // Wait for the modal to be fully rendered before initializing
                this.$nextTick(() => {
                    setTimeout(() => {
                        this.initializeTimeTrackingForm();
                    }, 100); // Reduced delay for faster response
                });
            },
            
            openEditTimeEntryForm(entryId) {
                console.log('Opening EDIT time entry form for ID:', entryId);
                this.showFormModal = true;
                // Wait for the modal to be fully rendered before initializing
                this.$nextTick(() => {
                    setTimeout(() => {
                        this.initializeTimeTrackingFormWithEntry(entryId);
                    }, 100); // Reduced delay for faster response
                });
            },
            initializeTimeTrackingForm() {
                console.log('Initializing time tracking form...');
                this.formLoaded = false;
                
                // Check if the form script is already loaded
                if (window.timeTrackingFormInitialized) {
                    console.log('Form already initialized, reinitializing...');
                    // Clean up existing form
                    const formContainer = document.getElementById('time-tracking-form-app');
                    if (formContainer) {
                        formContainer.innerHTML = '';
                        if (window.timeTrackingFormApp) {
                            window.timeTrackingFormApp.unmount();
                        }
                    }
                }
                
                // Load the form script if not already loaded
                if (!window.timeTrackingFormScriptLoaded) {
                    console.log('Loading time tracking form script...');
                    const script = document.createElement('script');
                    script.type = 'module';
                    script.src = '/js/time-tracking-form.js';
                    script.onload = () => {
                        console.log('Time tracking form script loaded successfully');
                        window.timeTrackingFormScriptLoaded = true;
                        this.initializeFormAfterScriptLoad();
                    };
                    script.onerror = (error) => {
                        console.error('Failed to load time tracking form script:', error);
                    };
                    document.head.appendChild(script);
                } else {
                    console.log('Form script already loaded, initializing directly...');
                    this.initializeFormAfterScriptLoad();
                }
            },
            
            initializeFormAfterScriptLoad() {
                // Wait for the modal to be fully rendered
                this.$nextTick(() => {
                    setTimeout(() => {
                        // Debug: Check if modal is in DOM
                        const modal = document.querySelector('.modal');
                        console.log('Modal in DOM:', !!modal);
                        if (modal) {
                            console.log('Modal HTML:', modal.outerHTML.substring(0, 500));
                        }
                        
                        const formContainer = document.getElementById('time-tracking-form-app');
                        console.log('Form container found:', !!formContainer);
                        if (formContainer) {
                            console.log('Form container HTML:', formContainer.outerHTML);
                            console.log('Form container dataset:', formContainer.dataset);
                            
                            // Force the form container to be visible so the script can find it
                            formContainer.style.display = 'block';
                            
                            // Ensure the container is properly initialized before mounting Vue
                            if (!formContainer.dataset.entry || !formContainer.dataset.employees) {
                                console.log('Form container not properly initialized, retrying...');
                                setTimeout(() => {
                                    this.initializeFormAfterScriptLoad();
                                }, 50);
                                return;
                            }
                            
                            // Manually trigger the form initialization
                            if (window.timeTrackingFormScriptLoaded) {
                                console.log('Manually triggering form initialization...');
                                // Call the initializeForm function directly
                                if (typeof window.initializeTimeTrackingForm === 'function') {
                                    try {
                                        window.initializeTimeTrackingForm();
                                    } catch (error) {
                                        console.error('Error initializing form:', error);
                                        // Retry after a short delay
                                        setTimeout(() => {
                                            if (typeof window.initializeTimeTrackingForm === 'function') {
                                                window.initializeTimeTrackingForm();
                                            }
                                        }, 100);
                                    }
                                } else {
                                    // If the function doesn't exist, try to load the script again
                                    console.log('Form script not available, loading...');
                                    const script = document.createElement('script');
                                    script.type = 'module';
                                    script.src = '/js/time-tracking-form.js';
                                    script.onload = () => {
                                        console.log('Form script loaded manually');
                                        window.timeTrackingFormScriptLoaded = true;
                                        // Try to initialize again
                                        setTimeout(() => {
                                            if (typeof window.initializeTimeTrackingForm === 'function') {
                                                window.initializeTimeTrackingForm();
                                            }
                                        }, 100);
                                    };
                                    document.head.appendChild(script);
                                }
                            }
                            
                            // Set formLoaded to true after a short delay to ensure the form script can initialize
                            setTimeout(() => {
                                this.formLoaded = true;
                            }, 100);
                        } else {
                            console.error('Form container not found after script load');
                        }
                    }, 100); // Reduced delay for faster initialization
                });
            },
            
            async initializeTimeTrackingFormWithEntry(entryId) {
                console.log('Initializing time tracking form with entry:', entryId);
                this.formLoaded = false;
                
                try {
                    // First, fetch the entry data
                    const response = await fetch(`/timetracking/api/${entryId}`);
                    if (!response.ok) {
                        throw new Error('Failed to fetch entry data');
                    }
                    const entryData = await response.json();
                    console.log('Fetched entry data:', entryData);
                    
                    // Store the entry data in Vue state
                    this.currentEntryData = entryData;
                    
                    // Ensure modal is still open before proceeding
                    if (!this.showFormModal) {
                        console.log('Modal was closed during data fetch, reopening...');
                        this.showFormModal = true;
                    }
                    
                    // Load the form script if not already loaded
                    if (!window.timeTrackingFormScriptLoaded) {
                        console.log('Loading time tracking form script...');
                        const script = document.createElement('script');
                        script.type = 'module';
                        script.src = '/js/time-tracking-form.js';
                        script.onload = () => {
                            console.log('Time tracking form script loaded successfully');
                            window.timeTrackingFormScriptLoaded = true;
                            this.initializeFormAfterScriptLoad();
                        };
                        script.onerror = (error) => {
                            console.error('Failed to load time tracking form script:', error);
                        };
                        document.head.appendChild(script);
                    } else {
                        console.log('Form script already loaded, initializing directly...');
                        this.initializeFormAfterScriptLoad();
                    }
                } catch (error) {
                    console.error('Error initializing form with entry:', error);
                    // Don't close the modal on error, just show an error message
                    this.showSuccessMessage('Fehler beim Laden des Eintrags: ' + error.message);
                }
            },
            closeFormModal() {
                console.log('closeFormModal called from:', new Error().stack);
                this.showFormModal = false;
                this.formLoaded = false;
                // Clean up the form container
                const formContainer = document.getElementById('time-tracking-form-app');
                if (formContainer) {
                    formContainer.innerHTML = '';
                    // Remove any Vue apps that might be mounted
                    if (window.timeTrackingFormApp) {
                        try {
                            window.timeTrackingFormApp.unmount();
                        } catch (e) {
                            console.log('Error unmounting app:', e);
                        }
                        window.timeTrackingFormApp = null;
                    }
                }
            },

            scrollTimeUp() {
                // Expand the time range upward (earlier times)
                if (this.timeRangeStart > 6) {
                    this.timeRangeStart--;
                }
            },
            scrollTimeDown() {
                // Expand the time range downward (later times)
                if (this.timeRangeEnd < 20) {
                    this.timeRangeEnd++;
                }
            },
            // Debug method to check grid lines
            debugGridLines() {
                console.log('Debug Grid Lines:');
                console.log('Visible Hours:', this.visibleHours);
                console.log('Time Range:', this.timeRangeStart, 'to', this.timeRangeEnd);
                
                const lines = this.generateGridLines();
                console.log('Generated Grid Lines:', lines);
                
                lines.forEach(line => {
                    console.log(`${line.type.toUpperCase()} line at ${line.position}% (${line.key})`);
                });
                
                // Also show the old calculation for comparison
                console.log('Old calculation:');
                for (let h = 1; h <= this.visibleHours; h++) {
                    const hourPosition = (h-1) * (100 / (this.visibleHours-1));
                    console.log(`Hour ${h}: ${hourPosition}%`);
                }
            },
            // Generate all grid lines for a day column
            generateGridLines() {
                const lines = [];
                const totalHeight = 100;
                const hourHeight = totalHeight / (this.visibleHours - 1);
                
                // Generate hour lines
                for (let h = 0; h < this.visibleHours; h++) {
                    lines.push({
                        type: 'hour',
                        position: h * hourHeight,
                        key: `hour-${h}`
                    });
                }
                
                // Generate quarter lines (only between hours, not after the last hour)
                for (let h = 0; h < this.visibleHours - 1; h++) {
                    const hourStart = h * hourHeight;
                    lines.push({
                        type: 'quarter',
                        position: hourStart + (hourHeight * 0.25),
                        key: `quarter-1-${h}`
                    });
                    lines.push({
                        type: 'quarter',
                        position: hourStart + (hourHeight * 0.5),
                        key: `quarter-2-${h}`
                    });
                    lines.push({
                        type: 'quarter',
                        position: hourStart + (hourHeight * 0.75),
                        key: `quarter-3-${h}`
                    });
                }
                
                return lines;
            },
            isTimeBlockVisible(startTime, endTime) {
                if (!startTime || !endTime) {
                    console.log('Time block not visible - missing times:', { startTime, endTime });
                    return false;
                }
                
                try {
                    const [sh, sm] = startTime.split(":").map(Number);
                    const [eh, em] = endTime.split(":").map(Number);
                    
                    if (isNaN(sh) || isNaN(sm) || isNaN(eh) || isNaN(em)) {
                        console.log('Time block not visible - invalid time format:', { startTime, endTime });
                        return false;
                    }
                    
                    const startHour = sh + sm / 60;
                    const endHour = eh + em / 60;
                    
                    // Check if the time block overlaps with the visible range
                    // Now we show blocks that are within or overlap the visible range
                    const isVisible = (startHour < this.timeRangeEnd && endHour > this.timeRangeStart);
                    
                    return isVisible;
                } catch (error) {
                    console.warn('Error checking time block visibility:', error);
                    return false;
                }
            },
            getBreakStyle(breakItem) {
                const startMins = this.timeToMinutes(breakItem.start);
                const endMins = this.timeToMinutes(breakItem.end);
                const totalMins = this.visibleHours * 60; // Use dynamic total hours
                const top = Math.max(0, ((startMins - (this.timeRangeStart * 60)) / totalMins) * 100);
                const height = Math.max(2, ((endMins - startMins) / totalMins) * 100);
                
                return {
                    position: 'absolute',
                    left: '0',
                    right: '0',
                    top: top + '%',
                    height: height + '%',
                    background: 'rgba(255, 255, 255, 0.7)',
                    border: '1px dashed #666',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '8px',
                    color: '#666',
                    zIndex: 3
                };
            },
            getTimeEntriesForDayAndHour(dateStr, hour) {
                const dayEntries = this.timeEntries.filter(entry => {
                    const entryDate = new Date(entry.date);
                    const targetDate = new Date(dateStr);
                    return entryDate.toDateString() === targetDate.toDateString();
                });
                
                return dayEntries.filter(entry => {
                    const startHour = parseInt(entry.startTime.split(':')[0]);
                    return startHour === hour;
                }).map(entry => {
                    const layout = this.calculateTimeBlockLayout([entry]);
                    return {
                        ...entry,
                        laneIndex: layout[0]?.laneIndex || 0,
                        totalLanes: layout[0]?.totalLanes || 1
                    };
                });
            },
            /**
             * Load default workday hours for the selected employee
             */
            async loadEmployeeDefaultWorkdayHours(employeeId) {
                if (!employeeId) return;
                
                // Check cache first
                if (this.employeeDefaultWorkdayHours[employeeId]) {
                    this.defaultWorkdayHours = this.employeeDefaultWorkdayHours[employeeId];
                    return;
                }
                
                try {
                    const response = await fetch(`/api/summary/default-workday-hours?employeeId=${employeeId}`);
                    if (response.ok) {
                        const data = await response.json();
                        const hours = data.defaultWorkdayHours;
                        this.employeeDefaultWorkdayHours[employeeId] = hours;
                        this.defaultWorkdayHours = hours;
                        console.log(`Loaded default workday hours for employee ${employeeId}: ${hours}`);
                    } else {
                        console.warn(`Failed to load default workday hours for employee ${employeeId}, using fallback`);
                        this.defaultWorkdayHours = 8.0;
                    }
                } catch (error) {
                    console.error(`Error loading default workday hours for employee ${employeeId}:`, error);
                    this.defaultWorkdayHours = 8.0;
                }
            }
        },
        watch: {
            showFormModal(newVal) {
                console.log('showFormModal watcher triggered:', newVal);
                if (newVal) {
                    // Close week picker popup when modal opens
                    this.showWeekPicker = false;
                    
                    // Modal is opening, check if it's rendered
                    this.$nextTick(() => {
                        setTimeout(() => {
                            const modal = document.querySelector('.modal');
                            console.log('Modal in DOM after watcher:', !!modal);
                            if (modal) {
                                console.log('Modal found in DOM');
                            } else {
                                console.log('Modal not found in DOM');
                            }
                        }, 100);
                    });
                }
            },
            showWeekPicker(newVal) {
                if (newVal && !this.showFormModal) {
                    // Week picker is opening and modal is not open, scroll to current week
                    this.$nextTick(() => {
                        setTimeout(() => {
                            this.scrollToCurrentWeekInPopup();
                        }, 100);
                    });
                }
            }
        },
        mounted() {
            console.log('Calendar app mounted');
            this.loadHolidayTypes().then(() => {
                this.loadDailySummaries();
                this.loadWeeklySummaries();
                this.loadCumulativeSaldo();
                this.loadMonthlyAccount(); // Load monthly account for consistent saldo
                this.loadWeekData(); // Load week data for color coding
                
                // Load employee's default workday hours
                if (this.selectedEmployeeId) {
                    this.loadEmployeeDefaultWorkdayHours(this.selectedEmployeeId);
                }
            });
            
            // Load company settings for expected hours calculation
            this.loadCompanySettings();
            
            // Listen for entry-saved events from the form
            document.addEventListener('entry-saved', (event) => {
                if (event.detail.action === 'close-modal') {
                    this.closeFormModal();
                    if (event.detail.refresh) {
                        this.loadDailySummaries();
                        this.loadWeeklySummaries();
                        this.loadCumulativeSaldo();
                        this.loadMonthlyAccount(); // Reload monthly account after changes
                        this.loadWeekData(); // Reload week data after changes
                    }
                }
            });

            // Restore saved week and employee from localStorage
            const savedWeek = localStorage.getItem('calendar-current-week');
            const savedYear = localStorage.getItem('calendar-current-year');
            const savedEmployeeId = localStorage.getItem('calendar-selected-employee');

            if (savedWeek) {
                this.currentWeek = parseInt(savedWeek);
            }
            if (savedYear) {
                this.currentYear = parseInt(savedYear);
            }
            if (savedEmployeeId) {
                this.selectedEmployeeId = parseInt(savedEmployeeId);
            }
        },
        template: `
        <div class="container-fluid">
            <!-- Success Message -->
            <div v-if="successMessage" class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="bi bi-check-circle me-2"></i>{{ successMessage }}
                <button type="button" class="btn-close" @click="successMessage = ''"></button>
            </div>
            
            <!-- Header Navigation -->
            <div class="d-flex align-items-center justify-content-between mb-3">
                <div class="d-flex align-items-center gap-2">
                    <button @click="goToToday" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-calendar-check"></i> Heute
                    </button>
                    <div class="week-navigation d-flex align-items-center gap-2" style="position: relative;">
                        <!-- Error Toast -->
                        <div v-if="errorMessage" class="toast align-items-center text-bg-danger border-0 show" style="position: absolute; top: -50px; left: 50%; transform: translateX(-50%); z-index: 2000; min-width: 220px;">
                            <div class="d-flex">
                                <div class="toast-body">
                                    <i class="bi bi-exclamation-triangle me-2"></i>{{ errorMessage }}
                                </div>
                            </div>
                        </div>
                        <button @click="previousWeek" class="btn btn-outline-secondary btn-sm">
                            <i class="bi bi-chevron-left"></i>
                        </button>
                        <button @click="showWeekPicker = !showWeekPicker" class="btn btn-outline-primary btn-sm week-display">
                            <i class="bi bi-calendar-week"></i>
                            
                            <!-- Week Picker Popup -->
                            <div v-if="showWeekPicker" class="week-picker-popup">
                                <div class="week-picker-container">
                                    <div class="week-picker-scroll">
                                        <div v-for="week in allWeeks" 
                                             :key="week.weekNumber" 
                                             :class="['week-picker-item', getWeekPickerItemClass(week)]"
                                             @click="selectWeekFromPopup(week)">
                                            <div class="week-number">KW {{ week.weekNumber }}</div>
                                            <div class="week-range">{{ week.shortRange }}</div>
                                        </div>
                                    </div>
                                </div>
                                <!-- Legend -->
                                <div class="mt-3 pt-2 border-top" style="font-size: 11px; color: #666;">
                                    <div class="d-flex justify-content-center gap-3">
                                        <div class="d-flex align-items-center">
                                            <div style="width: 12px; height: 12px; border-radius: 2px; background-color: #d1e7dd; border: 1px solid #198754; margin-right: 4px;"></div>
                                            <span>Genehmigt</span>
                                        </div>
                                        <div class="d-flex align-items-center">
                                            <div style="width: 12px; height: 12px; border-radius: 2px; background-color: #fff3cd; border: 1px solid #ffc107; margin-right: 4px;"></div>
                                            <span>Nicht genehmigt</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </button>
                        <button @click="nextWeek" class="btn btn-outline-secondary btn-sm">
                            <i class="bi bi-chevron-right"></i>
                        </button>
                    </div>
                    <span class="month-year">
                        <span class="month">{{ currentMonthYear }}</span>
                        <span class="week-info"> / KW {{ currentWeek }}</span>
                    </span>
                </div>
                
                <div class="d-flex align-items-center gap-2">
                    <div class="employee-selector">
                        <select v-model="selectedEmployeeId" @change="onEmployeeChange" class="form-select form-select-sm">
                            <option value="">Alle Mitarbeiter</option>
                            <option v-for="employee in employees" :key="employee.id" :value="employee.id">
                                {{ employee.firstName }} {{ employee.lastName }}
                            </option>
                        </select>
                    </div>
                    <button @click="openTimeEntryForm" class="btn btn-primary btn-sm shadow-sm" style="border-radius: 8px; padding: 0.5rem 1rem; font-weight: 500; transition: all 0.2s ease;" title="Neuen Zeiteintrag erstellen">
                        <i class="bi bi-plus-circle me-1"></i> 
                        Zeit erfassen
                    </button>
                    <button @click="debugGridLines" class="btn btn-outline-info btn-sm" title="Debug Grid Lines">
                        <i class="bi bi-bug"></i>
                    </button>
                </div>
            </div>

            <!-- Time Entry Form Modal -->
            <div v-if="showFormModal" class="modal fade show" style="display: block; background-color: rgba(0,0,0,0.5); position: fixed; top: 0; left: 0; width: 100%; height: 100%; z-index: 1050; backdrop-filter: blur(3px);" tabindex="-1" @click="closeFormModal">
                <div class="modal-dialog modal-lg" style="position: relative; z-index: 1051; max-width: 800px; margin: 1.5rem auto;">
                    <div class="modal-content shadow-lg border-0" @click.stop style="border-radius: 12px; overflow: hidden; box-shadow: 0 20px 60px rgba(0,0,0,0.3);">
                        <!-- Single Header -->
                        <div class="modal-header text-white" style="background: linear-gradient(135deg, #495057 0%, #6c757d 100%); border-bottom: none; padding: 1.25rem 1.5rem;">
                            <h5 class="modal-title mb-0" style="font-weight: 600; font-size: 1.1rem;">
                                <i class="bi bi-clock me-2"></i>
                                {{ currentEntryData ? 'Zeiteintrag bearbeiten' : 'Neuer Zeiteintrag' }}
                            </h5>
                            <button type="button" class="btn-close btn-close-white" @click="closeFormModal" style="opacity: 0.9; transition: opacity 0.2s;" onmouseover="this.style.opacity='1'" onmouseout="this.style.opacity='0.9'"></button>
                        </div>
                        
                        <!-- Form Content -->
                        <div class="modal-body p-0" style="max-height: 75vh; overflow-y: auto; background: #f8f9fa;">
                            <div v-if="!formLoaded" class="text-center p-5">
                                <div class="spinner-border text-primary" role="status" style="width: 3rem; height: 3rem;">
                                    <span class="visually-hidden">Lade Formular...</span>
                                </div>
                                <p class="mt-3 text-muted">Formular wird geladen...</p>
                            </div>
                            <div id="time-tracking-form-app" 
                                 :data-entry="currentEntryData ? JSON.stringify(currentEntryData) : '{}'"
                                 :data-employees="JSON.stringify(employees)"
                                 :data-projects="JSON.stringify(projects)"
                                 :data-categories="JSON.stringify(['Arbeit', 'Probleme', 'Material', 'Sonstiges'])"
                                 :data-catalog-items="JSON.stringify([])"
                                 :data-current-date="new Date().toISOString().split('T')[0]"
                                 data-modal="time-entry-modal"
                                 ref="formContainer"
                                 style="padding: 2rem; background: white; margin: 1rem; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                            </div>
                            <script type="module" src="/js/time-tracking-form.js"></script>
                        </div>
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
            <div v-if="!loading">
                <div class="card">
                    
                    <div class="card-body">
                        <!-- Vertical Time Grid -->
                        <div class="calendar-vertical-grid" style="display:flex; flex-direction:column; overflow-x:auto; border-radius: 0.375rem;">
                            <!-- Header Row: Empty cell + day names -->
                            <div style="display:flex;">
                                <div style="width:48px;"></div>
                                <div v-for="(day, i) in calendarDays" :key="'header'+i" style="flex:1; min-width:110px; text-align:left; font-weight:bold; border-bottom:1px solid #dee2e6; padding-bottom:4px; padding-left:8px;">
                                    {{ day.day }} {{ day.dayName }}
                                </div>
                            </div>
                            <div style="display:flex; height:calc({{ visibleHours * 40 }}px);">
                                <!-- Time labels with navigation -->
                                <div class="time-labels" style="width:48px; display:flex; flex-direction:column; align-items:flex-end; position:relative;">
                                    <!-- Time navigation buttons -->
                                    <div style="display:flex; justify-content:center; margin-bottom:5px;">
                                        <button @click="scrollTimeUp" class="btn btn-outline-secondary btn-sm" style="font-size:10px; padding:2px 4px;" :disabled="timeRangeStart <= 6" title="Zeitbereich nach oben erweitern">
                                            <i class="bi bi-chevron-up"></i>
                                        </button>
                                    </div>
                                    <!-- Time labels -->
                                    <div style="flex:1; display:flex; flex-direction:column; align-items:flex-end;">
                                        <div v-for="h in visibleHours" :key="'label'+h" style="height:40px; font-size:12px; color:#666; position:relative; font-weight:500;">
                                            <span style="position:absolute; right:4px; top:-10px;">{{ (timeRangeStart + h - 1).toString().padStart(2,'0') }}:00</span>
                                        </div>
                                    </div>
                                    <!-- Time navigation buttons -->
                                    <div style="display:flex; justify-content:center; margin-top:5px;">
                                        <button @click="scrollTimeDown" class="btn btn-outline-secondary btn-sm" style="font-size:10px; padding:2px 4px;" :disabled="timeRangeEnd >= 20" title="Zeitbereich nach unten erweitern">
                                            <i class="bi bi-chevron-down"></i>
                                        </button>
                                    </div>
                                </div>
                                <!-- Day columns -->
                                <div v-for="(day, dayIdx) in calendarDays" :key="'col'+dayIdx" class="day-column" style="flex:1; min-width:110px; position:relative;">
                                    <!-- Overlay für Tage vor Eintritt -->
                                    <div v-if="day.isBeforeStartDate"
                                         style="position:absolute; top:0; left:0; right:0; bottom:0; background:rgba(220,53,69,0.15); border:2px solid #dc3545; z-index:20; display:flex; align-items:center; justify-content:center; pointer-events:none;">
                                      <span style="color:#dc3545; font-weight:bold; font-size:13px; text-shadow:1px 1px 2px #fff;">Vor Eintritt</span>
                                    </div>
                                    <!-- Grid lines -->
                                    <div v-for="line in generateGridLines()" 
                                         :key="line.key" 
                                         :class="line.type === 'hour' ? 'hour-line' : 'quarter-line'"
                                         :style="{ top: line.position + '%' }">
                                    </div>
                                    <!-- Time entry blocks -->
                                    <template v-for="(layoutItem, index) in calculateTimeBlockLayout(day.summary?.timeEntries || [])" :key="layoutItem?.entry?.id || index">
                                        <div v-if="layoutItem && layoutItem.entry && isTimeBlockVisible(layoutItem.entry.startTime, layoutItem.entry.endTime)"
                                             :style="getTimeBlockStyleWithLayout(layoutItem.entry.startTime, layoutItem.entry.endTime, layoutItem.entry.projectName, layoutItem.laneIndex, layoutItem.totalLanes)"
                                             class="calendar-time-block"
                                             @click="navigateToEdit(layoutItem.entry.id)"
                                             style="position: absolute !important; z-index: 999 !important;"
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
                                        
                                        <div style="font-size:11px; font-weight:bold; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; color: #333; margin-bottom:2px;">
                                            {{ layoutItem.entry.projectName || 'Unbekanntes Projekt' }}
                                        </div>
                                        <div style="font-size:10px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; color: #666; margin-bottom:2px;">
                                            {{ layoutItem.entry.title || 'Arbeit' }}
                                        </div>
                                        <div style="font-size:10px; font-weight:bold; color: #333;">
                                            {{ layoutItem.entry.hoursWorked }}h
                                        </div>
                                        <div v-if="layoutItem.entry.breaks && layoutItem.entry.breaks.length > 0" class="calendar-break-indicator">
                                            {{ layoutItem.entry.breaks.length }} Pause{{ layoutItem.entry.breaks.length > 1 ? 'n' : '' }}
                                        </div>
                                    </div>
                                </template>
                                </div>
                            </div>
                            <!-- Daily Summary Row -->
                            <div style="display:flex; height:60px; border-top:2px solid #dee2e6;">
                                <div style="width:48px;"></div>
                                <div v-for="(day, dayIdx) in calendarDays" :key="'summary'+dayIdx" 
                                     :class="['calendar-day-summary', getDayClass(day)]"
                                     style="flex:1; min-width:110px; border-left:1px solid #eee; padding:8px; display:flex; flex-direction:column; justify-content:center; align-items:center;">
                                    <div v-if="day.summary" style="text-align:center;">
                                        <!-- Weekend: Show hours + "Kein Arbeitstag" -->
                                        <div v-if="day.summary.isWeekend" style="text-align:center;">
                                            <div style="font-size:14px; font-weight:bold; color: #333;">
                                                {{ day.summary.workedHours.toFixed(1) }}h
                                            </div>
                                            <div style="font-size:12px; color: #666;">
                                                Kein Arbeitstag
                                            </div>
                                        </div>
                                        <!-- Holiday: Show holiday type instead of "von X.Xh" -->
                                        <div v-else-if="day.summary.holidayTypeCode || day.summary.isPublicHoliday" style="text-align:center;">
                                            <div style="font-size:14px; font-weight:bold; color: #333;">
                                                {{ (day.summary.workedHours - day.summary.expectedHours) > 0 ? '+' : '' }}{{ (day.summary.workedHours - day.summary.expectedHours).toFixed(1) }}h
                                            </div>
                                            <div v-if="day.summary.holidayTypeCode" style="font-size:10px; color: #0dcaf0; font-weight: 500;">
                                                {{ getHolidayTypeDisplayName(day.summary.holidayTypeCode) }}
                                                <!-- Holiday approval button -->
                                                <button v-if="!day.summary.holidayApproved" 
                                                        @click.stop="approveHoliday(day.summary.holidayId)"
                                                        class="btn btn-success btn-sm ms-1"
                                                        style="font-size: 8px; padding: 1px 4px;">
                                                    <i class="bi bi-check-circle"></i>
                                                </button>
                                            </div>
                                            <div v-if="day.summary.holidayReason" style="font-size:8px; color: #666; font-style: italic; max-width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                                                {{ day.summary.holidayReason }}
                                            </div>
                                            <div v-if="day.summary.isPublicHoliday && day.summary.publicHolidayName" style="font-size:10px; color: #0dcaf0; font-weight: 500;">
                                                {{ day.summary.publicHolidayName }}
                                            </div>
                                        </div>
                                        <!-- Normal workday: Show hours with "von X.Xh" -->
                                        <div v-else style="text-align:center;">
                                            <div style="font-size:14px; font-weight:bold; color: #333;">
                                                {{ day.summary.workedHours.toFixed(1) }}h
                                            </div>
                                            <div style="font-size:10px; color: #666;">
                                                von {{ day.summary.expectedHours.toFixed(1) }}h
                                            </div>
                                            <div v-if="(day.summary.workedHours - day.summary.expectedHours) !== 0" 
                                                 :style="{ fontSize: '9px', color: (day.summary.workedHours - day.summary.expectedHours) > 0 ? '#198754' : '#dc3545' }">
                                                {{ (day.summary.workedHours - day.summary.expectedHours) > 0 ? '+' : '' }}{{ (day.summary.workedHours - day.summary.expectedHours).toFixed(1) }}h
                                            </div>
                                        </div>
                                    </div>
                                    <div v-else style="text-align:center; color: #999; font-size:12px;">
                                        Keine Daten
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Stundenkonto -->
                <div class="stundenkonto-card">
                    <div class="stundenkonto-header">
                        <h5 class="mb-0">Stundenkonto</h5>
                        <small class="text-muted">{{ weekDisplay.fullRange }}</small>
                    </div>
                    <div class="stundenkonto-body">
                        <div v-if="cumulativeHoursAccount" class="stundenkonto-content">
                            <!-- Einheitliches Grid mit 5 Karten -->
                            <div class="metrics-grid">
                                <!-- Aktuelle Woche - Links -->
                                <div class="metric-card primary-metric" 
                                     :aria-label="'Wochensaldo: ' + (cumulativeHoursAccount.weeklyBalance >= 0 ? 'plus' : 'minus') + ' ' + Math.abs(cumulativeHoursAccount.weeklyBalance).toFixed(1) + ' Stunden'">
                                    <div class="metric-icon">
                                        <i class="bi bi-clock-history"></i>
                                    </div>
                                    <div class="metric-title">Wochensaldo</div>
                                    <div :class="['metric-value primary', cumulativeHoursAccount.weeklyBalance >= 0 ? 'positive' : 'negative']">
                                    {{ cumulativeHoursAccount.weeklyBalance >= 0 ? '+' : '' }}{{ cumulativeHoursAccount.weeklyBalance.toFixed(1) }} h
                                </div>
                                    <div class="metric-subtitle">
                                        {{ cumulativeHoursAccount.weeklyWorkedHours.toFixed(1) }} h von {{ cumulativeHoursAccount.weeklyExpectedHours.toFixed(1) }} h Soll
                                </div>
                            </div>
                            
                                <div class="metric-card secondary-metric"
                                     :aria-label="'Gearbeitete Stunden: ' + cumulativeHoursAccount.weeklyWorkedHours.toFixed(1) + ' Stunden diese Woche'">
                                    <div class="metric-icon">
                                        <i class="bi bi-calendar-check"></i>
                                    </div>
                                    <div class="metric-title">Gearbeitete Stunden</div>
                                    <div class="metric-value secondary">
                                        {{ cumulativeHoursAccount.weeklyWorkedHours.toFixed(1) }} h
                                    </div>
                                    <div class="metric-subtitle">
                                        Diese Woche
                                    </div>
                                </div>
                                
                                <!-- Vertikale Trennung -->
                                <div class="vertical-divider"></div>
                                
                                <!-- Kumulativ - Rechts -->
                                <div class="metric-card secondary-metric"
                                     :aria-label="'Stundenkonto: ' + (cumulativeHoursAccount.cumulativeBalance >= 0 ? 'plus' : 'minus') + ' ' + Math.abs(cumulativeHoursAccount.cumulativeBalance).toFixed(1) + ' Stunden kumulativ'">
                                    <div class="metric-icon">
                                        <i class="bi bi-calculator"></i>
                                    </div>
                                    <div class="metric-title">Stundenkonto</div>
                                    <div :class="['metric-value secondary', cumulativeHoursAccount.cumulativeBalance >= 0 ? 'positive' : 'negative']">
                                    {{ cumulativeHoursAccount.cumulativeBalance >= 0 ? '+' : '' }}{{ cumulativeHoursAccount.cumulativeBalance.toFixed(1) }} h
                                </div>
                                    <div class="metric-subtitle">
                                        Kumulativ bis KW {{ currentWeek }}
                                    </div>
                            </div>
                            
                                <div class="metric-card secondary-metric"
                                     :aria-label="'Urlaubstage: ' + cumulativeHoursAccount.remainingVacationDays + ' von ' + cumulativeHoursAccount.totalVacationDays + ' Tagen verbleibend'">
                                    <div class="metric-icon">
                                        <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                                            <!-- Sonne -->
                                            <circle cx="12" cy="8" r="3" fill="#FFD700"/>
                                            <!-- Sonnenstrahlen -->
                                            <path d="M12 2V4M12 20V22M22 12H20M4 12H2M19.8 4.2L18.4 5.6M5.6 18.4L4.2 19.8M19.8 19.8L18.4 18.4M5.6 5.6L4.2 4.2" stroke="#FFD700" stroke-width="1.5" stroke-linecap="round"/>
                                            <!-- Strand/Welle -->
                                            <path d="M2 18C4 16 8 17 12 17C16 17 20 16 22 18" stroke="#fd7e14" stroke-width="2" fill="none" stroke-linecap="round"/>
                                        </svg>
                                        </div>
                                    <div class="metric-title">Urlaubstage</div>
                                    <div class="metric-value secondary">
                                        {{ cumulativeHoursAccount.remainingVacationDays }} / {{ cumulativeHoursAccount.totalVacationDays }}
                                    </div>
                                    <div class="metric-subtitle">
                                        Verbraucht {{ cumulativeHoursAccount.usedVacationDays }}, Rest {{ cumulativeHoursAccount.remainingVacationDays }}
                                    </div>
                                </div>
                                
                                <div class="metric-card secondary-metric"
                                     :aria-label="'Abwesenheitstage: ' + cumulativeHoursAccount.holidayDays + ' Tage, ' + (cumulativeHoursAccount.pendingHolidayRequests > 0 ? cumulativeHoursAccount.pendingHolidayRequests + ' ausstehend' : 'alle genehmigt')">
                                    <div class="metric-icon">
                                        <i class="bi bi-briefcase"></i>
                                        </div>
                                    <div class="metric-title">Abwesenheitstage</div>
                                    <div class="metric-value secondary">
                                        {{ cumulativeHoursAccount.holidayDays }} Tage
                                    </div>
                                    <div class="metric-subtitle">
                                        <span v-if="cumulativeHoursAccount.pendingHolidayRequests > 0" class="status-pending">
                                            {{ cumulativeHoursAccount.pendingHolidayRequests }} ausstehend
                                        </span>
                                        <span v-else class="status-approved">Genehmigt</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div v-else class="text-center p-4">
                            <div class="spinner-border" role="status">
                                <span class="visually-hidden">Lade Stundenkonto...</span>
                            </div>
                            <p class="mt-2">Stundenkonto wird geladen...</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        `
    });

    app.mount('#time-tracking-calendar-app');
});