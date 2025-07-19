class HolidaySettingsApp {
    constructor() {
        this.currentYear = new Date().getFullYear();
        this.holidays = [];
        this.holidayModal = null;
        this.init();
    }

    init() {
        this.holidayModal = new bootstrap.Modal(document.getElementById('holidayModal'));
        this.loadHolidays(this.currentYear);
        this.setupEventListeners();
    }

    setupEventListeners() {
        this.holidayModal = new bootstrap.Modal(document.getElementById('holidayModal'));
        
        // Add year navigation event listeners
        document.getElementById('prevYearBtn').addEventListener('click', () => {
            this.loadHolidays(this.currentYear - 1);
        });
        
        document.getElementById('nextYearBtn').addEventListener('click', () => {
            this.loadHolidays(this.currentYear + 1);
        });
        
        // Load initial year
        this.loadHolidays(this.currentYear);
    }

    async loadHolidays(year) {
        this.showLoading(true);
        this.currentYear = year;
        
        try {
            const response = await fetch(`/timetracking/api/holiday-definitions/year/${year}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const data = await response.json();
            this.holidays = Array.isArray(data.holidays) ? data.holidays : [];
            this.updateYearButton();
            this.renderHolidays();
        } catch (error) {
            console.error('Error loading holidays:', error);
            this.showError('Fehler beim Laden der Feiertage');
        } finally {
            this.showLoading(false);
        }
    }

    async generateHolidays() {
        if (!confirm(`Möchten Sie alle Schweizer Feiertage für das Jahr ${this.currentYear} automatisch generieren?`)) {
            return;
        }

        this.showLoading(true);
        
        try {
            const response = await fetch(`/timetracking/api/holiday-definitions/generate/${this.currentYear}`, {
                method: 'POST'
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            this.showSuccess('Feiertage erfolgreich generiert');
            await this.loadHolidays(this.currentYear);
        } catch (error) {
            console.error('Error generating holidays:', error);
            this.showError('Fehler beim Generieren der Feiertage');
        } finally {
            this.showLoading(false);
        }
    }

    renderHolidays() {
        const tbody = document.getElementById('holidayTableBody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (this.holidays.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted">
                        <i class="bi bi-calendar-x"></i> Keine Feiertage für ${this.currentYear} gefunden
                    </td>
                </tr>
            `;
            return;
        }

        this.holidays.forEach(holiday => {
            const row = this.createHolidayRow(holiday);
            tbody.appendChild(row);
        });
    }

    createHolidayRow(holiday) {
        const row = document.createElement('tr');
        // Remove the conditional styling - all holidays are now editable
        row.className = 'editable-holiday';
        
        const date = new Date(holiday.date);
        const formattedDate = date.toLocaleDateString('de-CH', {
            weekday: 'short',
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        });

        const typeBadge = this.getTypeBadge(holiday.holidayType);
        const workFreeIcon = holiday.isWorkFree ? 
            '<i class="bi bi-check-circle text-success"></i>' : 
            '<i class="bi bi-x-circle text-danger"></i>';
        
        const statusBadge = holiday.active ? 
            '<span class="badge bg-success">Aktiv</span>' : 
            '<span class="badge bg-secondary">Inaktiv</span>';

        row.innerHTML = `
            <td><strong>${formattedDate}</strong></td>
            <td>${holiday.name}</td>
            <td>${typeBadge}</td>
            <td>${holiday.canton || '-'}</td>
            <td>${workFreeIcon}</td>
            <td>${statusBadge}</td>
            <td>
                <div class="btn-group btn-group-sm" role="group">
                    <button type="button" class="btn btn-outline-primary" onclick="holidaySettingsApp.editHoliday(${holiday.id})">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button type="button" class="btn btn-outline-danger" onclick="holidaySettingsApp.deleteHoliday(${holiday.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        `;

        return row;
    }

    getTypeBadge(type) {
        const badgeClasses = {
            'Öffentlicher Feiertag': 'bg-primary',
            'Kantonaler Feiertag': 'bg-info',
            'Betriebsfeiertag': 'bg-warning',
            'Benutzerdefinierter Feiertag': 'bg-secondary'
        };
        
        const badgeClass = badgeClasses[type] || 'bg-secondary';
        return `<span class="badge ${badgeClass} holiday-type-badge">${type}</span>`;
    }

    updateYearButton() {
        const yearBtn = document.getElementById('currentYearBtn');
        const prevYearText = document.getElementById('prevYearText');
        const nextYearText = document.getElementById('nextYearText');
        
        if (yearBtn) {
            yearBtn.textContent = this.currentYear;
        }
        
        if (prevYearText) {
            prevYearText.textContent = this.currentYear - 1;
        }
        
        if (nextYearText) {
            nextYearText.textContent = this.currentYear + 1;
        }
    }

    addHoliday() {
        this.clearForm();
        document.getElementById('modalTitle').textContent = 'Feiertag hinzufügen';
        document.getElementById('holidayDate').value = `${this.currentYear}-01-01`;
        this.holidayModal.show();
    }

    async editHoliday(id) {
        const holiday = this.holidays.find(h => h.id === id);
        if (!holiday) return;

        this.clearForm();
        document.getElementById('modalTitle').textContent = 'Feiertag bearbeiten';
        
        // Fill form with holiday data
        document.getElementById('holidayId').value = holiday.id;
        document.getElementById('holidayDate').value = holiday.date;
        document.getElementById('holidayName').value = holiday.name;
        document.getElementById('holidayType').value = holiday.holidayType;
        document.getElementById('holidayCanton').value = holiday.canton || '';
        document.getElementById('isWorkFree').checked = holiday.isWorkFree;
        document.getElementById('holidayDescription').value = holiday.description || '';
        
        this.holidayModal.show();
    }

    async saveHoliday() {
        const form = document.getElementById('holidayForm');
        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

        const holidayData = {
            id: document.getElementById('holidayId').value || null,
            year: this.currentYear,
            date: document.getElementById('holidayDate').value,
            name: document.getElementById('holidayName').value,
            holidayType: document.getElementById('holidayType').value,
            canton: document.getElementById('holidayCanton').value || null,
            isWorkFree: document.getElementById('isWorkFree').checked,
            description: document.getElementById('holidayDescription').value || null,
            isFixed: false,
            isEditable: true,
            active: true
        };

        try {
            const url = holidayData.id ? 
                `/timetracking/api/holiday-definitions/${holidayData.id}` : 
                '/timetracking/api/holiday-definitions';
            
            const method = holidayData.id ? 'PUT' : 'POST';
            
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(holidayData)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            this.holidayModal.hide();
            this.showSuccess(holidayData.id ? 'Feiertag erfolgreich aktualisiert' : 'Feiertag erfolgreich erstellt');
            await this.loadHolidays(this.currentYear);
        } catch (error) {
            console.error('Error saving holiday:', error);
            this.showError('Fehler beim Speichern des Feiertags');
        }
    }

    async deleteHoliday(id) {
        const holiday = this.holidays.find(h => h.id === id);
        if (!holiday) return;

        if (!confirm(`Möchten Sie den Feiertag "${holiday.name}" wirklich löschen?`)) {
            return;
        }

        try {
            const response = await fetch(`/timetracking/api/holiday-definitions/${id}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            this.showSuccess('Feiertag erfolgreich gelöscht');
            await this.loadHolidays(this.currentYear);
        } catch (error) {
            console.error('Error deleting holiday:', error);
            this.showError('Fehler beim Löschen des Feiertags');
        }
    }

    clearForm() {
        document.getElementById('holidayForm').reset();
        document.getElementById('holidayId').value = '';
        document.getElementById('holidayDate').value = '';
        document.getElementById('holidayName').value = '';
        document.getElementById('holidayType').value = 'Öffentlicher Feiertag';
        document.getElementById('holidayCanton').value = '';
        document.getElementById('isWorkFree').checked = true;
        document.getElementById('holidayDescription').value = '';
    }

    showLoading(show) {
        const spinner = document.getElementById('loadingSpinner');
        const table = document.getElementById('holidayTable');
        
        if (spinner && table) {
            if (show) {
                spinner.classList.remove('d-none');
                table.classList.add('d-none');
            } else {
                spinner.classList.add('d-none');
                table.classList.remove('d-none');
            }
        }
    }

    showSuccess(message) {
        this.showAlert(message, 'success');
    }

    showError(message) {
        this.showAlert(message, 'danger');
    }

    showAlert(message, type) {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        const container = document.querySelector('.container-fluid');
        container.insertBefore(alertDiv, container.firstChild);
        
        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 5000);
    }
}

// Global functions for onclick handlers
let holidaySettingsApp;

document.addEventListener('DOMContentLoaded', function() {
    holidaySettingsApp = new HolidaySettingsApp();
});

// Keep these global functions for the buttons that still use onclick
function generateHolidays() {
    if (holidaySettingsApp) {
        holidaySettingsApp.generateHolidays();
    }
}

function addHoliday() {
    if (holidaySettingsApp) {
        holidaySettingsApp.addHoliday();
    }
}

function saveHoliday() {
    if (holidaySettingsApp) {
        holidaySettingsApp.saveHoliday();
    }
} 