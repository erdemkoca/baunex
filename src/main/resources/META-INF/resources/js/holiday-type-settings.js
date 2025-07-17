class HolidayTypeSettingsApp {
    constructor() {
        this.holidayTypes = [];
        this.holidayTypeModal = null;
        this.currentEditId = null;
    }

    init() {
        this.holidayTypeModal = new bootstrap.Modal(document.getElementById('holidayTypeModal'));
        this.loadHolidayTypes();
        this.setupEventListeners();
    }

    setupEventListeners() {
        // Form validation
        document.getElementById('holidayTypeForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.saveHolidayType();
        });

        // Auto-generate code from display name
        document.getElementById('holidayTypeDisplayName').addEventListener('input', (e) => {
            const displayName = e.target.value;
            const code = this.generateCodeFromDisplayName(displayName);
            document.getElementById('holidayTypeCode').value = code;
        });
    }

    generateCodeFromDisplayName(displayName) {
        if (!displayName) return '';
        
        // Convert to uppercase and replace spaces/special chars with underscores
        return displayName
            .toUpperCase()
            .replace(/[^A-ZÄÖÜß\s]/g, '') // Remove special chars except German umlauts
            .replace(/Ä/g, 'AE')
            .replace(/Ö/g, 'OE')
            .replace(/Ü/g, 'UE')
            .replace(/ß/g, 'SS')
            .replace(/\s+/g, '_') // Replace spaces with underscores
            .replace(/_+/g, '_') // Replace multiple underscores with single
            .replace(/^_|_$/g, ''); // Remove leading/trailing underscores
    }

    async loadHolidayTypes() {
        this.showLoading(true);
        
        try {
            const response = await fetch('/api/holiday-types/all');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            this.holidayTypes = await response.json();
            this.renderHolidayTypes();
        } catch (error) {
            console.error('Error loading holiday types:', error);
            this.showError('Fehler beim Laden der Abwesenheitstypen');
        } finally {
            this.showLoading(false);
        }
    }

    renderHolidayTypes() {
        const tbody = document.getElementById('holidayTypeTableBody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (this.holidayTypes.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">
                        <i class="bi bi-inbox"></i> Keine Abwesenheitstypen gefunden
                    </td>
                </tr>
            `;
            return;
        }

        this.holidayTypes.forEach(type => {
            const row = document.createElement('tr');
            row.className = type.isSystemType ? 'system-type' : 'custom-type';
            
            row.innerHTML = `
                <td>
                    <strong>${type.displayName}</strong>
                </td>
                <td class="expected-hours-cell">
                    ${type.defaultExpectedHours.toFixed(1)}h
                </td>
                <td>
                    ${type.description || '<span class="text-muted">-</span>'}
                </td>
                <td class="sort-order-cell">
                    ${type.sortOrder}
                </td>
                <td>
                    ${this.getStatusBadge(type.active)}
                </td>
                <td>
                    ${this.getTypeBadge(type.isSystemType)}
                </td>
                <td>
                    ${this.getActionButtons(type)}
                </td>
            `;
            
            tbody.appendChild(row);
        });
    }

    getStatusBadge(active) {
        if (active) {
            return '<span class="badge bg-success status-badge">Aktiv</span>';
        } else {
            return '<span class="badge bg-secondary status-badge">Inaktiv</span>';
        }
    }

    getTypeBadge(isSystemType) {
        if (isSystemType) {
            return '<span class="badge bg-primary status-badge">System</span>';
        } else {
            return '<span class="badge bg-info status-badge">Benutzer</span>';
        }
    }

    getActionButtons(type) {
        const buttons = [];
        
        // Edit button
        if (!type.isSystemType) {
            buttons.push(`
                <button class="btn btn-sm btn-outline-primary" onclick="editHolidayType(${type.id})" title="Bearbeiten">
                    <i class="bi bi-pencil"></i>
                </button>
            `);
        }
        
        // Activate/Deactivate button
        if (!type.isSystemType) {
            if (type.active) {
                buttons.push(`
                    <button class="btn btn-sm btn-outline-warning" onclick="deactivateHolidayType(${type.id})" title="Deaktivieren">
                        <i class="bi bi-pause-circle"></i>
                    </button>
                `);
            } else {
                buttons.push(`
                    <button class="btn btn-sm btn-outline-success" onclick="activateHolidayType(${type.id})" title="Aktivieren">
                        <i class="bi bi-play-circle"></i>
                    </button>
                `);
            }
        }
        
        return buttons.join(' ');
    }

    addHolidayType() {
        this.currentEditId = null;
        document.getElementById('modalTitle').textContent = 'Abwesenheitstyp hinzufügen';
        document.getElementById('holidayTypeForm').reset();
        document.getElementById('holidayTypeCode').value = '';
        document.getElementById('activeCheckboxContainer').style.display = 'block';
        this.holidayTypeModal.show();
    }

    editHolidayType(id) {
        const type = this.holidayTypes.find(t => t.id === id);
        if (!type) return;

        this.currentEditId = id;
        document.getElementById('modalTitle').textContent = 'Abwesenheitstyp bearbeiten';
        
        // Fill form
        document.getElementById('holidayTypeId').value = type.id;
        document.getElementById('holidayTypeCode').value = type.code;
        document.getElementById('holidayTypeDisplayName').value = type.displayName;
        document.getElementById('holidayTypeExpectedHours').value = type.defaultExpectedHours;
        document.getElementById('holidayTypeDescription').value = type.description || '';
        document.getElementById('holidayTypeSortOrder').value = type.sortOrder;
        document.getElementById('holidayTypeActive').checked = type.active;
        
        this.holidayTypeModal.show();
    }

    async saveHolidayType() {
        const form = document.getElementById('holidayTypeForm');
        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

        const holidayTypeData = {
            code: document.getElementById('holidayTypeCode').value,
            displayName: document.getElementById('holidayTypeDisplayName').value,
            defaultExpectedHours: parseFloat(document.getElementById('holidayTypeExpectedHours').value),
            description: document.getElementById('holidayTypeDescription').value || null,
            sortOrder: parseInt(document.getElementById('holidayTypeSortOrder').value) || 1
        };

        try {
            let url, method;
            
            if (this.currentEditId) {
                // Update existing
                url = `/api/holiday-types/${this.currentEditId}`;
                method = 'PUT';
                holidayTypeData.active = document.getElementById('holidayTypeActive').checked;
            } else {
                // Create new
                url = '/api/holiday-types';
                method = 'POST';
            }
            
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(holidayTypeData)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || `HTTP error! status: ${response.status}`);
            }

            this.holidayTypeModal.hide();
            this.showSuccess(this.currentEditId ? 'Abwesenheitstyp erfolgreich aktualisiert' : 'Abwesenheitstyp erfolgreich erstellt');
            await this.loadHolidayTypes();
        } catch (error) {
            console.error('Error saving holiday type:', error);
            this.showError('Fehler beim Speichern: ' + error.message);
        }
    }

    async activateHolidayType(id) {
        try {
            const response = await fetch(`/api/holiday-types/${id}/activate`, {
                method: 'POST'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            this.showSuccess('Abwesenheitstyp erfolgreich aktiviert');
            await this.loadHolidayTypes();
        } catch (error) {
            console.error('Error activating holiday type:', error);
            this.showError('Fehler beim Aktivieren des Abwesenheitstyps');
        }
    }

    async deactivateHolidayType(id) {
        try {
            const response = await fetch(`/api/holiday-types/${id}/deactivate`, {
                method: 'POST'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            this.showSuccess('Abwesenheitstyp erfolgreich deaktiviert');
            await this.loadHolidayTypes();
        } catch (error) {
            console.error('Error deactivating holiday type:', error);
            this.showError('Fehler beim Deaktivieren des Abwesenheitstyps');
        }
    }



    showLoading(show) {
        const spinner = document.getElementById('loadingSpinner');
        const table = document.getElementById('holidayTypeTable');
        
        if (show) {
            spinner.classList.remove('d-none');
            table.classList.add('d-none');
        } else {
            spinner.classList.add('d-none');
            table.classList.remove('d-none');
        }
    }

    showSuccess(message) {
        // Create success toast
        const toast = document.createElement('div');
        toast.className = 'toast align-items-center text-bg-success border-0 position-fixed top-0 end-0 m-3';
        toast.style.zIndex = '9999';
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi bi-check-circle me-2"></i>${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;
        
        document.body.appendChild(toast);
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();
        
        // Remove toast after it's hidden
        toast.addEventListener('hidden.bs.toast', () => {
            document.body.removeChild(toast);
        });
    }

    showError(message) {
        // Create error toast
        const toast = document.createElement('div');
        toast.className = 'toast align-items-center text-bg-danger border-0 position-fixed top-0 end-0 m-3';
        toast.style.zIndex = '9999';
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi bi-exclamation-triangle me-2"></i>${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;
        
        document.body.appendChild(toast);
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();
        
        // Remove toast after it's hidden
        toast.addEventListener('hidden.bs.toast', () => {
            document.body.removeChild(toast);
        });
    }
}

// Global functions for onclick handlers
window.addHolidayType = function() {
    app.addHolidayType();
};

window.editHolidayType = function(id) {
    app.editHolidayType(id);
};

window.saveHolidayType = function() {
    app.saveHolidayType();
};

window.activateHolidayType = function(id) {
    app.activateHolidayType(id);
};

window.deactivateHolidayType = function(id) {
    app.deactivateHolidayType(id);
};



// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.app = new HolidayTypeSettingsApp();
    window.app.init();
}); 