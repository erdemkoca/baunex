// Baunex Business Management JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Highlight active menu item
    highlightActiveMenu();
    
    // Add confirmation to delete buttons
    setupDeleteConfirmations();
    
    // Add form validation
    setupFormValidation();
    
    // Add table sorting
    setupTableSorting();
});

// Highlight the active menu item based on current URL
function highlightActiveMenu() {
    const path = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-link');
    
    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        // Special case for dashboard
        if (href === '/dashboard' && (path === '/' || path === '/dashboard')) {
            link.classList.add('active');
        }
        // For other links
        else if (href === path || 
            (href !== '/dashboard' && href !== '/' && path.startsWith(href))) {
            link.classList.add('active');
        }
    });
}

// Setup confirmation dialogs for delete actions
function setupDeleteConfirmations() {
    const deleteLinks = document.querySelectorAll('a[href*="delete"]');
    
    deleteLinks.forEach(link => {
        if (!link.hasAttribute('onclick')) {
            link.setAttribute('onclick', 'return confirm("Are you sure you want to delete this item?")');
        }
    });
}

// Setup form validation for all forms
function setupFormValidation() {
    const forms = document.querySelectorAll('form');
    
    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
                
                // Highlight all invalid fields
                const invalidFields = form.querySelectorAll(':invalid');
                invalidFields.forEach(field => {
                    field.classList.add('is-invalid');
                    
                    // Add event listener to remove invalid class when field is changed
                    field.addEventListener('input', function() {
                        if (field.checkValidity()) {
                            field.classList.remove('is-invalid');
                        }
                    });
                });
                
                // Show validation message
                const firstInvalid = invalidFields[0];
                if (firstInvalid) {
                    firstInvalid.focus();
                    
                    // Create alert if it doesn't exist
                    if (!document.querySelector('.validation-alert')) {
                        const alert = document.createElement('div');
                        alert.className = 'alert alert-danger validation-alert';
                        alert.innerHTML = 'Please fill in all required fields correctly.';
                        form.prepend(alert);
                        
                        // Remove alert when any field is changed
                        form.addEventListener('input', function() {
                            const existingAlert = document.querySelector('.validation-alert');
                            if (existingAlert) {
                                existingAlert.remove();
                            }
                        }, { once: true });
                    }
                }
            }
        });
    });
}

// Setup table sorting functionality
function setupTableSorting() {
    const tables = document.querySelectorAll('.table');
    
    tables.forEach(table => {
        const headers = table.querySelectorAll('th');
        
        headers.forEach((header, index) => {
            // Add sort indicator and cursor pointer
            header.style.cursor = 'pointer';
            header.innerHTML += ' <span class="sort-indicator"></span>';
            
            // Add click event for sorting
            header.addEventListener('click', function() {
                sortTable(table, index);
                
                // Update sort indicators
                headers.forEach(h => {
                    h.querySelector('.sort-indicator').textContent = '';
                });
                
                const indicator = header.querySelector('.sort-indicator');
                if (header.asc) {
                    indicator.textContent = ' ↑';
                } else {
                    indicator.textContent = ' ↓';
                }
            });
        });
    });
}

// Sort table function
function sortTable(table, column) {
    const tbody = table.querySelector('tbody');
    const rows = Array.from(tbody.querySelectorAll('tr'));
    const header = table.querySelectorAll('th')[column];
    
    // Toggle sort direction
    header.asc = !header.asc;
    const direction = header.asc ? 1 : -1;
    
    // Sort rows
    rows.sort((a, b) => {
        const cellA = a.querySelectorAll('td')[column].textContent.trim();
        const cellB = b.querySelectorAll('td')[column].textContent.trim();
        
        // Check if the content is a number
        if (!isNaN(cellA) && !isNaN(cellB)) {
            return direction * (parseFloat(cellA) - parseFloat(cellB));
        }
        
        // Otherwise sort as strings
        return direction * cellA.localeCompare(cellB);
    });
    
    // Remove existing rows
    rows.forEach(row => {
        tbody.removeChild(row);
    });
    
    // Add sorted rows
    rows.forEach(row => {
        tbody.appendChild(row);
    });
} 