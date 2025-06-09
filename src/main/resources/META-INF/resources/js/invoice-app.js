import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';
import * as Sortable from 'https://cdn.jsdelivr.net/npm/sortablejs@1.15.0/Sortable.js';

const app = document.getElementById('invoice-app');
console.log('App element:', app);
console.log('Dataset:', app.dataset);

const invoice = JSON.parse(app.dataset.invoice || '{}');
const company = JSON.parse(app.dataset.company || '{}');
const billing = JSON.parse(app.dataset.billing || '{}');
const currentDate = app.dataset.currentDate;

console.log('Parsed data:', { invoice, company, billing, currentDate });

createApp({
    data() {
        return {
            invoice,
            company,
            billing,
            invoiceNumber: invoice.invoiceNumber || '',
            invoiceDate: invoice.invoiceDate || currentDate,
            dueDate: invoice.dueDate || currentDate,
            notes: invoice.notes || '',
            vatRate: invoice.vatRate || 8.1,
            serviceItems: invoice.items?.filter(i => i.type === 'VA') || [],
            materialItems: invoice.items?.filter(i => i.type === 'IC') || [],
        };
    },
    created() {
        console.log('Component created with data:', this.$data);
        // Initialize service items from billing time entries
        if (this.billing?.timeEntries) {
            this.serviceItems = this.billing.timeEntries.map(entry => ({
                description: entry.title || '',
                quantity: entry.hoursWorked || 0,
                price: entry.hourlyRate || 0
            }));
        }

        // Initialize material items from billing materials
        if (this.billing?.materials) {
            this.materialItems = this.billing.materials.map(item => ({
                description: item.itemName,
                quantity: item.quantity,
                price: item.unitPrice
            }));
        }

        // Initialize markdown editors
        this.$nextTick(() => {
            const termsEditor = new EasyMDE({
                element: document.getElementById('terms-editor'),
                spellChecker: false,
                status: false,
                toolbar: ['bold', 'italic', 'heading', '|', 'quote', 'unordered-list', 'ordered-list', '|', 'preview'],
                initialValue: this.company.defaultInvoiceTerms || ''
            });

            const footerEditor = new EasyMDE({
                element: document.getElementById('footer-editor'),
                spellChecker: false,
                status: false,
                toolbar: ['bold', 'italic', 'heading', '|', 'quote', 'unordered-list', 'ordered-list', '|', 'preview'],
                initialValue: this.company.defaultInvoiceFooter || ''
            });

            // Update company data when editors change
            termsEditor.codemirror.on('change', () => {
                this.company.defaultInvoiceTerms = termsEditor.value();
            });

            footerEditor.codemirror.on('change', () => {
                this.company.defaultInvoiceFooter = footerEditor.value();
            });
        });

        console.log('After initialization:', { serviceItems: this.serviceItems, materialItems: this.materialItems });
    },
    computed: {
        serviceTotal() {
            return this.serviceItems.reduce((sum, i) => sum + (i.quantity || 0) * (i.price || 0), 0);
        },
        materialTotal() {
            return this.materialItems.reduce((sum, i) => sum + (i.quantity || 0) * (i.price || 0), 0);
        },
        grossTotal() {
            return this.serviceTotal + this.materialTotal;
        },
        vatAmount() {
            return this.grossTotal * (this.vatRate / 100);
        },
        netTotal() {
            return this.grossTotal + this.vatAmount;
        }
    },
    methods: {
        addService() {
            this.serviceItems.push({ description: '', quantity: 1, price: 0 });
        },
        removeService(index) {
            this.serviceItems.splice(index, 1);
        },
        addMaterial() {
            this.materialItems.push({ description: '', quantity: 1, price: 0 });
        },
        removeMaterial(index) {
            this.materialItems.splice(index, 1);
        },
        submitInvoice(asDraft = true) {
            const payload = {
                invoiceNumber: this.invoiceNumber,
                invoiceDate: this.invoiceDate,
                dueDate: this.dueDate,
                notes: this.notes,
                vatRate: this.vatRate,
                customerId: this.invoice.customerId,
                projectId: this.invoice.projectId,
                items: [
                    ...this.serviceItems.map(i => ({ ...i, type: 'VA' })),
                    ...this.materialItems.map(i => ({ ...i, type: 'IC' }))
                ]
            };

            console.log('Submitting invoice with payload:', payload);

            // Wenn es eine neue Rechnung ist (keine ID vorhanden)
            if (!this.invoice.id) {
                fetch('/api/invoice/new', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                })
                .then(async response => {
                    if (!response.ok) {
                        const errorText = await response.text();
                        console.error('Server response:', errorText);
                        throw new Error(`Server error: ${response.status} ${response.statusText}\n${errorText}`);
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('Success:', data);
                    window.location.href = '/invoice';
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Fehler beim Speichern der Rechnung: ' + error.message);
                });
            } else {
                // Wenn es eine existierende Rechnung ist
                payload.id = this.invoice.id;
                payload.invoiceStatus = asDraft ? 'DRAFT' : 'ISSUED';

                fetch('/api/invoice/create', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                })
                .then(async response => {
                    if (!response.ok) {
                        const errorText = await response.text();
                        console.error('Server response:', errorText);
                        throw new Error(`Server error: ${response.status} ${response.statusText}\n${errorText}`);
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('Success:', data);
                    window.location.href = '/invoice';
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Fehler beim Speichern der Rechnung: ' + error.message);
                });
            }
        }
    },
    mounted() {
        const serviceTbody = this.$el.querySelector('table:nth-of-type(1) tbody');
        new Sortable(serviceTbody, {
            handle: '.handle',
            animation: 150,
            ghostClass: 'sortable-ghost',
            onEnd: evt => {
                const item = this.serviceItems.splice(evt.oldIndex, 1)[0];
                this.serviceItems.splice(evt.newIndex, 0, item);
            }
        });

        const materialTbody = this.$el.querySelector('table:nth-of-type(2) tbody');
        new Sortable(materialTbody, {
            handle: '.handle',
            animation: 150,
            ghostClass: 'sortable-ghost',
            onEnd: evt => {
                const item = this.materialItems.splice(evt.oldIndex, 1)[0];
                this.materialItems.splice(evt.newIndex, 0, item);
            }
        });
    },
    template: `
        <div class="container-fluid">
            <div class="d-flex justify-content-between mb-4">
                <div>
                    <h2>RECHNUNG</h2>
                    <div class="mt-3">
                        <strong>Rechnung an:</strong><br>
                        {{ invoice.customerName }}<br>
                        {{ invoice.customerAddress }}
                    </div>
                </div>
                <div class="text-end">
                    <div style="height: 80px; border: 1px dashed #ccc; display: flex; align-items: center; justify-content: center;">
                        Firmenlogo
                    </div>
                    <div class="mt-3">
                        <strong>Datum:</strong>
                        <input v-model="invoiceDate" type="date" class="form-control form-control-sm">
                        <div><strong>Projekt:</strong><br>{{ invoice.projectName }}</div>
                    </div>
                </div>
            </div>

            <div class="mb-4">
                <label class="form-label">Rechnungsnummer</label>
                <input v-model="invoiceNumber" type="text" class="form-control">
            </div>

            <!-- Service Tabelle -->
            <h5>Leistungen</h5>
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <th style="width: 40px"></th>
                        <th>Bezeichnung</th>
                        <th>Menge</th>
                        <th>Preis</th>
                        <th>Betrag</th>
                        <th style="width: 40px"></th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(item, index) in serviceItems" :key="'s' + index" class="draggable">
                        <td class="text-center"><i class="bi bi-grip-vertical handle"></i></td>
                        <td><input v-model="item.description" class="form-control"></td>
                        <td><input v-model.number="item.quantity" type="number" class="form-control"></td>
                        <td><input v-model.number="item.price" type="number" class="form-control"></td>
                        <td>{{ (item.quantity * item.price).toFixed(2) }}</td>
                        <td><button @click.prevent="removeService(index)" class="btn btn-sm btn-outline-danger">x</button></td>
                    </tr>
                </tbody>
                <tfoot>
                    <tr>
                        <td colspan="4" class="text-end"><strong>Total Leistungen:</strong></td>
                        <td><strong>{{ serviceTotal.toFixed(2) }} CHF</strong></td>
                        <td></td>
                    </tr>
                </tfoot>
            </table>
            <button @click.prevent="addService" class="btn btn-outline-primary mb-3">+ Leistung hinzufügen</button>

            <!-- Material Tabelle -->
            <h5>Material</h5>
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <th style="width: 40px"></th>
                        <th>Bezeichnung</th>
                        <th>Menge</th>
                        <th>Preis</th>
                        <th>Betrag</th>
                        <th style="width: 40px"></th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(item, index) in materialItems" :key="'m' + index" class="draggable">
                        <td class="text-center"><i class="bi bi-grip-vertical handle"></i></td>
                        <td><input v-model="item.description" class="form-control"></td>
                        <td><input v-model.number="item.quantity" type="number" class="form-control"></td>
                        <td><input v-model.number="item.price" type="number" class="form-control"></td>
                        <td>{{ (item.quantity * item.price).toFixed(2) }}</td>
                        <td><button @click.prevent="removeMaterial(index)" class="btn btn-sm btn-outline-danger">x</button></td>
                    </tr>
                </tbody>
                <tfoot>
                    <tr>
                        <td colspan="4" class="text-end"><strong>Total Material:</strong></td>
                        <td><strong>{{ materialTotal.toFixed(2) }} CHF</strong></td>
                        <td></td>
                    </tr>
                </tfoot>
            </table>
            <button @click.prevent="addMaterial" class="btn btn-outline-primary mb-3">+ Material hinzufügen</button>

            <!-- Zusammenfassung -->
            <div class="card mb-4">
                <div class="card-body">
                    <h5>Zusammenfassung</h5>
                    <table class="table table-sm">
                        <tr><td>Service Total:</td><td class="text-end">{{ serviceTotal.toFixed(2) }} CHF</td></tr>
                        <tr><td>Material Total:</td><td class="text-end">{{ materialTotal.toFixed(2) }} CHF</td></tr>
                        <tr><td>Brutto:</td><td class="text-end">{{ grossTotal.toFixed(2) }} CHF</td></tr>
                        <tr><td>MWST ({{ vatRate }}%):</td><td class="text-end">{{ vatAmount.toFixed(2) }} CHF</td></tr>
                        <tr class="table-primary"><td><strong>Nettobetrag:</strong></td><td class="text-end"><strong>{{ netTotal.toFixed(2) }} CHF</strong></td></tr>
                    </table>
                </div>
            </div>

            <!-- Notizen & AGB -->
            <div class="mb-3">
                <label class="form-label">Notizen</label>
                <textarea v-model="notes" class="form-control"></textarea>
            </div>
            <div class="row">
                <div class="col-md-6">
                    <h6>AGB</h6>
                    <textarea id="terms-editor" class="form-control"></textarea>
                </div>
                <div class="col-md-6">
                    <h6>Zahlungsinformationen</h6>
                    <textarea id="footer-editor" class="form-control"></textarea>
                </div>
            </div>

            <!-- Aktionen -->
            <div class="mt-4 d-flex gap-2">
                <button @click="submitInvoice(true)" class="btn btn-outline-primary">
                    <i class="bi bi-save"></i> Als Entwurf speichern
                </button>
                <button @click="submitInvoice(false)" class="btn btn-primary">
                    <i class="bi bi-send"></i> Als Rechnung speichern
                </button>
                <a href="/invoice" class="btn btn-outline-secondary">Abbrechen</a>
            </div>
        </div>
    `
}).mount('#invoice-app'); 