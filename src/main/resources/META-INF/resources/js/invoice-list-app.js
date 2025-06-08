import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

const app = document.getElementById('invoice-list-app');
const invoices = JSON.parse(app.dataset.invoices || '[]');
const projects = JSON.parse(app.dataset.projects || '[]');

createApp({
    data() {
        return {
            invoices,
            projects,
            selectedStatus: 'ALL',
            statuses: {
                ALL: { label: 'Alle', color: 'secondary', icon: 'bi-list-ul' },
                DRAFT: { label: 'Entwurf', color: 'primary', icon: 'bi-pencil-square' },
                ISSUED: { label: 'Offen', color: 'warning', icon: 'bi-hourglass-split' },
                PAID: { label: 'Bezahlt', color: 'success', icon: 'bi-check-circle' },
                CANCELLED: { label: 'Storniert', color: 'danger', icon: 'bi-x-circle' }
            }
        };
    },
    computed: {
        filteredInvoices() {
            if (this.selectedStatus === 'ALL') {
                return this.invoices;
            }
            return this.invoices.filter(inv => inv.invoiceStatus === this.selectedStatus);
        },
        statusCounts() {
            const counts = {
                DRAFT: 0,
                ISSUED: 0,
                PAID: 0,
                CANCELLED: 0
            };
            
            this.invoices.forEach(invoice => {
                if (invoice.invoiceStatus && counts.hasOwnProperty(invoice.invoiceStatus)) {
                    counts[invoice.invoiceStatus]++;
                }
            });
            
            return counts;
        }
    },
    methods: {
        navigateToInvoice(id) {
            window.location.href = `/invoice/${id}`;
        },
        getStatusInfo(status) {
            return this.statuses[status] || this.statuses.DRAFT; // Default to DRAFT if status is invalid
        },
        setStatus(status) {
            this.selectedStatus = status;
        },
        async publishInvoice(id) {
            if (confirm('Möchten Sie diese Rechnung wirklich veröffentlichen?')) {
                try {
                    const response = await fetch(`/invoice/${id}/publish`, {
                        method: 'POST'
                    });
                    if (response.ok) {
                        window.location.reload();
                    } else {
                        alert('Fehler beim Veröffentlichen der Rechnung');
                    }
                } catch (error) {
                    console.error('Error:', error);
                    alert('Fehler beim Veröffentlichen der Rechnung');
                }
            }
        },
        async markAsPaid(id) {
            if (confirm('Möchten Sie diese Rechnung wirklich als bezahlt markieren?')) {
                try {
                    const response = await fetch(`/invoice/${id}/mark-as-paid`, {
                        method: 'POST'
                    });
                    if (response.ok) {
                        window.location.reload();
                    } else {
                        alert('Fehler beim Markieren der Rechnung als bezahlt');
                    }
                } catch (error) {
                    console.error('Error:', error);
                    alert('Fehler beim Markieren der Rechnung als bezahlt');
                }
            }
        },
        async cancelInvoice(id) {
            if (confirm('Möchten Sie diese Rechnung wirklich stornieren?')) {
                try {
                    const response = await fetch(`/invoice/${id}/cancel`, {
                        method: 'POST'
                    });
                    if (response.ok) {
                        window.location.reload();
                    } else {
                        alert('Fehler beim Stornieren der Rechnung');
                    }
                } catch (error) {
                    console.error('Error:', error);
                    alert('Fehler beim Stornieren der Rechnung');
                }
            }
        }
    },
    template: `
        <div class="container-fluid">
            <!-- New Invoice Form -->
            <div class="row mb-3">
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <h5 class="card-title">Neue Rechnung erstellen</h5>
                            <form action="/invoice/new" method="get">
                                <div class="mb-3">
                                    <label for="projectId" class="form-label">Projekt auswählen</label>
                                    <select class="form-select" id="projectId" name="projectId" required>
                                        <option value="">-- Bitte wählen --</option>
                                        <option v-for="proj in projects" :key="proj.id" :value="proj.id">
                                            {{ proj.name }} – {{ proj.customerName }}
                                        </option>
                                    </select>
                                </div>
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-plus"></i> Neue Rechnung
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Status Overview Cards -->
            <div class="row mb-4">
                <div v-for="(status, key) in statuses" :key="key" class="col-md-2">
                    <div :class="['card', 'border-' + status.color, 'h-100']">
                        <div class="card-body text-center">
                            <i :class="['bi', status.icon, 'fs-1', 'text-' + status.color]"></i>
                            <h5 class="card-title mt-2">{{ status.label }}</h5>
                            <p class="card-text display-6">
                                {{ key === 'ALL' ? invoices.length : statusCounts[key] }}
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Status Filter -->
            <div class="mb-3">
                <div class="btn-group" role="group">
                    <button v-for="(status, key) in statuses" 
                            :key="key"
                            type="button" 
                            :class="['btn', 'btn-outline-' + status.color, { active: selectedStatus === key }]"
                            @click="setStatus(key)">
                        <i :class="['bi', status.icon, 'me-1']"></i>
                        {{ status.label }}
                    </button>
                </div>
            </div>

            <!-- Invoice Table -->
            <div class="card">
                <div class="card-body">
                    <table class="table table-hover">
                        <thead>
                            <tr>
                                <th>Rechnungsnummer</th>
                                <th>Rechnungsdatum</th>
                                <th>Fälligkeitsdatum</th>
                                <th>Kunde</th>
                                <th>Projekt</th>
                                <th>Status</th>
                                <th>Gesamtbetrag</th>
                                <th>Aktionen</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="inv in filteredInvoices" 
                                :key="inv.id" 
                                class="invoice-row"
                                style="cursor: pointer;"
                                @click="navigateToInvoice(inv.id)">
                                <td>{{ inv.invoiceNumber }}</td>
                                <td>{{ new Date(inv.invoiceDate).toLocaleDateString('de-CH') }}</td>
                                <td>{{ new Date(inv.dueDate).toLocaleDateString('de-CH') }}</td>
                                <td>{{ inv.customerName }}</td>
                                <td>{{ inv.projectName }}</td>
                                <td>
                                    <span :class="['badge', 'bg-' + getStatusInfo(inv.invoiceStatus).color]">
                                        <i :class="['bi', getStatusInfo(inv.invoiceStatus).icon, 'me-1']"></i>
                                        {{ getStatusInfo(inv.invoiceStatus).label }}
                                    </span>
                                </td>
                                <td>{{ inv.formattedGrandTotal }}</td>
                                <td>
                                    <div class="btn-group" @click.stop>
                                        <template v-if="inv.invoiceStatus === 'DRAFT'">
                                            <button type="button"
                                                    class="btn btn-sm btn-primary"
                                                    @click="publishInvoice(inv.id)"
                                                    title="Veröffentlichen">
                                                <i class="bi bi-send"></i> Veröffentlichen
                                            </button>
                                        </template>
                                        <template v-else-if="inv.invoiceStatus === 'ISSUED'">
                                            <button type="button"
                                                    class="btn btn-sm btn-success"
                                                    @click="markAsPaid(inv.id)"
                                                    title="Als bezahlt markieren">
                                                <i class="bi bi-check-circle"></i> Bezahlt
                                            </button>
                                            <button type="button"
                                                    class="btn btn-sm btn-danger"
                                                    @click="cancelInvoice(inv.id)"
                                                    title="Stornieren">
                                                <i class="bi bi-x-circle"></i> Stornieren
                                            </button>
                                        </template>
                                        <template v-else>
                                            <a :href="'/invoice/' + inv.id + '/pdf'"
                                               class="btn btn-sm btn-outline-secondary"
                                               title="PDF herunterladen">
                                                <i class="bi bi-file-pdf"></i> PDF
                                            </a>
                                        </template>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    `
}).mount('#invoice-list-app'); 