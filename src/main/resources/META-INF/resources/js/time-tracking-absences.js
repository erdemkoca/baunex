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
        console.log('Absences app initialized successfully');
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
        
        const firstDay = new Date(appState.currentYear, appState.currentMonth, 1);
        const lastDay = new Date(appState.currentYear, appState.currentMonth + 1, 0);
        const startDate = new Date(firstDay);
        startDate.setDate(startDate.getDate() - firstDay.getDay() + 1);
        
        const days = generateCalendarDays(startDate, lastDay.getDate(), appState.currentYear, appState.currentMonth);
        
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

    function generateCalendarDays(startDate, lastDay, year, month) {
        const days = [];
        const currentDate = new Date(startDate);
        
        // Generate 6 weeks (42 days) to ensure full calendar display
        for (let i = 0; i < 42; i++) {
            const dayNumber = currentDate.getDate();
            const isCurrentMonth = currentDate.getMonth() === month && currentDate.getFullYear() === year;
            const isWeekend = currentDate.getDay() === 0 || currentDate.getDay() === 6;
            
            let events = '';
            let hasEvents = false;
            
            if (isCurrentMonth) {
                const dateString = currentDate.toISOString().split('T')[0];
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
            
            currentDate.setDate(currentDate.getDate() + 1);
        }
        
        return days;
    }

    function getEventsForDate(dateString) {
        const events = [];
        
        // Check holidays for this date
        appState.holidays.forEach(holiday => {
            const startDate = new Date(holiday.startDate);
            const endDate = new Date(holiday.endDate);
            const checkDate = new Date(dateString);
            
            if (checkDate >= startDate && checkDate <= endDate) {
                const employee = appState.employees.find(emp => emp.id === holiday.employeeId);
                const employeeName = employee ? `${employee.firstName} ${employee.lastName}` : 'Unbekannt';
                const color = appState.employeeColors[holiday.employeeId] || '#6c757d';
                
                const isStart = checkDate.getTime() === startDate.getTime();
                const isEnd = checkDate.getTime() === endDate.getTime();
                
                events.push(`
                    <div class="holiday-bar ${isStart ? 'start' : ''} ${isEnd ? 'end' : ''}" 
                         style="background-color: ${color};"
                         title="${employeeName} - ${holiday.type}">
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

    function setupEventListeners() {
        // Employee search
        document.getElementById('employee-search')?.addEventListener('input', function(e) {
            filterEmployees(null, e.target.value);
        });

        // Banner event listeners
        document.getElementById('review-now-btn')?.addEventListener('click', function() {
            // Scroll to pending requests section
            const requestsSection = document.getElementById('holiday-requests-section');
            if (requestsSection) {
                requestsSection.scrollIntoView({ behavior: 'smooth' });
                // Activate pending tab
                const pendingTab = document.getElementById('pending-tab');
                if (pendingTab) {
                    pendingTab.click();
                }
            }
        });

        document.getElementById('dismiss-banner-btn')?.addEventListener('click', function() {
            appState.bannerDismissed = true;
            const banner = document.getElementById('pending-requests-banner');
            if (banner) {
                banner.style.display = 'none';
            }
        });
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

    // Initialize the app
    initializeApp();
});