// Modern Absences Management JavaScript
// This script generates all UI (tabs, employee stats, calendar, floating button) dynamically.
document.addEventListener('DOMContentLoaded', function() {
    // App state
    const appState = {
        holidays: [],
        employees: [],
        pendingHolidays: [],
        approvedHolidays: [],
        rejectedHolidays: [],
        employeeStats: [],
        publicHolidays: [],
        currentYear: new Date().getFullYear(),
        currentMonth: new Date().getMonth(),
        employeeColors: {},
        bannerDismissed: false
    };

    // Initialize the app
    function initializeApp() {
        loadData();
        setupEventListeners();
        initializeTooltips();
        console.log('Absences app initialized successfully');
    }

    function initializeTooltips() {
        // Initialize Bootstrap tooltips
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }

    function loadData() {
        const appElement = document.getElementById('absences-app');
        if (!appElement) return;

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
                rejected: appState.rejectedHolidays.length
            });

            // Generate employee colors
            generateEmployeeColors();
            
            // Render the page with new logic
            renderPage();
            
        } catch (error) {
            console.error('Error loading data:', error);
        }
    }

    function renderPage() {
        // Check if there are pending requests
        const hasPendingRequests = appState.pendingHolidays.length > 0;
        
        // Show/hide banner based on pending requests and dismissal state
        const banner = document.getElementById('pending-requests-banner');
        if (banner) {
            if (hasPendingRequests && !appState.bannerDismissed) {
                banner.style.display = 'block';
                updateBannerContent();
            } else {
                banner.style.display = 'none';
            }
        }
        
        // Show/hide holiday requests section
        const requestsSection = document.getElementById('holiday-requests-section');
        if (requestsSection) {
            if (appState.holidays.length > 0) {
                requestsSection.style.display = 'block';
                renderHolidayRequests();
            } else {
                requestsSection.style.display = 'none';
            }
        }
        
        // Always render calendar and employee accounts
        renderCalendar();
        renderEmployeeAccounts();
        
        // Re-initialize tooltips after rendering
        initializeTooltips();
    }

    function updateBannerContent() {
        const pendingCount = appState.pendingHolidays.length;
        const countElement = document.getElementById('pending-count');
        const countTextElement = document.getElementById('pending-count-text');
        
        if (countElement) {
            countElement.textContent = pendingCount;
        }
        
        if (countTextElement) {
            if (pendingCount === 1) {
                countTextElement.textContent = 'Sie haben 1 offenen Urlaubsantrag, der überprüft werden muss.';
            } else {
                countTextElement.textContent = `Sie haben ${pendingCount} offene Urlaubsanträge, die überprüft werden müssen.`;
            }
        }
    }

    function generateEmployeeColors() {
        const colors = [
            '#0d6efd', '#198754', '#ffc107', '#dc3545', '#0dcaf0',
            '#6f42c1', '#fd7e14', '#20c997', '#e83e8c', '#6c757d'
        ];
        
        appState.employees.forEach((employee, index) => {
            appState.employeeColors[employee.id] = colors[index % colors.length];
        });
    }

    function renderEmployeeLegend() {
        const legendContainer = document.getElementById('legend-items');
        if (!legendContainer) return;

        legendContainer.innerHTML = '';
        
        appState.employees.forEach(employee => {
            const color = appState.employeeColors[employee.id];
            const legendItem = document.createElement('div');
            legendItem.className = 'legend-item';
            legendItem.onclick = () => filterEmployees(employee.id);
            
            legendItem.innerHTML = `
                <div class="legend-color" style="background-color: ${color}"></div>
                <span class="legend-name">${employee.firstName} ${employee.lastName}</span>
            `;
            
            legendContainer.appendChild(legendItem);
        });
    }

    function renderRequestTab(containerId, requests, status) {
        const container = document.getElementById(containerId);
        const noContentId = containerId.replace('-requests', '-no');
        const noContent = document.getElementById(noContentId);
        
        if (!container) return;
        
        if (requests.length === 0) {
            container.innerHTML = '';
            if (noContent) noContent.style.display = 'block';
            return;
        }
        
        if (noContent) noContent.style.display = 'none';
        
        container.innerHTML = '';
        requests.forEach(holiday => {
            const card = createHolidayCard(holiday, status);
            container.appendChild(card);
        });
    }

    function createHolidayCard(holiday, status) {
        const card = document.createElement('div');
        card.className = `holiday-request-card ${status.toLowerCase()}`;
        
        const startDate = new Date(holiday.startDate).toLocaleDateString('de-DE');
        const endDate = new Date(holiday.endDate).toLocaleDateString('de-DE');
        const requestDate = holiday.createdAt ? new Date(holiday.createdAt).toLocaleDateString('de-DE') : 'Unbekannt';
        
        card.innerHTML = `
            <div class="request-header">
                <div class="request-info">
                    <h5>${holiday.employeeName || 'Unbekannter Mitarbeiter'}</h5>
                    <div class="request-dates">
                        <strong>Zeitraum:</strong> ${startDate} - ${endDate}<br>
                        <strong>Antrag eingereicht:</strong> ${requestDate}
                    </div>
                </div>
                <div class="holiday-type-badge ${getHolidayTypeBadgeClass(holiday.type)}">
                    ${getHolidayTypeLabel(holiday.type)}
                </div>
            </div>
            ${holiday.reason ? `<div class="request-reason">${holiday.reason}</div>` : ''}
            ${status === 'pending' ? `
                <div class="request-actions">
                    <button class="btn btn-success" onclick="approveHoliday(${holiday.id})">
                        <i class="bi bi-check"></i> Genehmigen
                    </button>
                    <button class="btn btn-danger" onclick="rejectHoliday(${holiday.id})">
                        <i class="bi bi-x"></i> Ablehnen
                    </button>
                </div>
            ` : ''}
        `;
        
        return card;
    }

    function getHolidayTypeBadgeClass(type) {
        const typeLower = type?.toLowerCase() || '';
        if (typeLower.includes('urlaub') || typeLower.includes('vacation')) return 'vacation';
        if (typeLower.includes('unbezahlt') || typeLower.includes('unpaid')) return 'unpaid';
        if (typeLower.includes('krank') || typeLower.includes('sick')) return 'sick';
        return 'special';
    }

    function getHolidayTypeLabel(type) {
        if (!type) return 'Unbekannt';
        return type;
    }

    function updateTabCounts() {
        const pendingCount = appState.pendingHolidays.length;
        const approvedCount = appState.approvedHolidays.length;
        const rejectedCount = appState.rejectedHolidays.length;
        
        const pendingBadge = document.getElementById('pending-count-badge');
        const approvedBadge = document.getElementById('approved-count-badge');
        const rejectedBadge = document.getElementById('rejected-count-badge');
        
        if (pendingBadge) pendingBadge.textContent = pendingCount;
        if (approvedBadge) approvedBadge.textContent = approvedCount;
        if (rejectedBadge) rejectedBadge.textContent = rejectedCount;
    }

    function renderHolidayRequests() {
        renderRequestTab('pending-requests', appState.pendingHolidays, 'pending');
        renderRequestTab('approved-requests', appState.approvedHolidays, 'approved');
        renderRequestTab('rejected-requests', appState.rejectedHolidays, 'rejected');
        updateTabCounts();
    }

    function renderEmployeeAccounts() {
        const tbody = document.getElementById('employee-accounts-body');
        if (!tbody) return;

        tbody.innerHTML = '';
        
        appState.employeeStats.forEach(stat => {
            const row = document.createElement('tr');
            
            // Get employee name from the employee object
            const employeeName = stat.employee ? `${stat.employee.firstName} ${stat.employee.lastName}` : 'Unbekannter Mitarbeiter';
            const employeeId = stat.employee?.id || 0;
            
            const takenDays = stat.approvedDays || 0;
            const totalDays = stat.employee?.vacationDays || 25;
            const remainingDays = stat.remainingDays || (totalDays - takenDays);
            const progressPercent = totalDays > 0 ? (takenDays / totalDays) * 100 : 0;
            
            row.innerHTML = `
                <td>
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                        <div style="width: 12px; height: 12px; border-radius: 50%; background-color: ${appState.employeeColors[employeeId] || '#6c757d'};"></div>
                        <strong>${employeeName}</strong>
                    </div>
                </td>
                <td>${takenDays} Tage</td>
                <td>${remainingDays} Tage</td>
                <td style="width: 200px;">
                    <div class="vacation-progress">
                        <div class="progress-fill" style="width: ${progressPercent}%"></div>
                        <div class="progress-text">${Math.round(progressPercent)}%</div>
                    </div>
                </td>
            `;
            
            tbody.appendChild(row);
        });
    }

    function renderMonthChips() {
        const container = document.getElementById('month-chips');
        if (!container) return;

        container.innerHTML = '';
        const months = [
            'Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun',
            'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'
        ];

        months.forEach((month, index) => {
            const chip = document.createElement('div');
            chip.className = `month-chip ${index === appState.currentMonth ? 'active' : ''}`;
            chip.textContent = month;
            chip.onclick = () => selectMonth(index);
            container.appendChild(chip);
        });
    }

    function renderCalendarGrid() {
        const container = document.getElementById('calendar-body');
        if (!container) return;

        container.innerHTML = '';
        
        const days = generateCalendarDays(appState.currentYear, appState.currentMonth);
        
        days.forEach(day => {
            const dayElement = document.createElement('div');
            dayElement.className = `calendar-day ${day.isWeekend ? 'weekend' : ''} ${day.isEmpty ? 'empty' : ''} ${day.hasEvents ? 'has-events' : ''}`;
            
            if (!day.isEmpty) {
                dayElement.innerHTML = `
                    <div class="day-number">${day.dayNumber}</div>
                    <div class="day-events">${day.events}</div>
                `;
            }
            
            container.appendChild(dayElement);
        });
    }

    function generateCalendarDays(year, month) {
        const days = [];
        
        console.log(`Generating calendar for ${year}-${month + 1}`);
        
        // Calculate the first and last day of the month
        const firstDayOfMonth = new Date(year, month, 1);
        const lastDayOfMonth = new Date(year, month + 1, 0);
        
        // Helper function to format date as YYYY-MM-DD
        const formatDate = (date) => {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        };

        // Calculate the start of the calendar grid (Monday of the week containing the first day)
        const dayOfWeek = firstDayOfMonth.getDay(); // 0 = Sunday, 1 = Monday, etc.
        const daysToSubtract = dayOfWeek === 0 ? 6 : dayOfWeek - 1; // Convert to Monday-based week
        const gridStart = new Date(firstDayOfMonth);
        gridStart.setDate(gridStart.getDate() - daysToSubtract);
        
        // Calculate the end of the calendar grid (Sunday of the week containing the last day)
        const lastDayOfWeek = lastDayOfMonth.getDay(); // 0 = Sunday, 1 = Monday, etc.
        const daysToAdd = lastDayOfWeek === 0 ? 0 : 7 - lastDayOfWeek; // Convert to Sunday-based week
        const gridEnd = new Date(lastDayOfMonth);
        gridEnd.setDate(gridEnd.getDate() + daysToAdd);

        // Generate all days in the grid
        const currentGridDate = new Date(gridStart);
        while (currentGridDate <= gridEnd) {
            const dayNumber = currentGridDate.getDate();
            const isCurrentMonth = currentGridDate.getMonth() === month && currentGridDate.getFullYear() === year;
            const isWeekend = currentGridDate.getDay() === 0 || currentGridDate.getDay() === 6;
            
            let events = '';
            let hasEvents = false;
            
            if (isCurrentMonth) {
                // Create date string in YYYY-MM-DD format for comparison
                const dateString = formatDate(currentGridDate);
                const dayEvents = getEventsForDate(dateString);
                events = dayEvents;
                hasEvents = dayEvents.length > 0;
            }
            
            days.push({
                dayNumber: isCurrentMonth ? dayNumber : '',
                isEmpty: !isCurrentMonth,
                isWeekend: isWeekend && isCurrentMonth,
                hasEvents: hasEvents,
                events: events
            });
            
            currentGridDate.setDate(currentGridDate.getDate() + 1);
        }
        
        return days;
    }
    
    function getEventsForDate(dateString) {
        const events = [];

        // Check holidays for this date - only show APPROVED and PENDING
        appState.holidays.forEach(holiday => {
            // Skip rejected holidays
            if (holiday.status === 'REJECTED') {
                return;
            }
            
            // Compare dates as strings to avoid timezone issues
            // Backend sends dates in YYYY-MM-DD format
            const holidayStartDate = holiday.startDate;
            const holidayEndDate = holiday.endDate;

            if (dateString >= holidayStartDate && dateString <= holidayEndDate) {
                const employee = appState.employees.find(emp => emp.id === holiday.employeeId);
                const employeeName = employee ? `${employee.firstName} ${employee.lastName}` : 'Unbekannt';
                const color = appState.employeeColors[holiday.employeeId] || '#6c757d';
                
                const isStart = dateString === holidayStartDate;
                const isEnd = dateString === holidayEndDate;
                
                // More robust status checking - handle different possible values
                const isPending = holiday.status === 'PENDING' || holiday.status === 'pending' || holiday.status === 'Offen';
                const isApproved = holiday.status === 'APPROVED' || holiday.status === 'approved' || holiday.status === 'Genehmigt';
                
                // Create tooltip data
                const tooltipData = {
                    employeeName: employeeName,
                    holidayType: holiday.type,
                    startDate: holiday.startDate,
                    endDate: holiday.endDate,
                    status: holiday.status,
                    reason: holiday.reason,
                    color: color
                };
                
                // Apply different styles based on status
                const statusClass = isPending ? 'pending' : (isApproved ? 'approved' : 'unknown');
                const barStyle = isPending 
                    ? `color: ${color};` 
                    : `background-color: ${color};`;
                
                events.push(`
                    <div class="holiday-bar ${isStart ? 'start' : ''} ${isEnd ? 'end' : ''} ${statusClass}" 
                         style="${barStyle}"
                         data-holiday-id="${holiday.id}"
                         data-tooltip='${JSON.stringify(tooltipData)}'
                         onmouseenter="showHolidayTooltip(event, ${holiday.id})"
                         onmouseleave="hideHolidayTooltip()">
                        ${employeeName}
                    </div>
                `);
            }
        });
        
        return events.join('');
    }

    function updateCurrentMonthDisplay() {
        const months = [
            'Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
            'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'
        ];
        
        const display = document.getElementById('current-month-display');
        if (display) {
            display.textContent = `${months[appState.currentMonth]} ${appState.currentYear}`;
        }
    }

    // Setup event listeners
    function setupEventListeners() {
        // Employee search
        const employeeSearch = document.getElementById('employee-search');
        if (employeeSearch) {
            employeeSearch.addEventListener('input', function() {
                filterEmployees(null, this.value);
            });
        }

        // Review now button
        const reviewNowBtn = document.getElementById('review-now-btn');
        if (reviewNowBtn) {
            reviewNowBtn.addEventListener('click', function() {
                document.getElementById('holiday-requests-section').style.display = 'block';
                document.getElementById('pending-requests-banner').style.display = 'none';
                document.getElementById('pending-tab').click();
            });
        }

        // Dismiss banner button
        const dismissBannerBtn = document.getElementById('dismiss-banner-btn');
        if (dismissBannerBtn) {
            dismissBannerBtn.addEventListener('click', function() {
                document.getElementById('pending-requests-banner').style.display = 'none';
            });
        }

        // Conflict modal override button
        const overrideConflictBtn = document.getElementById('override-conflict-btn');
        if (overrideConflictBtn) {
            overrideConflictBtn.addEventListener('click', function() {
                console.log('Override button clicked');
                
                // Get the form data directly from the absence form
                const form = document.getElementById('absenceForm');
                if (!form) {
                    console.error('Absence form not found');
                    alert('Fehler: Formular nicht gefunden.');
                    return;
                }
                
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
                
                console.log('Created absence data from form:', absenceData);
                
                if (!absenceData.employeeId || !absenceData.type || !absenceData.startDate || !absenceData.endDate) {
                    alert('Fehler: Bitte füllen Sie alle Pflichtfelder aus.');
                    return;
                }
                
                try {
                    // Close conflict modal
                    const conflictModal = document.getElementById('conflictModal');
                    const modalInstance = bootstrap.Modal.getInstance(conflictModal);
                    if (modalInstance) {
                        modalInstance.hide();
                    }
                    
                    // Proceed with override
                    saveAbsenceWithOverride(absenceData);
                } catch (error) {
                    console.error('Error in override button handler:', error);
                    alert('Fehler beim Verarbeiten der Abwesenheitsdaten.');
                }
            });
        }
    }
        
    function filterEmployees(employeeId = null, searchTerm = '') {
        // This function can be expanded to filter the calendar view
        console.log('Filtering employees:', { employeeId, searchTerm });
    }

    function sortByRemaining() {
        // Sort employee stats by remaining days
        appState.employeeStats.sort((a, b) => {
            const aRemaining = a.remainingDays || 0;
            const bRemaining = b.remainingDays || 0;
            return aRemaining - bRemaining;
        });
        renderEmployeeAccounts();
    }

    // Make openAbsenceModal globally accessible
    window.openAbsenceModal = function() {
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
    };

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

        // First check for conflicts
        checkForConflicts(absenceData)
            .then(conflicts => {
                if (conflicts && conflicts.conflictingHolidays && conflicts.conflictingHolidays.length > 0) {
                    // Show conflict modal
                    showConflictModal(conflicts, absenceData);
                } else {
                    // No conflicts, proceed with saving
                    saveAbsence(absenceData);
                }
            })
            .catch(error => {
                console.error('Error checking for conflicts:', error);
                // If conflict check fails, try to save anyway
                saveAbsence(absenceData);
            });
    });

    function checkForConflicts(absenceData) {
        const url = `/timetracking/api/holidays/conflicts?employeeId=${absenceData.employeeId}&startDate=${absenceData.startDate}&endDate=${absenceData.endDate}`;
        
        return fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to check for conflicts');
                }
                return response.json();
            });
    }

    // Global variable to track if error was handled in modal
    let errorHandledInModal = false;
    
    // Global variable to store absence data for override
    let currentAbsenceData = null;

    function showConflictModal(conflicts, absenceData) {
        // Reset error handling flag
        errorHandledInModal = false;
        
        // Store absence data globally
        currentAbsenceData = absenceData;
        console.log('Storing absence data globally:', currentAbsenceData);
        
        // Hide any previous error message
        const errorDiv = document.getElementById('conflict-error-message');
        if (errorDiv) {
            errorDiv.classList.add('d-none');
            errorDiv.textContent = '';
        }
        // Populate the conflict list
        const conflictList = document.getElementById('conflicting-holidays-list');
        conflictList.innerHTML = '';
        
        conflicts.conflictingHolidays.forEach(holiday => {
            const startDate = new Date(holiday.startDate).toLocaleDateString('de-DE');
            const endDate = new Date(holiday.endDate).toLocaleDateString('de-DE');
            const statusClass = holiday.status.toLowerCase();
            const statusText = getGermanStatusText(holiday.status);
            
            const conflictItem = document.createElement('div');
            conflictItem.className = 'conflict-item border rounded p-3 mb-2';
            conflictItem.innerHTML = `
                <div class="d-flex justify-content-between align-items-start">
                    <div>
                        <strong>${startDate} - ${endDate}:</strong> ${holiday.type}
                        <br>
                        <small class="text-muted">Status: <span class="badge bg-${statusClass === 'approved' ? 'success' : statusClass === 'pending' ? 'warning' : 'danger'}">${statusText}</span></small>
                        ${holiday.reason ? `<br><small class="text-muted">Grund: ${holiday.reason}</small>` : ''}
                    </div>
                </div>
            `;
            conflictList.appendChild(conflictItem);
        });
        
        // Show the modal
        const conflictModal = document.getElementById('conflictModal');
        if (conflictModal) {
            const modal = new bootstrap.Modal(conflictModal);
            modal.show();
        } else {
            console.error('Conflict modal not found');
        }
    }

    function saveAbsence(absenceData) {
        fetch('/timetracking/api/holidays', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(absenceData)
        })
        .then(response => {
            console.log('Response status:', response.status, response.statusText);
            console.log('Response headers:', response.headers);
            
            if (!response.ok) {
                // Try to parse error response
                return response.text().then(responseText => {
                    console.log('Raw response text:', responseText);
                    
                    try {
                        const errorData = JSON.parse(responseText);
                        console.log('Parsed error data:', errorData);
                        // If it's a HolidayOverlapException, show in modal
                        if (errorData.type === 'HolidayOverlapException') {
                            errorHandledInModal = true;
                            showConflictErrorInModal(formatErrorResponse(errorData));
                            throw new Error('handled-in-modal');
                        }
                        throw new Error(formatErrorResponse(errorData));
                    } catch (parseError) {
                        console.error('Failed to parse error response as JSON:', parseError);
                        // If error response is not JSON, use the raw text
                        throw new Error(responseText || 'Network response was not ok');
                    }
                });
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
            console.log('Catch block - error type:', typeof error);
            console.log('Catch block - error:', error);
            console.log('Catch block - error.message:', error?.message);
            console.log('Error handled in modal:', errorHandledInModal);
            
            // Check if error was already handled in modal
            if (errorHandledInModal) {
                console.log('Error already handled in modal, returning early');
                return;
            }
            
            console.error('Error saving absence:', error);
            
            // Check if the error message is a JSON string (raw error response)
            let displayMessage = error.message;
            if (error.message && error.message.startsWith('{') && error.message.includes('"error"')) {
                try {
                    const errorData = JSON.parse(error.message);
                    console.log('Parsed error data in catch:', errorData);
                    displayMessage = formatErrorResponse(errorData);
                } catch (parseError) {
                    // If parsing fails, use the original message
                    displayMessage = error.message;
                }
            }
            
            alert('Fehler beim Speichern der Abwesenheit: ' + displayMessage);
        });
    }

    function saveAbsenceWithOverride(absenceData) {
        console.log('Saving absence with override:', absenceData);
        
        // First, get the conflicting holidays to cancel them
        fetch(`/timetracking/api/holidays/conflicts?employeeId=${absenceData.employeeId}&startDate=${absenceData.startDate}&endDate=${absenceData.endDate}`)
            .then(response => {
                if (!response.ok) {
                    console.warn('Failed to get conflicts, proceeding anyway');
                    return { conflictingHolidays: [] };
                }
                return response.json();
            })
            .then(conflicts => {
                console.log('Found conflicts to cancel:', conflicts);
                
                // Cancel all conflicting holidays
                if (conflicts.conflictingHolidays && conflicts.conflictingHolidays.length > 0) {
                    const cancelPromises = conflicts.conflictingHolidays.map(holiday => {
                        console.log('Canceling holiday:', holiday.id);
                        return fetch(`/timetracking/api/holidays/${holiday.id}/cancel`, {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                            }
                        });
                    });
                    
                    return Promise.all(cancelPromises);
                } else {
                    console.log('No conflicts to cancel');
                    return Promise.resolve();
                }
            })
            .then(() => {
                console.log('All conflicts canceled, creating new holiday');
                // Now create the new holiday
                return fetch('/timetracking/api/holidays', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(absenceData)
                });
            })
            .then(response => {
                if (!response.ok) {
                    // Try to get more details about the error
                    return response.text().then(responseText => {
                        console.log('Error response from holiday creation:', responseText);
                        try {
                            const errorData = JSON.parse(responseText);
                            if (errorData.type === 'HolidayOverlapException') {
                                console.log('Holiday overlap detected, automatically canceling existing holidays');
                                
                                // Automatically cancel all existing holidays for this date range
                                return cancelAllHolidaysForDateRange(absenceData.employeeId, absenceData.startDate, absenceData.endDate)
                                    .then(() => {
                                        console.log('All existing holidays canceled, retrying creation');
                                        // Retry creating the new holiday
                                        return fetch('/timetracking/api/holidays', {
                                            method: 'POST',
                                            headers: {
                                                'Content-Type': 'application/json',
                                            },
                                            body: JSON.stringify(absenceData)
                                        });
                                    })
                                    .then(retryResponse => {
                                        if (!retryResponse.ok) {
                                            return retryResponse.text().then(retryText => {
                                                throw new Error(`Fehler beim zweiten Versuch: ${retryText}`);
                                            });
                                        }
                                        return retryResponse.json();
                                    });
                            }
                            throw new Error(`Fehler beim Erstellen der Abwesenheit: ${errorData.error || responseText}`);
                        } catch (parseError) {
                            throw new Error(`Fehler beim Erstellen der Abwesenheit: ${responseText}`);
                        }
                    });
                }
                return response.json();
            })
            .then(data => {
                showSuccessMessage('Abwesenheit erfolgreich erfasst! Bestehende Abwesenheiten wurden angepasst.');
                
                // Close any open modals
                const absenceModal = document.getElementById('absenceModal');
                const absenceModalInstance = bootstrap.Modal.getInstance(absenceModal);
                if (absenceModalInstance) {
                    absenceModalInstance.hide();
                }
                
                const conflictModal = document.getElementById('conflictModal');
                const conflictModalInstance = bootstrap.Modal.getInstance(conflictModal);
                if (conflictModalInstance) {
                    conflictModalInstance.hide();
                }
                
                // Reset form
                const form = document.getElementById('absenceForm');
                if (form) {
                    form.reset();
                }
                
                // Reload page to refresh data
                setTimeout(() => window.location.reload(), 1500);
            })
            .catch(error => {
                console.error('Error saving absence with override:', error);
                alert('Fehler beim Überschreiben der Abwesenheit: ' + error.message);
            });
    }

    function showConflictErrorInModal(message) {
        console.log('Showing conflict error in modal:', message);
        
        // Make sure the conflict modal is visible
        const conflictModal = document.getElementById('conflictModal');
        if (conflictModal) {
            const modalInstance = bootstrap.Modal.getInstance(conflictModal);
            if (!modalInstance || !modalInstance._isShown) {
                console.log('Conflict modal not visible, showing it now');
                const newModalInstance = new bootstrap.Modal(conflictModal);
                newModalInstance.show();
            }
        }
        
        const errorDiv = document.getElementById('conflict-error-message');
        if (errorDiv) {
            errorDiv.innerHTML = `
                <i class="bi bi-exclamation-triangle"></i>
                <strong>Fehler:</strong> ${message}
            `;
            errorDiv.classList.remove('d-none');
            
            // Scroll to the error message
            errorDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
            
            // Add some visual emphasis
            errorDiv.style.border = '2px solid #dc3545';
            errorDiv.style.backgroundColor = '#f8d7da';
            errorDiv.style.color = '#721c24';
            
            console.log('Error message added to modal');
        } else {
            console.error('Error div not found in modal');
        }
    }

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
        updateCurrentMonthDisplay();
        renderEmployeeLegend();
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

    // Tooltip functions
    window.showHolidayTooltip = function(event, holidayId) {
        const holiday = appState.holidays.find(h => h.id === holidayId);
        if (!holiday) return;
        
        const employee = appState.employees.find(emp => emp.id === holiday.employeeId);
        const employeeName = employee ? `${employee.firstName} ${employee.lastName}` : 'Unbekannt';
        
        // Remove existing tooltip
        const existingTooltip = document.querySelector('.holiday-tooltip');
        if (existingTooltip) {
            existingTooltip.remove();
        }
        
        // Create tooltip
        const tooltip = document.createElement('div');
        tooltip.className = 'holiday-tooltip';
        
        const startDate = new Date(holiday.startDate).toLocaleDateString('de-DE');
        const endDate = new Date(holiday.endDate).toLocaleDateString('de-DE');
        const requestDate = holiday.createdAt ? new Date(holiday.createdAt).toLocaleDateString('de-DE') : 'Unbekannt';
        
        // Get proper German status text
        const statusText = getGermanStatusText(holiday.status);
        

        
        // Ensure status is defined before using toLowerCase()
        // If status is undefined, assume it's pending
        const statusClass = holiday.status ? holiday.status.toLowerCase() : 'pending';
        
        const tooltipHTML = `
            <div class="tooltip-header">
                <i class="bi bi-person"></i> ${employeeName}
            </div>
            <div class="tooltip-content">
                <div class="tooltip-row">
                    <span class="tooltip-label">Urlaubstyp:</span>
                    <span class="tooltip-value">${holiday.type || 'Unbekannt'}</span>
                </div>
                <div class="tooltip-row">
                    <span class="tooltip-label">Zeitraum:</span>
                    <span class="tooltip-value">${startDate} - ${endDate}</span>
                </div>
                <div class="tooltip-row">
                    <span class="tooltip-label">Antrag eingereicht:</span>
                    <span class="tooltip-value">${requestDate}</span>
                </div>
                ${holiday.reason ? `
                <div class="tooltip-row">
                    <span class="tooltip-label">Grund:</span>
                    <span class="tooltip-value">${holiday.reason}</span>
                </div>
                ` : ''}
                <div class="tooltip-status ${statusClass}">
                    ${statusText}
                </div>
            </div>
        `;
        

        
        tooltip.innerHTML = tooltipHTML;
        
        // Add to body first to get proper dimensions
        document.body.appendChild(tooltip);
        
        // Position tooltip relative to the holiday bar element
        const barElement = event.target;
        
        // Get the actual holiday bar element (not a child element)
        const actualBarElement = barElement.classList.contains('holiday-bar') ? barElement : barElement.closest('.holiday-bar');
        if (!actualBarElement) return;
        
        // Get positions relative to viewport
        const barRect = actualBarElement.getBoundingClientRect();
        const tooltipRect = tooltip.getBoundingClientRect();
        const viewportHeight = window.innerHeight;
        const viewportWidth = window.innerWidth;
        
        // Add scroll offset to get absolute positions
        const scrollX = window.pageXOffset || document.documentElement.scrollLeft;
        const scrollY = window.pageYOffset || document.documentElement.scrollTop;
        
        // Calculate horizontal position (center over the bar)
        let left = barRect.left + scrollX + (barRect.width / 2) - (tooltipRect.width / 2);
        
        // Ensure tooltip doesn't go off-screen horizontally
        if (left < scrollX + 10) left = scrollX + 10;
        if (left + tooltipRect.width > scrollX + viewportWidth - 10) {
            left = scrollX + viewportWidth - tooltipRect.width - 10;
        }
        
        // Calculate vertical position
        let top;
        const gap = 4; // Small gap between tooltip and bar
        let tooltipPosition = 'below'; // Default position
        
        // Check if there's enough space below the bar
        if (barRect.bottom + scrollY + tooltipRect.height + gap <= scrollY + viewportHeight) {
            // Position below the bar
            top = barRect.bottom + scrollY + gap;
            tooltipPosition = 'below';
        } else {
            // Position above the bar
            top = barRect.top + scrollY - tooltipRect.height - gap;
            tooltipPosition = 'above';
        }
        
        // Ensure tooltip doesn't go off-screen vertically
        if (top < scrollY + 10) {
            top = scrollY + 10;
            tooltipPosition = 'below';
        }
        if (top + tooltipRect.height > scrollY + viewportHeight - 10) {
            top = scrollY + viewportHeight - tooltipRect.height - 10;
            tooltipPosition = 'above';
        }
        
        // Add position class for proper arrow direction
        tooltip.classList.add(`tooltip-${tooltipPosition}`);
        
        // Set position using fixed positioning to avoid scroll issues
        tooltip.style.position = 'fixed';
        tooltip.style.left = (left - scrollX) + 'px';
        tooltip.style.top = (top - scrollY) + 'px';
        
        // Show with animation
        setTimeout(() => {
            tooltip.classList.add('show');
        }, 10);
    };

    window.hideHolidayTooltip = function() {
        const tooltip = document.querySelector('.holiday-tooltip');
        if (tooltip) {
            tooltip.classList.remove('show');
            setTimeout(() => {
                tooltip.remove();
            }, 200);
        }
    };

    // Helper function to get German status text
    function getGermanStatusText(status) {
        // If status is undefined/null, assume it's pending (new requests often don't have status set)
        if (!status) {
            return 'Ausstehend';
        }
        
        const statusMap = {
            'PENDING': 'Ausstehend',
            'pending': 'Ausstehend',
            'Offen': 'Ausstehend',
            'offen': 'Ausstehend',
            'APPROVED': 'Genehmigt',
            'approved': 'Genehmigt',
            'Genehmigt': 'Genehmigt',
            'genehmigt': 'Genehmigt',
            'REJECTED': 'Abgelehnt',
            'rejected': 'Abgelehnt',
            'Abgelehnt': 'Abgelehnt',
            'abgelehnt': 'Abgelehnt'
        };
        
        return statusMap[status] || 'Unbekannt';
    }

    function formatErrorResponse(errorData) {
        console.log('Formatting error response:', errorData);
        
        // Handle cases where errorData might be a string or have different structure
        if (typeof errorData === 'string') {
            return errorData;
        }
        
        if (!errorData || typeof errorData !== 'object') {
            return 'Ein unbekannter Fehler ist aufgetreten';
        }
        
        const { error, type, field, value } = errorData;
        
        // If no error message is provided, use a default
        if (!error) {
            return 'Ein Fehler ist aufgetreten';
        }
        
        // Benutzerfreundliche Fehlermeldungen basierend auf dem Fehlertyp
        switch (type) {
            case 'HolidayOverlapException':
                return error; // Die Fehlermeldung ist bereits benutzerfreundlich
            
            case 'MissingRequiredFieldException':
                return `📝 **Pflichtfeld fehlt**: Das Feld "${field}" ist erforderlich. Bitte füllen Sie alle markierten Felder aus.`;
            
            case 'InvalidDateException':
                return '📅 **Ungültiges Datum**: ' + error;
            
            case 'EmployeeNotFoundException':
                return '👤 **Mitarbeiter nicht gefunden**: Der ausgewählte Mitarbeiter existiert nicht mehr.';
            
            case 'BusinessRuleViolationException':
                return '⚠️ **Geschäftsregel verletzt**: ' + error;
            
            case 'ValidationError':
                return `⚠️ **Validierungsfehler**: ${error}`;
            
            default:
                return `❌ **Fehler**: ${error}`;
        }
    }

    function cancelAllHolidaysForDateRange(employeeId, startDate, endDate) {
        console.log(`Canceling all holidays for employee ${employeeId} from ${startDate} to ${endDate}`);
        
        // First, get all holidays for this employee and date range
        return fetch(`/timetracking/api/holidays/employee/${employeeId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to get employee holidays');
                }
                return response.json();
            })
            .then(holidays => {
                console.log('All employee holidays:', holidays);
                
                // Filter holidays that overlap with the date range
                const overlappingHolidays = holidays.filter(holiday => {
                    const holidayStart = new Date(holiday.startDate);
                    const holidayEnd = new Date(holiday.endDate);
                    const requestStart = new Date(startDate);
                    const requestEnd = new Date(endDate);
                    
                    // Check for overlap
                    return (holidayStart <= requestEnd && holidayEnd >= requestStart) &&
                           (holiday.status === 'PENDING' || holiday.status === 'APPROVED');
                });
                
                console.log('Overlapping holidays to cancel:', overlappingHolidays);
                
                if (overlappingHolidays.length === 0) {
                    console.log('No overlapping holidays found');
                    return Promise.resolve();
                }
                
                // Cancel all overlapping holidays
                const cancelPromises = overlappingHolidays.map(holiday => {
                    console.log(`Canceling holiday ${holiday.id}: ${holiday.startDate} to ${holiday.endDate} (${holiday.type})`);
                    return fetch(`/timetracking/api/holidays/${holiday.id}/cancel`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        }
                    });
                });
                
                return Promise.all(cancelPromises);
            });
    }

    // Initialize the app
    initializeApp();
});