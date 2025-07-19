// Stundenkonto App - Interactive functionality only
console.log('Stundenkonto.js loaded');

// Debug function to check monthly data
function debugMonthlyData() {
    console.log('=== DEBUG: Checking Monthly Data ===');
    
    // Check if monthly overview exists
    const monthlyOverview = document.querySelector('.monthly-overview');
    if (monthlyOverview) {
        console.log('✓ Monthly overview found');
        
        // Count month sections
        const monthSections = monthlyOverview.querySelectorAll('.month-section');
        console.log(`Found ${monthSections.length} month sections`);
        
        // Log each month
        monthSections.forEach((section, index) => {
            const monthName = section.querySelector('.month-name');
            const monthBalance = section.querySelector('.month-balance-box');
            console.log(`Month ${index + 1}: ${monthName?.textContent?.trim()} - Balance: ${monthBalance?.textContent?.trim()}`);
        });
    } else {
        console.log('✗ Monthly overview not found');
    }
    
    // Check for any hidden elements
    const hiddenElements = document.querySelectorAll('.month-section[style*="display: none"], .month-section[style*="visibility: hidden"]');
    console.log(`Found ${hiddenElements.length} hidden month sections`);
    
    // Check CSS that might be hiding elements
    const computedStyles = [];
    const monthSections = document.querySelectorAll('.month-section');
    monthSections.forEach((section, index) => {
        const style = window.getComputedStyle(section);
        if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') {
            computedStyles.push(`Month ${index + 1}: display=${style.display}, visibility=${style.visibility}, opacity=${style.opacity}`);
        }
    });
    if (computedStyles.length > 0) {
        console.log('Hidden elements by CSS:', computedStyles);
    }
    
    console.log('=== END DEBUG ===');
}

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
    
    // Run debug after a short delay to ensure everything is rendered
    setTimeout(() => {
        debugMonthlyData();
    }, 1000);
});

// Also try to initialize immediately if DOM is already loaded
if (document.readyState === 'loading') {
    console.log('DOM still loading, waiting for DOMContentLoaded');
} else {
    console.log('DOM already loaded, initializing immediately');
    new StundenkontoApp();
} 