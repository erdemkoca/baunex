// Modern Absences Management JavaScript
// This script generates all UI (tabs, employee stats, calendar, floating button) dynamically.
document.addEventListener('DOMContentLoaded', function() {
    if (!window.location.pathname.includes('/timetracking/absences')) return;

    const appElement = document.getElementById('absences-app');
    if (!appElement) {
        console.error('App element not found');
        return;
    }

    // Parse data from data attributes
    let appState = {
        holidays: [],
        employees: [],
        pendingHolidays: [],
        approvedHolidays: [],
        rejectedHolidays: [],
        employeeStats: [],
        publicHolidays: [],
        currentYear: new Date().getFullYear(),
        currentMonth: new Date().getMonth(),
        employeeColors: {}
    };

    try {
        appState.holidays = JSON.parse(appElement.dataset.holidays || '[]');
        appState.employees = JSON.parse(appElement.dataset.employees || '[]');
        appState.pendingHolidays = JSON.parse(appElement.dataset.pendingHolidays || '[]');
        appState.approvedHolidays = JSON.parse(appElement.dataset.approvedHolidays || '[]');
        appState.rejectedHolidays = JSON.parse(appElement.dataset.rejectedHolidays || '[]');
        appState.employeeStats = JSON.parse(appElement.dataset.employeeStats || '[]');
        appState.publicHolidays = JSON.parse(appElement.dataset.publicHolidays || '[]');
        appState.currentYear = parseInt(appElement.dataset.currentYear) || new Date().getFullYear();
        
        console.log('Data loaded:', {
            holidays: appState.holidays.length,
            employees: appState.employees.length,
            pending: appState.pendingHolidays.length,
            approved: appState.approvedHolidays.length,
            rejected: appState.rejectedHolidays.length,
            stats: appState.employeeStats.length,
            publicHolidays: appState.publicHolidays.length
        });
    } catch (error) {
        console.error('Error parsing template data:', error);
    }

    // Generate employee colors
    function generateEmployeeColors() {
        const approvedHolidays = appState.approvedHolidays;
        const employeeIds = [...new Set(approvedHolidays.map(h => h.employeeId))];
        
        // Predefined colors for good contrast
        const colors = [
            '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7',
            '#DDA0DD', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9',
            '#F8C471', '#82E0AA', '#F1948A', '#85C1E9', '#D7BDE2'
        ];
        
        employeeIds.forEach((employeeId, index) => {
            appState.employeeColors[employeeId] = colors[index % colors.length];
        });
    }

    // Render employee legend
    function renderEmployeeLegend() {
        const legendContainer = document.getElementById('employee-legend');
        if (!legendContainer) return;

        const approvedHolidays = appState.approvedHolidays;
        const uniqueEmployees = [...new Set(approvedHolidays.map(h => h.employeeId))];
        
        if (uniqueEmployees.length === 0) {
            legendContainer.innerHTML = '<div class="no-employees">Keine genehmigten Urlaube vorhanden</div>';
            return;
        }

        const legendItems = uniqueEmployees.map(employeeId => {
            const employee = appState.employees.find(e => e.id === employeeId);
            
            // Use employeeName from holiday data as fallback, or construct from employee data
            let employeeName = 'Unbekannt';
            if (employee) {
                employeeName = `${employee.firstName} ${employee.lastName}`;
            } else {
                // Try to find the holiday with this employeeId to get the employeeName
                const holiday = approvedHolidays.find(h => h.employeeId === employeeId);
                if (holiday && holiday.employeeName) {
                    employeeName = holiday.employeeName;
                }
            }
            
            const color = appState.employeeColors[employeeId];
            
            return `
                <div class="legend-item">
                    <div class="legend-color" style="background-color: ${color}"></div>
                    <span class="legend-name">${employeeName}</span>
                </div>
            `;
        }).join('');

        legendContainer.innerHTML = `
            <div class="legend-container">
                <h4>Mitarbeiter</h4>
                <div class="legend-items">
                    ${legendItems}
                </div>
            </div>
        `;
    }

    // --- UI Generation ---
    appElement.innerHTML = `
        <!-- Sticky Holiday Requests Tabs -->
        <div class="sticky-tabs">
            <div class="container-fluid">
                <ul class="nav nav-tabs" role="tablist">
                    <li class="nav-item">
                        <a class="nav-link active" href="#pending-tab" data-bs-toggle="tab" role="tab">
                            🕒 Offen <span class="badge bg-warning text-dark" id="pending-count">0</span>
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="#approved-tab" data-bs-toggle="tab" role="tab">
                            ✅ Genehmigt <span class="badge bg-success" id="approved-count">0</span>
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="#rejected-tab" data-bs-toggle="tab" role="tab">
                            ❌ Abgelehnt <span class="badge bg-danger" id="rejected-count">0</span>
                        </a>
                    </li>
                </ul>
            </div>
        </div>

        <!-- Tab Content -->
        <div class="tab-content">
            <!-- Pending Tab -->
            <div class="tab-pane fade show active" id="pending-tab" role="tabpanel">
                <div id="pending-requests"></div>
                <div id="no-pending" class="text-center py-5" style="display: none;">
                    <div class="text-muted">
                        <i class="bi bi-check-circle" style="font-size: 3rem;"></i>
                        <h4 class="mt-3">Keine ausstehenden Anfragen</h4>
                        <p>Alle Urlaubsanträge wurden bearbeitet.</p>
                    </div>
                </div>
            </div>

            <!-- Approved Tab -->
            <div class="tab-pane fade" id="approved-tab" role="tabpanel">
                <div id="approved-requests"></div>
                <div id="no-approved" class="text-center py-5" style="display: none;">
                    <div class="text-muted">
                        <i class="bi bi-calendar-check" style="font-size: 3rem;"></i>
                        <h4 class="mt-3">Keine genehmigten Urlaube</h4>
                        <p>Noch keine Urlaubsanträge genehmigt.</p>
                    </div>
                </div>
            </div>

            <!-- Rejected Tab -->
            <div class="tab-pane fade" id="rejected-tab" role="tabpanel">
                <div id="rejected-requests"></div>
                <div id="no-rejected" class="text-center py-5" style="display: none;">
                    <div class="text-muted">
                        <i class="bi bi-calendar-x" style="font-size: 3rem;"></i>
                        <h4 class="mt-3">Keine abgelehnten Anfragen</h4>
                        <p>Alle Urlaubsanträge wurden genehmigt.</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Employee Accounts Table -->
        <div class="container-fluid">
            <div class="employee-accounts-section">
                <div class="section-header">
                    <h2>Mitarbeiter-Urlaubskonten</h2>
                </div>
                <div class="search-filter">
                    <div class="row">
                        <div class="col-md-6">
                            <input type="text" class="form-control" id="employee-search" placeholder="Filter nach Name...">
                        </div>
                        <div class="col-md-6 text-end">
                            <button class="btn btn-outline-secondary btn-sm" id="sort-remaining">
                                <i class="bi bi-sort-down"></i> Nach Verbleibend sortieren
                            </button>
                        </div>
                    </div>
                </div>
                <div class="table-responsive">
                    <table class="employee-table" id="employee-table">
                        <thead>
                            <tr>
                                <th>Mitarbeiter</th>
                                <th>Gesamttage</th>
                                <th>Genommen</th>
                                <th>Ausstehend</th>
                                <th>Verbleibend</th>
                                <th>Fortschritt</th>
                            </tr>
                        </thead>
                        <tbody id="employee-table-body">
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Calendar Section -->
        <div class="container-fluid">
            <div class="calendar-section">
                <div class="section-header">
                    <h2>Urlaubs-Kalender ${appState.currentYear}</h2>
                </div>
                <div class="calendar-navigation">
                    <div class="month-navigation">
                        <button class="btn btn-outline-secondary btn-sm" id="prev-month">
                            <i class="bi bi-chevron-left"></i>
                        </button>
                        <span class="current-month" id="current-month">Januar ${appState.currentYear}</span>
                        <button class="btn btn-outline-secondary btn-sm" id="next-month">
                            <i class="bi bi-chevron-right"></i>
                        </button>
                    </div>
                </div>
                <div class="employee-legend" id="employee-legend"></div>
                <div class="month-chips" id="month-chips"></div>
                <div class="calendar-grid" id="calendar-grid"></div>
            </div>
        </div>

        <!-- Floating Add Button -->
        <button class="add-absence-btn" id="add-absence-btn" title="Neue Abwesenheit erfassen">
            <i class="bi bi-plus"></i>
        </button>
    `;

    // --- Tab Rendering ---
    function renderRequestTab(containerId, requests, status) {
        const container = document.getElementById(containerId);
        const noDataElement = document.getElementById(`no-${status}`);
        const tabContent = document.querySelector('.tab-content');
        
        if (!container) return;

        if (requests.length === 0) {
            container.innerHTML = '';
            if (noDataElement) noDataElement.style.display = 'block';
            
            // Reduce tab-content height when no requests
            if (tabContent) {
                tabContent.style.minHeight = '150px';
                tabContent.style.maxHeight = '200px';
            }
            return;
        }

        if (noDataElement) noDataElement.style.display = 'none';
        
        // Restore normal tab-content height when there are requests
        if (tabContent) {
            tabContent.style.minHeight = '200px';
            tabContent.style.maxHeight = 'calc(100vh - 300px)';
        }

        const cardsHtml = requests.map(holiday => createHolidayCard(holiday, status)).join('');
        container.innerHTML = cardsHtml;
    }

    function createHolidayCard(holiday, status) {
        // Use employeeName from holiday data if available, otherwise fallback to employees list
        let employeeName = holiday.employeeName;
        if (!employeeName || employeeName.trim() === '') {
            const employee = appState.employees.find(emp => emp.id === holiday.employeeId);
            employeeName = employee ? `${employee.firstName} ${employee.lastName}` : 'Unbekannter Mitarbeiter';
        }
        
        const startDate = new Date(holiday.startDate).toLocaleDateString('de-DE');
        const endDate = new Date(holiday.endDate).toLocaleDateString('de-DE');
        const dateRange = startDate === endDate ? startDate : `${startDate} - ${endDate}`;
        
        const typeBadgeClass = getHolidayTypeBadgeClass(holiday.type);
        const typeLabel = getHolidayTypeLabel(holiday.type);
        
        const actionsHtml = status === 'pending' ? `
            <div class="request-actions">
                <button class="btn btn-success btn-sm" onclick="approveHoliday(${holiday.id})">
                    <i class="bi bi-check"></i> Genehmigen
                </button>
                <button class="btn btn-danger btn-sm" onclick="rejectHoliday(${holiday.id})">
                    <i class="bi bi-x"></i> Ablehnen
                </button>
            </div>
        ` : '';

        return `
            <div class="holiday-request-card ${status}">
                <div class="request-header">
                    <div class="request-info">
                        <h5>${employeeName}</h5>
                        <div class="request-dates">
                            <i class="bi bi-calendar"></i> ${dateRange}
                        </div>
                    </div>
                    <span class="holiday-type-badge ${typeBadgeClass}">${typeLabel}</span>
                </div>
                ${holiday.reason ? `<div class="request-reason">${holiday.reason}</div>` : ''}
                ${actionsHtml}
            </div>
        `;
    }

    function getHolidayTypeBadgeClass(type) {
        const typeMap = {
            'Urlaub': 'vacation',
            'Unbezahlt': 'unpaid',
            'Krank': 'sick',
            'Sonderurlaub': 'special',
            'PAID_VACATION': 'vacation',
            'UNPAID_VACATION': 'unpaid',
            'SICK_LEAVE': 'sick',
            'SPECIAL_LEAVE': 'special'
        };
        return typeMap[type] || 'vacation';
    }

    function getHolidayTypeLabel(type) {
        const typeMap = {
            'Urlaub': 'Urlaub',
            'Unbezahlt': 'Unbezahlt',
            'Krank': 'Krank',
            'Sonderurlaub': 'Sonderurlaub',
            'PAID_VACATION': 'Urlaub',
            'UNPAID_VACATION': 'Unbezahlt',
            'SICK_LEAVE': 'Krank',
            'SPECIAL_LEAVE': 'Sonderurlaub'
        };
        return typeMap[type] || 'Urlaub';
    }

    function updateTabCounts() {
        document.getElementById('pending-count').textContent = appState.pendingHolidays.length;
        document.getElementById('approved-count').textContent = appState.approvedHolidays.length;
        document.getElementById('rejected-count').textContent = appState.rejectedHolidays.length;
    }

    renderRequestTab('pending-requests', appState.pendingHolidays, 'pending');
    renderRequestTab('approved-requests', appState.approvedHolidays, 'approved');
    renderRequestTab('rejected-requests', appState.rejectedHolidays, 'rejected');
    updateTabCounts();

    // --- Employee Accounts Table ---
    function renderEmployeeAccounts() {
        const tbody = document.getElementById('employee-table-body');
        if (!tbody) return;

        const rowsHtml = appState.employeeStats.map(stat => {
            const employee = stat.employee || {};
            const employeeName = employee.firstName && employee.lastName ? 
                `${employee.firstName} ${employee.lastName}` : 'Unbekannter Mitarbeiter';
            
            const totalDays = employee.vacationDays || 0;
            const takenDays = stat.approvedDays || 0;
            const pendingDays = stat.pendingDays || 0;
            const remainingDays = totalDays - takenDays - pendingDays;
            const progressPercent = totalDays > 0 ? Math.round((takenDays / totalDays) * 100) : 0;

            return `
                <tr>
                    <td><strong>${employeeName}</strong></td>
                    <td>${totalDays}</td>
                    <td>${takenDays}</td>
                    <td>${pendingDays}</td>
                    <td><strong>${remainingDays}</strong></td>
                    <td>
                        <div class="vacation-progress">
                            <div class="progress-bar">
                                <div class="progress-fill" style="width: ${progressPercent}%"></div>
                            </div>
                            <span class="progress-text">${progressPercent}%</span>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        tbody.innerHTML = rowsHtml;
    }

    renderEmployeeAccounts();

    // --- Calendar Section ---
    function renderMonthChips() {
        const container = document.getElementById('month-chips');
        if (!container) return;

        const months = [
            'Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun',
            'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'
        ];

        const chipsHtml = months.map((month, index) => `
            <span class="month-chip ${index === appState.currentMonth ? 'active' : ''}" 
                  onclick="selectMonth(${index})">
                ${month}
            </span>
        `).join('');

        container.innerHTML = chipsHtml;
    }

    function renderCalendarGrid() {
        const container = document.getElementById('calendar-grid');
        if (!container) return;

        const year = appState.currentYear;
        const month = appState.currentMonth;
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const startDate = new Date(firstDay);
        startDate.setDate(startDate.getDate() - firstDay.getDay());

        // Header
        const headerHtml = `
            <div class="calendar-header">
                <div class="calendar-day-header">Mo</div>
                <div class="calendar-day-header">Di</div>
                <div class="calendar-day-header">Mi</div>
                <div class="calendar-day-header">Do</div>
                <div class="calendar-day-header">Fr</div>
                <div class="calendar-day-header">Sa</div>
                <div class="calendar-day-header">So</div>
            </div>
        `;

        // Body
        const bodyHtml = `
            <div class="calendar-body">
                ${generateCalendarDays(startDate, lastDay, year, month)}
            </div>
        `;

        container.innerHTML = headerHtml + bodyHtml;
        updateCurrentMonthDisplay();
    }

    function generateCalendarDays(startDate, lastDay, year, month) {
        const days = [];
        const currentDate = new Date(startDate);

        while (currentDate <= lastDay || currentDate.getDay() !== 0) {
            const dayNumber = currentDate.getDate();
            const isCurrentMonth = currentDate.getMonth() === month;
            const isWeekend = currentDate.getDay() === 0 || currentDate.getDay() === 6;
            const isToday = currentDate.toDateString() === new Date().toDateString();
            
            const events = getEventsForDate(currentDate);
            const hasEvents = events.length > 0;

            let dayClass = 'calendar-day';
            if (!isCurrentMonth) dayClass += ' empty';
            if (isWeekend) dayClass += ' weekend';
            if (isToday) dayClass += ' today';
            if (hasEvents) dayClass += ' has-events';

            const eventsHtml = events.map(event => {
                let barClass = 'holiday-bar';
                if (event.isStart) barClass += ' start';
                if (event.isEnd) barClass += ' end';
                
                return `
                    <div class="${barClass}" 
                         style="background-color: ${event.color};"
                         title="${event.tooltip}"
                         data-employee-id="${event.employeeId}"
                         data-holiday-id="${event.holidayId}">
                        ${event.isStart ? event.employeeName : ''}
                    </div>
                `;
            }).join('');

            days.push(`
                <div class="${dayClass}" data-date="${currentDate.toISOString().split('T')[0]}">
                    <div class="day-number">${isCurrentMonth ? dayNumber : ''}</div>
                    <div class="day-events">${eventsHtml}</div>
                </div>
            `);

            currentDate.setDate(currentDate.getDate() + 1);
        }

        return days.join('');
    }

    function getEventsForDate(date) {
        const events = [];
        const dateStr = date.toISOString().split('T')[0];

        // Only show approved holidays
        appState.approvedHolidays.forEach(holiday => {
            // Parse dates properly - handle both string and Date objects
            const startDate = holiday.startDate instanceof Date ? holiday.startDate : new Date(holiday.startDate + 'T00:00:00');
            const endDate = holiday.endDate instanceof Date ? holiday.endDate : new Date(holiday.endDate + 'T23:59:59');
            
            // Compare dates using time components
            const currentDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
            const startDateOnly = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate());
            const endDateOnly = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate());
            
            if (currentDate >= startDateOnly && currentDate <= endDateOnly) {
                const employee = appState.employees.find(e => e.id === holiday.employeeId);
                
                // Use employeeName from holiday data as fallback, or construct from employee data
                let employeeName = 'Unbekannt';
                if (employee) {
                    employeeName = `${employee.firstName} ${employee.lastName}`;
                } else if (holiday.employeeName) {
                    employeeName = holiday.employeeName;
                }
                
                const typeLabel = getHolidayTypeLabel(holiday.type);
                const color = appState.employeeColors[holiday.employeeId];
                
                events.push({
                    type: 'approved',
                    employeeId: holiday.employeeId,
                    employeeName: employeeName,
                    holidayType: typeLabel,
                    color: color,
                    tooltip: `${typeLabel}: ${employeeName}`,
                    isStart: currentDate.getTime() === startDateOnly.getTime(),
                    isEnd: currentDate.getTime() === endDateOnly.getTime(),
                    holidayId: holiday.id
                });
            }
        });

        return events;
    }

    function updateCurrentMonthDisplay() {
        const months = [
            'Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
            'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'
        ];
        document.getElementById('current-month').textContent = `${months[appState.currentMonth]} ${appState.currentYear}`;
    }

    // Global function for month chip selection
    window.selectMonth = function(monthIndex) {
        appState.currentMonth = monthIndex;
        renderMonthChips();
        renderCalendarGrid();
        updateCurrentMonthDisplay();
    };

    // Initialize calendar
    generateEmployeeColors();
    renderEmployeeLegend();
    renderMonthChips();
    renderCalendarGrid();

    // --- Event Listeners ---
    function setupEventListeners() {
        // Add absence button
        const addBtn = document.getElementById('add-absence-btn');
        if (addBtn) {
            addBtn.addEventListener('click', openAbsenceModal);
        }

        // Search filter
        const searchInput = document.getElementById('employee-search');
        if (searchInput) {
            searchInput.addEventListener('input', filterEmployees);
        }

        // Sort button
        const sortBtn = document.getElementById('sort-remaining');
        if (sortBtn) {
            sortBtn.addEventListener('click', sortByRemaining);
        }

        // Month navigation
        const prevMonthBtn = document.getElementById('prev-month');
        const nextMonthBtn = document.getElementById('next-month');
        
        if (prevMonthBtn) {
            prevMonthBtn.addEventListener('click', () => {
                appState.currentMonth--;
                if (appState.currentMonth < 0) {
                    appState.currentMonth = 11;
                    appState.currentYear--;
                }
                renderMonthChips();
                renderCalendarGrid();
                updateCurrentMonthDisplay();
            });
        }
        
        if (nextMonthBtn) {
            nextMonthBtn.addEventListener('click', () => {
                appState.currentMonth++;
                if (appState.currentMonth > 11) {
                    appState.currentMonth = 0;
                    appState.currentYear++;
                }
                renderMonthChips();
                renderCalendarGrid();
                updateCurrentMonthDisplay();
            });
        }
    }

    function filterEmployees() {
        const searchTerm = document.getElementById('employee-search').value.toLowerCase();
        const rows = document.querySelectorAll('#employee-table-body tr');
        
        rows.forEach(row => {
            const employeeName = row.querySelector('td:first-child').textContent.toLowerCase();
            row.style.display = employeeName.includes(searchTerm) ? '' : 'none';
        });
    }

    function sortByRemaining() {
        appState.employeeStats.sort((a, b) => {
            const aRemaining = (a.employee?.vacationDays || 0) - (a.approvedDays || 0) - (a.pendingDays || 0);
            const bRemaining = (b.employee?.vacationDays || 0) - (b.approvedDays || 0) - (b.pendingDays || 0);
            return bRemaining - aRemaining;
        });
        renderEmployeeAccounts();
    }

    // Modal functionality
    function openAbsenceModal() {
        loadHolidayTypes();
        loadEmployees();
        prefillForm();
        
        const modalElement = document.getElementById('absenceModal');
        const modal = new bootstrap.Modal(modalElement);
        
        // Add event listeners for proper focus management
        modalElement.addEventListener('hidden.bs.modal', function () {
            // Reset focus when modal is hidden
            document.activeElement?.blur();
        });
        
        modal.show();
    }

    function loadHolidayTypes() {
        fetch('/api/holiday-types')
            .then(response => response.json())
            .then(types => {
                const select = document.getElementById('absence-type');
                if (!select) return;
                
                select.innerHTML = '<option value="">-- Bitte wählen --</option>';
                types.forEach(type => {
                    select.innerHTML += `<option value="${type.id}">${type.displayName}</option>`;
                });
            })
            .catch(error => {
                console.error('Error loading holiday types:', error);
                alert('Fehler beim Laden der Abwesenheitstypen');
            });
    }

    function loadEmployees() {
        const select = document.getElementById('absence-employee');
        if (!select) return;
        
        select.innerHTML = '<option value="">-- Bitte wählen --</option>';
        appState.employees.forEach(employee => {
            select.innerHTML += `<option value="${employee.id}">${employee.firstName} ${employee.lastName}</option>`;
        });
    }

    function prefillForm() {
        const today = new Date().toISOString().split('T')[0];
        const startDateInput = document.getElementById('absence-start-date');
        const endDateInput = document.getElementById('absence-end-date');
        
        if (startDateInput) startDateInput.value = today;
        if (endDateInput) endDateInput.value = today;
    }

    // Save absence
    document.getElementById('save-absence-btn')?.addEventListener('click', function() {
        const form = document.getElementById('absenceForm');
        const formData = new FormData(form);
        
        // Get the selected holiday type display name
        const typeSelect = document.getElementById('absence-type');
        const selectedTypeOption = typeSelect.options[typeSelect.selectedIndex];
        const holidayTypeName = selectedTypeOption ? selectedTypeOption.text : '';
        
        const absenceData = {
            employeeId: parseInt(formData.get('employee')),
            type: holidayTypeName,
            startDate: formData.get('startDate'),
            endDate: formData.get('endDate'),
            reason: formData.get('reason') || null
        };

        // Detaillierte Validierung mit spezifischen Fehlermeldungen
        const errors = [];
        
        if (!absenceData.employeeId) {
            errors.push('Mitarbeiter muss ausgewählt werden');
        }
        
        if (!absenceData.type) {
            errors.push('Abwesenheitstyp muss ausgewählt werden');
        }
        
        if (!absenceData.startDate) {
            errors.push('Startdatum muss angegeben werden');
        }
        
        if (!absenceData.endDate) {
            errors.push('Enddatum muss angegeben werden');
        }
        
        if (absenceData.startDate && absenceData.endDate) {
            const startDate = new Date(absenceData.startDate);
            const endDate = new Date(absenceData.endDate);
            
            if (endDate < startDate) {
                errors.push('Enddatum darf nicht vor dem Startdatum liegen');
            }
        }

        if (errors.length > 0) {
            alert('Bitte korrigieren Sie folgende Fehler:\n\n' + errors.join('\n'));
            return;
        }

        fetch('/timetracking/api/holidays', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(absenceData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            showSuccessMessage('Abwesenheit erfolgreich erfasst!');
            
            // Properly close the modal and reset focus
            const modal = document.getElementById('absenceModal');
            const modalInstance = bootstrap.Modal.getInstance(modal);
            if (modalInstance) {
                modalInstance.hide();
            }
            
            // Reset form
            form.reset();
            
            // Remove focus from modal elements
            document.activeElement?.blur();
            
            // Reload page to refresh data
            setTimeout(() => window.location.reload(), 1500);
        })
        .catch(error => {
            console.error('Error saving absence:', error);
            alert('Fehler beim Speichern der Abwesenheit.');
        });
    });

    function showSuccessMessage(message) {
        const successDiv = document.createElement('div');
        successDiv.className = 'success-message';
        successDiv.textContent = message;
        document.body.appendChild(successDiv);
        
        setTimeout(() => {
            successDiv.remove();
        }, 3000);
    }

    // Global functions for calendar navigation
    window.previousMonth = function() {
        if (appState.currentMonth > 0) {
            appState.currentMonth--;
        } else {
            appState.currentMonth = 11;
            appState.currentYear--;
        }
        renderCalendar();
    };

    window.nextMonth = function() {
        if (appState.currentMonth < 11) {
            appState.currentMonth++;
        } else {
            appState.currentMonth = 0;
            appState.currentYear++;
        }
        renderCalendar();
    };

    window.selectMonth = function(month) {
        appState.currentMonth = month;
        renderCalendar();
    };

    function renderCalendar() {
        renderMonthChips();
        renderCalendarGrid();
    }

    // Holiday approval/rejection functions
    window.approveHoliday = function(holidayId) {
        if (confirm('Urlaubsantrag genehmigen?')) {
            updateHolidayStatus(holidayId, 'APPROVED');
        }
    };

    window.rejectHoliday = function(holidayId) {
        if (confirm('Urlaubsantrag ablehnen?')) {
            updateHolidayStatus(holidayId, 'REJECTED');
        }
    };

    function updateHolidayStatus(holidayId, status) {
        // Find current user ID (you might need to adjust this based on your auth system)
        const currentUserId = 1; // Default admin user ID
        
        fetch(`/timetracking/api/holidays/${holidayId}/approve`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                approval: {
                    status: status,
                    approverId: currentUserId,
                    approvedAt: new Date().toISOString().split('T')[0]
                }
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            showSuccessMessage(`Urlaubsantrag ${status === 'APPROVED' ? 'genehmigt' : 'abgelehnt'}!`);
            setTimeout(() => window.location.reload(), 1500);
        })
        .catch(error => {
            console.error('Error updating holiday status:', error);
            alert('Fehler beim Aktualisieren des Urlaubsantrags.');
        });
    }

    // Initialize the app
    setupEventListeners();
    console.log('Absences app initialized successfully');
});