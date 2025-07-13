import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('absences-app');
    if (!el) {
        console.error('Element #absences-app not found!');
        return;
    }

    const holidays = JSON.parse(el.dataset.holidays || '[]');
    const employees = JSON.parse(el.dataset.employees || '[]');
    const pendingHolidays = JSON.parse(el.dataset.pendingHolidays || '[]');
    const approvedHolidays = JSON.parse(el.dataset.approvedHolidays || '[]');
    const rejectedHolidays = JSON.parse(el.dataset.rejectedHolidays || '[]');
    const employeeStats = JSON.parse(el.dataset.employeeStats || '[]');
    const publicHolidays = JSON.parse(el.dataset.publicHolidays || '[]');
    const currentYear = parseInt(el.dataset.currentYear || '2025');

    const app = createApp({
        data() {
            console.log('Vue app initializing with data:', {
                holidays: holidays.length,
                employees: employees.length,
                publicHolidays: publicHolidays.length
            });
            return {
                holidays,
                employees,
                pendingHolidays,
                approvedHolidays,
                rejectedHolidays,
                employeeStats,
                publicHolidays,
                currentYear,
                selectedEmployee: null,
                successMessage: '',
                showSuccessMessageTimeout: null,
                loading: false
            };
        },
        computed: {
            filteredHolidays() {
                if (!this.selectedEmployee) return this.holidays;
                return this.holidays.filter(h => h.employeeId === this.selectedEmployee);
            },
            yearlyCalendarData() {
                return this.generateYearlyCalendar();
            },
            employeeMatrixData() {
                const matrix = this.generateEmployeeMatrix();
                console.log('Generated matrix:', matrix);
                return matrix;
            },
            weeklyCalendarData() {
                try {
                    console.log('Generating weekly calendar data...');
                    const data = this.generateWeeklyCalendar();
                    console.log('Weekly calendar data generated:', data.length, 'weeks');
                    return data;
                } catch (error) {
                    console.error('Error generating weekly calendar:', error);
                    return [];
                }
            },
            totalEmployees() {
                return this.employeeStats ? this.employeeStats.length : 0;
            },
            totalVacationDays() {
                if (!this.employeeStats) return 0;
                return this.employeeStats.reduce((total, emp) => total + (emp.totalVacationDays || 0), 0);
            },
            usedVacationDays() {
                if (!this.employeeStats) return 0;
                return this.employeeStats.reduce((total, emp) => total + (emp.usedVacationDays || 0), 0);
            },
            pendingRequests() {
                return this.pendingHolidays ? this.pendingHolidays.length : 0;
            },
            monthlyStats() {
                const months = [
                    'Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
                    'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'
                ];

                return months.map((monthName, monthIndex) => {
                    const month = monthIndex + 1;
                    const approvedHolidays = this.approvedHolidays ? 
                        this.approvedHolidays.filter(h => {
                            const holidayMonth = new Date(h.startDate).getMonth() + 1;
                            return holidayMonth === month;
                        }).length : 0;

                    const pendingRequests = this.pendingHolidays ?
                        this.pendingHolidays.filter(h => {
                            const holidayMonth = new Date(h.startDate).getMonth() + 1;
                            return holidayMonth === month;
                        }).length : 0;

                    const publicHolidays = this.publicHolidays ?
                        this.publicHolidays.filter(h => {
                            const holidayMonth = new Date(h.date).getMonth() + 1;
                            return holidayMonth === month;
                        }).length : 0;

                    return {
                        month: month,
                        monthName: monthName,
                        approvedHolidays: approvedHolidays,
                        pendingRequests: pendingRequests,
                        publicHolidays: publicHolidays
                    };
                });
            }
        },
        methods: {
            approveRequest(requestId) {
                this.approveHoliday(requestId);
            },
            rejectRequest(requestId) {
                this.rejectHoliday(requestId);
            },
            formatDate(date) {
                return new Date(date).toLocaleDateString('de-CH');
            },
            calculateWorkingDays(startDate, endDate) {
                const start = new Date(startDate);
                const end = new Date(endDate);
                let workingDays = 0;
                const current = new Date(start);

                while (current <= end) {
                    const dayOfWeek = current.getDay();
                    if (dayOfWeek !== 0 && dayOfWeek !== 6) { // Not Sunday or Saturday
                        workingDays++;
                    }
                    current.setDate(current.getDate() + 1);
                }
                return workingDays;
            },
            async approveHoliday(holidayId) {
                if (!confirm('Möchten Sie diesen Urlaubsantrag genehmigen?')) {
                    return;
                }

                this.loading = true;
                try {
                    const response = await fetch(`/timetracking/api/holidays/${holidayId}/approve`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            approval: {
                                status: 'APPROVED',
                                approverId: 1, // Admin ID
                                approvedAt: new Date().toISOString().split('T')[0]
                            }
                        })
                    });

                    if (response.ok) {
                        this.showSuccessMessage('Urlaubsantrag erfolgreich genehmigt!');
                        // Reload page to update data
                        setTimeout(() => window.location.reload(), 1500);
                    } else {
                        const errorText = await response.text();
                        alert('Fehler beim Genehmigen: ' + errorText);
                    }
                } catch (error) {
                    console.error('Error approving holiday:', error);
                    alert('Fehler beim Genehmigen des Urlaubsantrags');
                } finally {
                    this.loading = false;
                }
            },
            async rejectHoliday(holidayId) {
                if (!confirm('Möchten Sie diesen Urlaubsantrag ablehnen?')) {
                    return;
                }

                this.loading = true;
                try {
                    const response = await fetch(`/timetracking/api/holidays/${holidayId}/approve`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            approval: {
                                status: 'REJECTED',
                                approverId: 1, // Admin ID
                                approvedAt: new Date().toISOString().split('T')[0]
                            }
                        })
                    });

                    if (response.ok) {
                        this.showSuccessMessage('Urlaubsantrag erfolgreich abgelehnt!');
                        // Reload page to update data
                        setTimeout(() => window.location.reload(), 1500);
                    } else {
                        const errorText = await response.text();
                        alert('Fehler beim Ablehnen: ' + errorText);
                    }
                } catch (error) {
                    console.error('Error rejecting holiday:', error);
                    alert('Fehler beim Ablehnen des Urlaubsantrags');
                } finally {
                    this.loading = false;
                }
            },
            showSuccessMessage(message) {
                this.successMessage = message;
                if (this.showSuccessMessageTimeout) {
                    clearTimeout(this.showSuccessMessageTimeout);
                }
                this.showSuccessMessageTimeout = setTimeout(() => {
                    this.successMessage = '';
                }, 3000);
            },
            generateYearlyCalendar() {
                const months = [];
                const monthNames = [
                    'Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
                    'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'
                ];

                for (let month = 0; month < 12; month++) {
                    const monthData = {
                        name: monthNames[month],
                        year: this.currentYear,
                        month: month,
                        weeks: []
                    };

                    // Get first day of month and number of days
                    const firstDay = new Date(this.currentYear, month, 1);
                    const lastDay = new Date(this.currentYear, month + 1, 0);
                    const daysInMonth = lastDay.getDate();
                    const startDayOfWeek = firstDay.getDay();

                    // Generate weeks
                    let currentWeek = [];
                    let dayCount = 1;

                    // Add empty cells for days before month starts
                    for (let i = 0; i < startDayOfWeek; i++) {
                        currentWeek.push({ day: '', type: 'empty', tooltip: '', date: '' });
                    }

                    // Add days of the month
                    for (let day = 1; day <= daysInMonth; day++) {
                        const date = new Date(this.currentYear, month, day);
                        const dateStr = date.toISOString().split('T')[0];

                        // Determine day type
                        let type = 'normal';
                        let tooltip = '';
                        let employees = [];

                        // Check if it's a weekend
                        if (date.getDay() === 0 || date.getDay() === 6) {
                            type = 'weekend';
                        }

                        // Check if it's a public holiday
                        const publicHoliday = this.publicHolidays.find(ph => ph.holidayDate === dateStr);
                        if (publicHoliday) {
                            type = 'public-holiday';
                            tooltip = `Feiertag: ${publicHoliday.name}`;
                        }

                        // Check if any employees are on holiday
                        const dayHolidays = this.holidays.filter(h => {
                            const start = new Date(h.startDate);
                            const end = new Date(h.endDate);
                            return date >= start && date <= end;
                        });

                        if (dayHolidays.length > 0) {
                            const employeeHolidays = dayHolidays.map(h => {
                                const employee = this.employees.find(e => e.id === h.employeeId);
                                return {
                                    name: employee ? `${employee.firstName} ${employee.lastName}` : 'Unbekannt',
                                    status: h.status,
                                    type: h.type
                                };
                            });

                            if (employeeHolidays.length === 1) {
                                const holiday = employeeHolidays[0];
                                if (holiday.status === 'APPROVED') {
                                    type = 'employee-holiday';
                                } else if (holiday.status === 'PENDING') {
                                    type = 'pending-holiday';
                                } else if (holiday.status === 'REJECTED') {
                                    type = 'rejected-holiday';
                                }
                                tooltip = `${holiday.name}: ${holiday.type}`;
                            } else {
                                type = 'multiple-employees';
                                tooltip = employeeHolidays.map(eh => `${eh.name}: ${eh.type}`).join(', ');
                            }
                        }

                        currentWeek.push({
                            day: day,
                            type: type,
                            tooltip: tooltip,
                            date: dateStr
                        });

                        // Start new week if needed
                        if (currentWeek.length === 7) {
                            monthData.weeks.push(currentWeek);
                            currentWeek = [];
                        }
                    }

                    // Add remaining days to complete the last week
                    while (currentWeek.length < 7) {
                        currentWeek.push({ day: '', type: 'empty', tooltip: '', date: '' });
                    }
                    if (currentWeek.length > 0) {
                        monthData.weeks.push(currentWeek);
                    }

                    months.push(monthData);
                }

                return months;
            },
            getDayClass(day) {
                if (!day || day.type === 'empty') return 'day-cell';
                return `day-cell ${day.type}`;
            },
            generateEmployeeMatrix() {
                const matrix = [];

                // Check if data is available
                if (!this.employees || this.employees.length === 0) {
                    return matrix;
                }

                const weeks = this.generateWeeksOfYear();

                // Add header row with week numbers
                const headerRow = {
                    employee: { firstName: 'KW', lastName: '' },
                    weeks: weeks.map(week => ({
                        weekNumber: week.weekNumber,
                        startDate: week.startDate,
                        endDate: week.endDate,
                        type: 'header'
                    }))
                };
                matrix.push(headerRow);

                // Add employee rows
                this.employees.forEach(employee => {
                    const employeeRow = {
                        employee: employee,
                        weeks: weeks.map(week => {
                            const weekData = this.getWeekDataForEmployee(employee.id, week);
                            return {
                                ...weekData,
                                weekNumber: week.weekNumber,
                                startDate: week.startDate,
                                endDate: week.endDate
                            };
                        })
                    };
                    matrix.push(employeeRow);
                });

                return matrix;
            },
            generateWeeksOfYear() {
                const weeks = [];
                const startDate = new Date(this.currentYear, 0, 1); // January 1st
                const endDate = new Date(this.currentYear, 11, 31); // December 31st

                let currentDate = new Date(startDate);

                // Find the first Monday of the year
                while (currentDate.getDay() !== 1) { // 1 = Monday
                    currentDate.setDate(currentDate.getDate() + 1);
                }

                let weekNumber = 1;

                while (currentDate <= endDate) {
                    const weekStart = new Date(currentDate);
                    const weekEnd = new Date(currentDate);
                    weekEnd.setDate(weekEnd.getDate() + 6);

                    weeks.push({
                        weekNumber: weekNumber,
                        startDate: weekStart.toISOString().split('T')[0],
                        endDate: weekEnd.toISOString().split('T')[0]
                    });

                    currentDate.setDate(currentDate.getDate() + 7);
                    weekNumber++;
                }

                return weeks;
            },
            getWeekDataForEmployee(employeeId, week) {
                const weekStart = new Date(week.startDate);
                const weekEnd = new Date(week.endDate);

                // Get employee holidays for this week
                const employeeHolidays = this.holidays.filter(h =>
                    h.employeeId === employeeId &&
                    this.datesOverlap(
                        new Date(h.startDate),
                        new Date(h.endDate),
                        weekStart,
                        weekEnd
                    )
                );

                // Get public holidays for this week
                const publicHolidays = this.publicHolidays.filter(ph => {
                    const holidayDate = new Date(ph.holidayDate);
                    return holidayDate >= weekStart && holidayDate <= weekEnd;
                });

                // Determine week status
                let status = 'normal';
                let tooltip = '';
                let holidayDays = 0;
                let publicHolidayDays = 0;

                if (employeeHolidays.length > 0) {
                    const approvedHolidays = employeeHolidays.filter(h => h.status === 'APPROVED');
                    const pendingHolidays = employeeHolidays.filter(h => h.status === 'PENDING');

                    if (approvedHolidays.length > 0) {
                        status = 'approved-holiday';
                        holidayDays = this.calculateOverlappingDays(approvedHolidays, weekStart, weekEnd);
                        tooltip = `Genehmigter Urlaub: ${approvedHolidays.map(h => h.type).join(', ')}`;
                    } else if (pendingHolidays.length > 0) {
                        status = 'pending-holiday';
                        holidayDays = this.calculateOverlappingDays(pendingHolidays, weekStart, weekEnd);
                        tooltip = `Ausstehender Urlaub: ${pendingHolidays.map(h => h.type).join(', ')}`;
                    }
                }

                if (publicHolidays.length > 0) {
                    if (status === 'normal') {
                        status = 'public-holiday';
                    } else {
                        status = 'mixed-holiday';
                    }
                    publicHolidayDays = publicHolidays.length;
                    tooltip += tooltip ? '\n' : '';
                    tooltip += `Feiertage: ${publicHolidays.map(ph => ph.name).join(', ')}`;
                }

                return {
                    status: status,
                    tooltip: tooltip,
                    holidayDays: holidayDays,
                    publicHolidayDays: publicHolidayDays,
                    totalDays: holidayDays + publicHolidayDays
                };
            },
            datesOverlap(start1, end1, start2, end2) {
                return start1 <= end2 && start2 <= end1;
            },
            calculateOverlappingDays(holidays, weekStart, weekEnd) {
                let totalDays = 0;

                holidays.forEach(holiday => {
                    const holidayStart = new Date(holiday.startDate);
                    const holidayEnd = new Date(holiday.endDate);

                    const overlapStart = new Date(Math.max(holidayStart.getTime(), weekStart.getTime()));
                    const overlapEnd = new Date(Math.min(holidayEnd.getTime(), weekEnd.getTime()));

                    if (overlapStart <= overlapEnd) {
                        const days = Math.floor((overlapEnd - overlapStart) / (1000 * 60 * 60 * 24)) + 1;
                        totalDays += days;
                    }
                });

                return totalDays;
            },
            getWeekCellClass(week) {
                if (week.type === 'header') return 'week-header-cell';

                const classes = ['week-cell'];

                switch (week.status) {
                    case 'approved-holiday':
                        classes.push('approved-holiday');
                        break;
                    case 'pending-holiday':
                        classes.push('pending-holiday');
                        break;
                    case 'public-holiday':
                        classes.push('public-holiday');
                        break;
                    case 'mixed-holiday':
                        classes.push('mixed-holiday');
                        break;
                    default:
                        classes.push('normal-week');
                }

                return classes.join(' ');
            },
            getMonthName(month) {
                const monthNames = [
                    'Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
                    'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'
                ];
                return monthNames[month];
            },
            getDaysInMonth(month, year) {
                return new Date(year, month + 1, 0).getDate();
            },
            getDayClasses(day) {
                if (!day || day.type === 'empty') return 'day-cell';
                return `day-cell ${day.type}`;
            },
            getDayCellClasses(day) {
                if (!day || day.type === 'empty') return '';
                return `day-cell ${day.type}`;
            },
            getHolidayBarClasses(day) {
                if (!day || day.type !== 'employee-holiday' && day.type !== 'pending-holiday' && day.type !== 'rejected-holiday') {
                    return '';
                }
                return `holiday-bar ${day.type}`;
            },
            generateWeeklyCalendar() {
                const weeks = [];

                // Check if data is available
                if (!this.employees || this.employees.length === 0) {
                    return weeks;
                }

                const employeeColors = [
                    '#974E4B', '#2D4262', '#363237', '#F4CCA3', '#3F4751',
                    '#A499A3', '#73605B', '#C4573B', '#D09683'
                ];

                // Generate all weeks of the year using ISO week calculation
                const yearStart = new Date(this.currentYear, 0, 1);
                const yearEnd = new Date(this.currentYear, 11, 31);

                // Find the first Monday of the year (ISO week 1)
                let firstMonday = new Date(yearStart);
                while (firstMonday.getDay() !== 1) { // 1 = Monday
                    firstMonday.setDate(firstMonday.getDate() + 1);
                }

                // Adjust to ISO week 1 (week containing January 4th)
                const jan4 = new Date(this.currentYear, 0, 4);
                const jan4Day = jan4.getDay();
                const daysToAdd = jan4Day === 0 ? 1 : 8 - jan4Day;
                const isoWeek1Start = new Date(jan4);
                isoWeek1Start.setDate(jan4.getDate() + daysToAdd - 7);

                let currentDate = new Date(isoWeek1Start);
                let weekNumber = 1;

                while (currentDate <= yearEnd) {
                    const weekStart = new Date(currentDate);
                    const weekEnd = new Date(currentDate);
                    weekEnd.setDate(weekEnd.getDate() + 6);

                    const week = {
                        weekNumber: weekNumber,
                        startDate: weekStart.toISOString().split('T')[0],
                        endDate: weekEnd.toISOString().split('T')[0],
                        days: []
                    };

                    // Generate days for this week (Monday to Sunday)
                    for (let i = 0; i < 7; i++) {
                        const dayDate = new Date(weekStart);
                        dayDate.setDate(weekStart.getDate() + i);
                        const dateStr = dayDate.toISOString().split('T')[0];

                        // Check if it's a weekend (Saturday = 6, Sunday = 0)
                        const isWeekend = dayDate.getDay() === 0 || dayDate.getDay() === 6;

                        // Check if it's a public holiday - fix date parsing
                        const publicHoliday = this.publicHolidays ? this.publicHolidays.find(ph => {
                            // Parse the holiday date properly
                            const holidayDate = new Date(ph.holidayDate + 'T00:00:00');
                            const dayDateOnly = new Date(dayDate.getFullYear(), dayDate.getMonth(), dayDate.getDate());
                            return holidayDate.getTime() === dayDateOnly.getTime();
                        }) : null;

                        // Get holidays for this day
                        const dayHolidays = this.holidays ? this.holidays.filter(h => {
                            const start = new Date(h.startDate);
                            const end = new Date(h.endDate);
                            return dayDate >= start && dayDate <= end;
                        }).map(h => {
                            const employee = this.employees.find(e => e.id === h.employeeId);
                            return {
                                id: h.id,
                                employeeId: h.employeeId,
                                employeeName: employee ? `${employee.firstName} ${employee.lastName}` : 'Unbekannt',
                                status: h.status,
                                reason: h.type || 'Urlaub',
                                color: employeeColors[h.employeeId % employeeColors.length],
                                startDate: h.startDate,
                                endDate: h.endDate
                            };
                        }) : [];

                        week.days.push({
                            date: dateStr,
                            dayNumber: dayDate.getDate(),
                            dayName: ['SO', 'MO', 'DI', 'MI', 'DO', 'FR', 'SA'][dayDate.getDay()],
                            isWeekend: isWeekend,
                            publicHoliday: publicHoliday ? publicHoliday.name : null,
                            employeeHolidays: dayHolidays
                        });
                    }

                    weeks.push(week);
                    currentDate.setDate(currentDate.getDate() + 7);
                    weekNumber++;
                }

                return weeks;
            },
            getEmployeeColor(employeeId) {
                const colors = [
                    // '#363237', '#2D4262', '#73605B', '#D09683',
                    // '#F4CCA3', '#C4573B', '#974E4B', '#3F4751', '#A499A3'

                    '#974E4B', '#2D4262', '#363237', '#F4CCA3', '#3F4751',
                    '#A499A3', '#73605B', '#C4573B', '#D09683'
                ];
                return colors[employeeId % colors.length];
            },
            getHolidayBarStyle(holiday) {
                // Use employee color for the background
                const employeeColor = this.getEmployeeColor(holiday.employeeId);

                // Add opacity for pending status
                const opacity = holiday.status === 'PENDING' ? 0.7 : 1;

                return {
                    backgroundColor: employeeColor,
                    opacity: opacity,
                    color: 'white',
                    fontSize: '10px',
                    padding: '4px 8px',
                    borderRadius: '3px',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis'
                };
            },
            getHolidayTooltip(holiday) {
                return `${holiday.employeeName}: ${holiday.type} (${holiday.status === 'APPROVED' ? 'Genehmigt' : 'Ausstehend'})`;
            },
            getDayTooltip(day) {
                let tooltip = `Datum: ${this.formatDate(day.date)}`;
                if (day.isWeekend) tooltip += '\nWochenende';
                if (day.publicHoliday) tooltip += `\nFeiertag: ${day.publicHoliday}`;
                if (day.employeeHolidays && day.employeeHolidays.length > 0) {
                    tooltip += '\nUrlaube:';
                    day.employeeHolidays.forEach(h => {
                        tooltip += `\n- ${h.employeeName}: ${h.reason}`;
                    });
                }
                return tooltip;
            },
            // Get month for a specific week
            getMonthForWeek(week) {
                if (!week.days || week.days.length === 0) return null;

                // Use the first day of the week to determine the month
                const firstDay = new Date(week.days[0].date);
                const monthNames = [
                    'Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
                    'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'
                ];

                return monthNames[firstDay.getMonth()];
            },

            // Get CSS class for month indicator
            getMonthClass(monthName) {
                if (!monthName) return '';

                const monthMap = {
                    'Januar': 'january',
                    'Februar': 'february',
                    'März': 'march',
                    'April': 'april',
                    'Mai': 'may',
                    'Juni': 'june',
                    'Juli': 'july',
                    'August': 'august',
                    'September': 'september',
                    'Oktober': 'october',
                    'November': 'november',
                    'Dezember': 'december'
                };

                return monthMap[monthName] || '';
            },

            // Check if month indicator should be shown for this week
            shouldShowMonthIndicator(week, weekIndex) {
                if (weekIndex === 0) return true; // Always show for first week

                if (!week.days || week.days.length === 0) return false;

                const currentWeek = new Date(week.days[0].date);
                const currentMonth = currentWeek.getMonth();

                // Check if any day in this week belongs to a new month
                for (let day of week.days) {
                    const dayDate = new Date(day.date);
                    const dayMonth = dayDate.getMonth();

                    // If this is a weekday (Monday-Friday) and it's a new month, show indicator
                    const dayOfWeek = dayDate.getDay();
                    if (dayOfWeek >= 1 && dayOfWeek <= 5) { // Monday = 1, Friday = 5
                        if (dayMonth !== currentMonth) {
                            return true;
                        }
                    }
                }

                // Also show if this is the first week of a new month
                const firstDayOfWeek = new Date(currentWeek);
                if (firstDayOfWeek.getDate() <= 7) {
                    return true;
                }

                return false;
            }
        }
    });

    app.mount('#absences-app');
    console.log('Vue app mounted successfully');
});