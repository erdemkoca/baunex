document.addEventListener('DOMContentLoaded', function() {
    const el = document.getElementById('absences-app');
    if (!el) return;

    // Parse data
    let holidays = [];
    let employees = [];
    let pendingHolidays = [];
    let approvedHolidays = [];
    let rejectedHolidays = [];
    let employeeStats = [];
    let publicHolidays = [];
    let currentYear = 2025;
    try { holidays = JSON.parse(el.dataset.holidays || '[]'); } catch {};
    try { employees = JSON.parse(el.dataset.employees || '[]'); } catch {};
    try { pendingHolidays = JSON.parse(el.dataset.pendingHolidays || '[]'); } catch {};
    try { approvedHolidays = JSON.parse(el.dataset.approvedHolidays || '[]'); } catch {};
    try { rejectedHolidays = JSON.parse(el.dataset.rejectedHolidays || '[]'); } catch {};
    try { employeeStats = JSON.parse(el.dataset.employeeStats || '[]'); } catch {};
    try { publicHolidays = JSON.parse(el.dataset.publicHolidays || '[]'); } catch {};
    try { currentYear = parseInt(el.dataset.currentYear || '2025'); } catch {};

    // Debug logging
    console.log('Data loaded:', {
        holidays: holidays.length,
        employees: employees.length,
        pendingHolidays: pendingHolidays.length,
        approvedHolidays: approvedHolidays.length,
        rejectedHolidays: rejectedHolidays.length,
        employeeStats: employeeStats.length,
        publicHolidays: publicHolidays.length,
        currentYear: currentYear
    });
    console.log('Pending holidays:', pendingHolidays);
    console.log('Approved holidays:', approvedHolidays);
    console.log('Rejected holidays:', rejectedHolidays);

    // --- Stat Cards ---
    const statCards = [
        { icon: '🕒', number: pendingHolidays.length, label: 'Ausstehend', class: 'pending' },
        { icon: '✅', number: approvedHolidays.length, label: 'Genehmigt', class: 'approved' },
        { icon: '❌', number: rejectedHolidays.length, label: 'Abgelehnt', class: 'rejected' },
        { icon: '🎉', number: publicHolidays.length, label: 'Feiertage ' + currentYear, class: 'holidays' }
    ];
    const statCardsDiv = document.getElementById('stat-cards');
    if (statCardsDiv) {
        statCardsDiv.innerHTML = '<h2 class="section-header">Übersicht ' + currentYear + '</h2>' +
            '<div class="stats-grid">' +
            statCards.map(card =>
                '<div class="stat-card ' + card.class + '">' +
                    '<div class="icon">' + card.icon + '</div>' +
                    '<div class="number">' + card.number + '</div>' +
                    '<div class="label">' + card.label + '</div>' +
                '</div>'
            ).join('') +
            '</div>';
    }

    // --- Employee Vacation Accounts Table ---
    const employeeTableDiv = document.getElementById('employee-table-section');
    if (employeeTableDiv) {
        let table = '<h2 class="section-header">Mitarbeiter-Urlaubskonten</h2>';
        table += '<div class="employee-table-container">';
        table += '<table class="employee-table"><thead><tr>' +
            '<th>Mitarbeiter-Name</th>' +
            '<th>Gesamturlaubstage</th>' +
            '<th>Genommene Tage</th>' +
            '<th>Ausstehend</th>' +
            '<th>Verbleibend</th>' +
            '<th>Fortschritt</th>' +
            '</tr></thead><tbody>';
        employeeStats.forEach(emp => {
            const e = emp.employee;
            const percent = e.vacationDays > 0 ? Math.round((emp.approvedDays / e.vacationDays) * 100) : 0;
            table += '<tr>' +
                '<td><strong>' + e.firstName + ' ' + e.lastName + '</strong></td>' +
                '<td>' + e.vacationDays + '</td>' +
                '<td>' + emp.approvedDays + '</td>' +
                '<td>' + emp.pendingDays + '</td>' +
                '<td>' + emp.remainingDays + '</td>' +
                '<td>' +
                    '<div class="vacation-progress"><div class="progress-bar"><div class="progress-fill" style="width:' + percent + '%"></div></div>' +
                    '<span style="font-size:0.8rem;color:var(--text-muted);">' + percent + '%</span></div>' +
                '</td>' +
                '</tr>';
        });
        table += '</tbody></table></div>';
        employeeTableDiv.innerHTML = table;
    }

    // --- Calendar Section ---
    renderCalendar();
    function renderCalendar() {
        const calendarDiv = document.getElementById('calendar-section');
        if (!calendarDiv) return;
        
        let html = '<h2 class="section-header">Urlaubs-Kalender ' + currentYear + '</h2>';
        html += '<div class="calendar-container">';
        
        // Calendar filters
        html += '<div class="calendar-filters mb-3">';
        html += '<div class="filter-group">';
        html += '<label class="filter-label">Filter:</label>';
        html += '<div class="filter-options">';
        html += '<label class="filter-option"><input type="checkbox" id="show-approved" checked> <span class="filter-badge approved">Genehmigt</span></label>';
        html += '<label class="filter-option"><input type="checkbox" id="show-pending" checked> <span class="filter-badge pending">Ausstehend</span></label>';
        html += '<label class="filter-option"><input type="checkbox" id="show-rejected" checked> <span class="filter-badge rejected">Abgelehnt</span></label>';
        html += '<label class="filter-option"><input type="checkbox" id="show-holidays" checked> <span class="filter-badge holidays">Feiertage</span></label>';
        html += '</div>';
        html += '</div>';
        html += '<div class="month-navigation">';
        html += '<button class="btn btn-outline-secondary btn-sm" onclick="previousMonth()">‹</button>';
        html += '<span class="current-month" id="current-month">Januar ' + currentYear + '</span>';
        html += '<button class="btn btn-outline-secondary btn-sm" onclick="nextMonth()">›</button>';
        html += '</div>';
        html += '</div>';
        
        // Month chips
        html += '<div class="month-chips mb-3">';
        const months = ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'];
        months.forEach((month, index) => {
            html += '<button class="month-chip' + (index === 0 ? ' active' : '') + '" data-month="' + index + '">' + month + '</button>';
        });
        html += '</div>';
        
        // Calendar grid
        html += '<div class="calendar-grid" id="calendar-grid"></div>';
        html += '</div>';
        
        calendarDiv.innerHTML = html;
        
        // Add event listeners
        document.querySelectorAll('.month-chip').forEach(chip => {
            chip.addEventListener('click', function() {
                document.querySelectorAll('.month-chip').forEach(c => c.classList.remove('active'));
                this.classList.add('active');
                renderCalendarGrid(parseInt(this.dataset.month));
            });
        });
        
        document.querySelectorAll('.filter-option input').forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                renderCalendarGrid(getCurrentMonth());
            });
        });
        
        // Initial render
        renderCalendarGrid(0);
    }
    
    let currentMonthIndex = 0;
    
    function getCurrentMonth() {
        return currentMonthIndex;
    }
    
    function previousMonth() {
        if (currentMonthIndex > 0) {
            currentMonthIndex--;
            updateMonthDisplay();
            renderCalendarGrid(currentMonthIndex);
        }
    }
    
    function nextMonth() {
        if (currentMonthIndex < 11) {
            currentMonthIndex++;
            updateMonthDisplay();
            renderCalendarGrid(currentMonthIndex);
        }
    }
    
    function updateMonthDisplay() {
        const monthNames = ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'];
        document.getElementById('current-month').textContent = monthNames[currentMonthIndex] + ' ' + currentYear;
        
        document.querySelectorAll('.month-chip').forEach((chip, index) => {
            chip.classList.toggle('active', index === currentMonthIndex);
        });
    }
    
    function renderCalendarGrid(monthIndex) {
        const grid = document.getElementById('calendar-grid');
        if (!grid) return;
        
        const monthNames = ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'];
        const dayNames = ['Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'];
        
        // Get first day of month and number of days
        const firstDay = new Date(currentYear, monthIndex, 1);
        const lastDay = new Date(currentYear, monthIndex + 1, 0);
        const daysInMonth = lastDay.getDate();
        const startDayOfWeek = firstDay.getDay() || 7; // Convert Sunday (0) to 7
        
        let html = '<div class="calendar-header">';
        dayNames.forEach(day => {
            html += '<div class="calendar-day-header">' + day + '</div>';
        });
        html += '</div>';
        
        html += '<div class="calendar-body">';
        
        // Add empty cells for days before the first day of the month
        for (let i = 1; i < startDayOfWeek; i++) {
            html += '<div class="calendar-day empty"></div>';
        }
        
        // Add days of the month
        for (let day = 1; day <= daysInMonth; day++) {
            const date = new Date(currentYear, monthIndex, day);
            const dateString = date.toISOString().split('T')[0];
            const dayOfWeek = date.getDay() || 7;
            
            let dayClass = 'calendar-day';
            if (dayOfWeek === 6 || dayOfWeek === 7) dayClass += ' weekend';
            
            // Check for events on this day
            const events = getEventsForDate(dateString);
            if (events.length > 0) {
                dayClass += ' has-events';
            }
            
            html += '<div class="' + dayClass + '" data-date="' + dateString + '">';
            html += '<div class="day-number">' + day + '</div>';
            
            if (events.length > 0) {
                html += '<div class="day-events">';
                events.forEach(event => {
                    html += '<div class="event-dot ' + event.type + '" title="' + event.title + '"></div>';
                });
                html += '</div>';
            }
            
            html += '</div>';
        }
        
        html += '</div>';
        grid.innerHTML = html;
        
        // Add click handlers for days with events
        grid.querySelectorAll('.calendar-day.has-events').forEach(day => {
            day.addEventListener('click', function() {
                showDayDetails(this.dataset.date);
            });
        });
    }
    
    function getEventsForDate(dateString) {
        const events = [];
        
        // Check filters
        const showApproved = document.getElementById('show-approved')?.checked !== false;
        const showPending = document.getElementById('show-pending')?.checked !== false;
        const showRejected = document.getElementById('show-rejected')?.checked !== false;
        const showHolidays = document.getElementById('show-holidays')?.checked !== false;
        
        // Add holidays
        if (showHolidays) {
            holidays.forEach(holiday => {
                if (isDateInRange(dateString, holiday.startDate, holiday.endDate)) {
                    events.push({
                        type: 'holiday',
                        title: holiday.employeeName + ' - ' + holiday.type + ' (' + holiday.status + ')',
                        status: holiday.status
                    });
                }
            });
        }
        
        // Add public holidays
        if (showHolidays) {
            publicHolidays.forEach(holiday => {
                if (holiday.holidayDate === dateString) {
                    events.push({
                        type: 'public-holiday',
                        title: holiday.name + ' (Feiertag)',
                        status: 'PUBLIC'
                    });
                }
            });
        }
        
        return events;
    }
    
    function isDateInRange(date, startDate, endDate) {
        const checkDate = new Date(date);
        const start = new Date(startDate);
        const end = new Date(endDate);
        return checkDate >= start && checkDate <= end;
    }
    
    function showDayDetails(dateString) {
        const events = getEventsForDate(dateString);
        if (events.length === 0) return;
        
        let details = '<h5>Details für ' + formatDate(dateString) + '</h5><ul>';
        events.forEach(event => {
            details += '<li><strong>' + event.title + '</strong></li>';
        });
        details += '</ul>';
        
        alert(details);
    }
    
    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('de-DE', { 
            weekday: 'long', 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric' 
        });
    }
    
    // Make functions globally available
    window.previousMonth = previousMonth;
    window.nextMonth = nextMonth;

    // --- Absence Tabs & Cards ---
    renderAbsenceTabs();
    function renderAbsenceTabs() {
        const section = document.getElementById('absence-tabs-section');
        if (!section) return;
        section.innerHTML = '';
        // Tabs
        const tabs = [
            { key: 'pending', label: '🕒 Offen', count: pendingHolidays.length, badge: 'bg-warning text-dark' },
            { key: 'approved', label: '✅ Genehmigt', count: approvedHolidays.length, badge: 'bg-success' },
            { key: 'rejected', label: '❌ Abgelehnt', count: rejectedHolidays.length, badge: 'bg-danger' }
        ];
        let html = '<h2 class="section-header">Urlaubsanfragen</h2>';
        html += '<div id="absence-tabs" class="absence-tabs"><ul class="nav nav-tabs justify-content-center">';
        tabs.forEach((tab, i) => {
            html += '<li class="nav-item">' +
                '<a class="nav-link' + (i === 0 ? ' active' : '') + '" href="#" data-tab="' + tab.key + '" onclick="return false;">' +
                tab.label + ' <span class="badge ' + tab.badge + '">' + tab.count + '</span></a></li>';
        });
        html += '</ul></div>';
        html += '<div class="requests-section"><div class="requests-header"><h5 id="tab-title">Ausstehende Anfragen</h5></div>';
        html += '<div class="requests-grid" id="pending-requests"></div>';
        html += '<div class="requests-grid" id="approved-requests" style="display:none"></div>';
        html += '<div class="requests-grid" id="rejected-requests" style="display:none"></div>';
        html += '<div id="no-requests" class="requests-grid" style="display:none"><div class="text-center" style="grid-column:1/-1;padding:2rem;color:var(--text-muted);">Keine Anfragen in diesem Status</div></div>';
        html += '</div>';
        section.innerHTML = html;
        // Tab click handler
        section.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                section.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
                this.classList.add('active');
                showTab(this.getAttribute('data-tab'));
            });
        });
        showTab('pending');
    }
    function showTab(tab) {
        const tabs = ['pending', 'approved', 'rejected'];
        tabs.forEach(t => {
            document.getElementById(t + '-requests').style.display = (t === tab) ? 'grid' : 'none';
        });
        document.getElementById('tab-title').textContent =
            tab === 'pending' ? 'Ausstehende Anfragen' :
            tab === 'approved' ? 'Genehmigte Urlaube' : 'Abgelehnte Anfragen';
        // Render cards
        let data = tab === 'pending' ? pendingHolidays : tab === 'approved' ? approvedHolidays : rejectedHolidays;
        renderRequests(document.getElementById(tab + '-requests'), data, tab);
        document.getElementById('no-requests').style.display = (data.length === 0) ? 'grid' : 'none';
    }
    function renderRequests(container, requests, tabName) {
        container.innerHTML = '';
        requests.forEach(function(request) {
            var card = document.createElement('div');
            card.className = 'request-card ' + tabName;
            var badgeClass = getHolidayTypeClass(request.type);
            var badgeIcon = getHolidayTypeIcon(request.type);
            var cardContent = '<div class="request-header">' +
                '<h6 class="employee-name">' + request.employeeName + '</h6>' +
                '<span class="holiday-badge ' + badgeClass + '">' + badgeIcon + ' ' + request.type + '</span>' +
                '</div>';
            cardContent += '<div class="request-period">' + request.startDate + ' - ' + request.endDate + '</div>';
            if (request.reason) {
                cardContent += '<div class="request-reason"><strong>Grund:</strong> ' + request.reason + '</div>';
            }
            var dateLabel = '';
            var dateValue = '';
            if (tabName === 'pending') {
                dateLabel = 'Angefordert';
                dateValue = request.createdAt || request.startDate || 'Unbekannt';
            } else if (tabName === 'approved') {
                dateLabel = 'Genehmigt';
                dateValue = (request.approval && request.approval.approvedAt) ? request.approval.approvedAt : (request.startDate || 'Unbekannt');
            } else {
                dateLabel = 'Abgelehnt';
                dateValue = (request.approval && request.approval.approvedAt) ? request.approval.approvedAt : (request.startDate || 'Unbekannt');
            }
            cardContent += '<div class="request-date"><strong>' + dateLabel + ' am:</strong> ' + dateValue + '</div>';
            if (tabName === 'pending') {
                cardContent += '<div class="request-actions">' +
                    '<button class="btn btn-success btn-sm" onclick="approveRequest(' + request.id + ')">Genehmigen</button>' +
                    '<button class="btn btn-danger btn-sm" onclick="rejectRequest(' + request.id + ')">Ablehnen</button>' +
                    '</div>';
            }
            card.innerHTML = cardContent;
            container.appendChild(card);
        });
    }
    function getHolidayTypeClass(type) {
        var typeLower = type.toLowerCase();
        if (typeLower.includes('urlaub') || typeLower.includes('vacation')) return 'vacation';
        if (typeLower.includes('unbezahlt') || typeLower.includes('unpaid')) return 'unpaid';
        if (typeLower.includes('krank') || typeLower.includes('sick')) return 'sick';
        return 'vacation';
    }
    function getHolidayTypeIcon(type) {
        var typeLower = type.toLowerCase();
        if (typeLower.includes('urlaub') || typeLower.includes('vacation')) return '🟢';
        if (typeLower.includes('unbezahlt') || typeLower.includes('unpaid')) return '🔴';
        if (typeLower.includes('krank') || typeLower.includes('sick')) return '💊';
        return '🟢';
    }
    window.approveRequest = async function(requestId) {
        if (!confirm('Möchten Sie diesen Urlaubsantrag genehmigen?')) return;
        try {
            var url = '/timetracking/api/holidays/' + requestId + '/approve';
            var response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ approval: { status: 'APPROVED', approverId: 1, approvedAt: new Date().toISOString().split('T')[0] } })
            });
            if (response.ok) {
                alert('Urlaubsantrag erfolgreich genehmigt!');
                window.location.reload();
            } else {
                var errorText = await response.text();
                alert('Fehler beim Genehmigen: ' + errorText);
            }
        } catch (error) {
            alert('Fehler beim Genehmigen des Urlaubsantrags');
        }
    };
    window.rejectRequest = async function(requestId) {
        if (!confirm('Möchten Sie diesen Urlaubsantrag ablehnen?')) return;
        try {
            var url = '/timetracking/api/holidays/' + requestId + '/approve';
            var response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ approval: { status: 'REJECTED', approverId: 1, approvedAt: new Date().toISOString().split('T')[0] } })
            });
            if (response.ok) {
                alert('Urlaubsantrag erfolgreich abgelehnt!');
                window.location.reload();
            } else {
                var errorText = await response.text();
                alert('Fehler beim Ablehnen: ' + errorText);
            }
        } catch (error) {
            alert('Fehler beim Ablehnen des Urlaubsantrags');
        }
    };

    // --- Modal Functionality ---
    const addAbsenceBtn = document.getElementById('add-absence-btn');
    const absenceModal = document.getElementById('absenceModal');
    const absenceForm = document.getElementById('absenceForm');
    const saveAbsenceBtn = document.getElementById('save-absence-btn');
    const employeeSelect = document.getElementById('absence-employee');
    const typeSelect = document.getElementById('absence-type');
    const startDateInput = document.getElementById('absence-start-date');
    const endDateInput = document.getElementById('absence-end-date');
    const reasonInput = document.getElementById('absence-reason');

    // Load holiday types when modal opens
    async function loadHolidayTypes() {
        try {
            const response = await fetch('/api/holiday-types');
            if (response.ok) {
                const types = await response.json();
                typeSelect.innerHTML = '<option value="">-- Bitte wählen --</option>';
                types.forEach(type => {
                    typeSelect.innerHTML += `<option value="${type.code}">${type.displayName}</option>`;
                });
            } else {
                console.error('Failed to load holiday types:', response.status);
                // Fallback to default types
                const defaultTypes = [
                    { code: 'PAID_VACATION', displayName: 'Bezahlter Urlaub' },
                    { code: 'UNPAID_LEAVE', displayName: 'Unbezahlter Urlaub' },
                    { code: 'SICK_LEAVE', displayName: 'Krankheit' },
                    { code: 'SPECIAL_LEAVE', displayName: 'Sonderurlaub' }
                ];
                typeSelect.innerHTML = '<option value="">-- Bitte wählen --</option>';
                defaultTypes.forEach(type => {
                    typeSelect.innerHTML += `<option value="${type.code}">${type.displayName}</option>`;
                });
            }
        } catch (error) {
            console.error('Error loading holiday types:', error);
            // Fallback to default types
            const defaultTypes = [
                { code: 'PAID_VACATION', displayName: 'Bezahlter Urlaub' },
                { code: 'UNPAID_LEAVE', displayName: 'Unbezahlter Urlaub' },
                { code: 'SICK_LEAVE', displayName: 'Krankheit' },
                { code: 'SPECIAL_LEAVE', displayName: 'Sonderurlaub' }
            ];
            typeSelect.innerHTML = '<option value="">-- Bitte wählen --</option>';
            defaultTypes.forEach(type => {
                typeSelect.innerHTML += `<option value="${type.code}">${type.displayName}</option>`;
            });
        }
    }

    // Populate employee select
    function populateEmployeeSelect() {
        employeeSelect.innerHTML = '<option value="">-- Bitte wählen --</option>';
        employees.forEach(employee => {
            employeeSelect.innerHTML += `<option value="${employee.id}">${employee.firstName} ${employee.lastName}</option>`;
        });
    }

    // Open modal
    addAbsenceBtn.addEventListener('click', function() {
        // Prefill current year
        const today = new Date();
        startDateInput.value = today.toISOString().split('T')[0];
        endDateInput.value = today.toISOString().split('T')[0];
        
        // Load data
        populateEmployeeSelect();
        loadHolidayTypes();
        
        // Show modal
        const modal = new bootstrap.Modal(absenceModal);
        modal.show();
    });

    // Save absence
    saveAbsenceBtn.addEventListener('click', async function() {
        if (!absenceForm.checkValidity()) {
            absenceForm.reportValidity();
            return;
        }

        const formData = {
            employeeId: parseInt(employeeSelect.value),
            type: typeSelect.value,
            startDate: startDateInput.value,
            endDate: endDateInput.value,
            reason: reasonInput.value || null
        };

        saveAbsenceBtn.disabled = true;
        saveAbsenceBtn.innerHTML = '<i class="bi bi-check"></i> Speichere...';

        try {
            const response = await fetch('/timetracking/api/holidays', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData)
            });

            if (response.ok) {
                // Show success message
                showSuccessMessage('Abwesenheit erfolgreich erfasst!');
                
                // Close modal
                const modal = bootstrap.Modal.getInstance(absenceModal);
                modal.hide();
                
                // Reset form
                absenceForm.reset();
                
                // Reload page to show new data
                setTimeout(() => window.location.reload(), 1500);
            } else {
                const errorText = await response.text();
                alert('Fehler beim Speichern: ' + errorText);
            }
        } catch (error) {
            alert('Fehler beim Speichern der Abwesenheit');
        } finally {
            saveAbsenceBtn.disabled = false;
            saveAbsenceBtn.innerHTML = '<i class="bi bi-check"></i> Abwesenheit speichern';
        }
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
});