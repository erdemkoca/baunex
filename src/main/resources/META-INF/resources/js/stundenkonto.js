// Stundenkonto App - Interactive functionality only
console.log('Stundenkonto.js loaded');

class StundenkontoApp {
    constructor() {
        console.log('StundenkontoApp constructor called');
        this.bindEvents();
    }

    bindEvents() {
        console.log('Binding events');
        
        // Employee selector
        const employeeSelect = document.getElementById('employee-select');
        if (employeeSelect) {
            employeeSelect.addEventListener('change', (e) => {
                const employeeId = e.target.value;
                const yearSelect = document.getElementById('year-select');
                const year = yearSelect ? yearSelect.value : new Date().getFullYear();
                
                if (employeeId) {
                    window.location.href = `/timetracking/stundenkonto?employeeId=${employeeId}&year=${year}`;
                } else {
                    window.location.href = `/timetracking/stundenkonto?year=${year}`;
                }
            });
        }

        // Year selector
        const yearSelect = document.getElementById('year-select');
        if (yearSelect) {
            yearSelect.addEventListener('change', (e) => {
                const year = e.target.value;
                const employeeSelect = document.getElementById('employee-select');
                const employeeId = employeeSelect ? employeeSelect.value : '';
                
                if (employeeId) {
                    window.location.href = `/timetracking/stundenkonto?employeeId=${employeeId}&year=${year}`;
                } else {
                    window.location.href = `/timetracking/stundenkonto?year=${year}`;
                }
            });
        }
    }
}

// Initialize the app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM loaded, initializing StundenkontoApp');
    new StundenkontoApp();
});

// Also try to initialize immediately if DOM is already loaded
if (document.readyState === 'loading') {
    console.log('DOM still loading, waiting for DOMContentLoaded');
} else {
    console.log('DOM already loaded, initializing immediately');
    new StundenkontoApp();
} 