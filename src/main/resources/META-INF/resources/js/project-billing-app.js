import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.prod.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('billing-app');
    if (!el) {
        console.error('Element #billing-app not found');
        return;
    }
    const billing = JSON.parse(el.dataset.billing || '{}');

    createApp({
        data() {
            return {
                billing
            };
        },
        methods: {
            formatCurrency(value) {
                return new Intl.NumberFormat('de-CH', {
                    style: 'currency',
                    currency: 'CHF'
                }).format(value);
            },
            formatDate(date) {
                return new Date(date).toLocaleDateString('de-CH');
            }
        },
        template: `
            <div class="card">
              <div class="card-body">
                <!-- Materialien -->
                <h5 class="mb-4">Materialien</h5>
                <table class="table">
                  <thead>
                    <tr>
                      <th>Bezeichnung</th>
                      <th>Menge</th>
                      <th>Einzelpreis</th>
                      <th>Gesamtpreis</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="m in billing.materials" :key="'m-'+m.id">
                      <td>{{ m.itemName }}</td>
                      <td>{{ m.quantity }}</td>
                      <td>{{ formatCurrency(m.unitPrice) }}</td>
                      <td>{{ formatCurrency(m.totalPrice) }}</td>
                    </tr>
                    <tr v-if="!billing.materials.length">
                      <td colspan="4" class="text-center text-muted">Keine Materialien vorhanden.</td>
                    </tr>
                  </tbody>
                </table>
            
                <!-- Zeiterfassungen -->
                <h5 class="mb-4 mt-5">Zeiterfassungen</h5>
                <table class="table">
                  <thead>
                    <tr>
                      <th>Datum</th>
                      <th>Bezeichnung</th>
                      <th>Stunden</th>
                      <th>Stundensatz</th>
                      <th>Gesamtpreis</th>
                    </tr>
                  </thead>
                  <tbody>
                  <!-- Projekt-Materialien -->
                  <template v-for="m in billing.materials" :key="'m-'+m.id">
                    <tr>
                      <td>Projekt</td>
                      <td>{{ m.itemName }}</td>
                      <td>{{ m.quantity }}</td>
                      <td>{{ formatCurrency(m.unitPrice) }}</td>
                      <td>{{ formatCurrency(m.totalPrice) }}</td>
                    </tr>
                  </template>
                
                  <!-- Katalogartikel aus Zeiterfassungen -->
                  <template v-for="entry in billing.timeEntries" :key="'e-'+entry.id">
                    <tr v-for="item in entry.catalogItems || []" :key="'ei-'+item.id">
                      <td>Zeiterfassung ({{ formatDate(entry.date) }})</td>
                      <td>{{ item.itemName }}</td>
                      <td>{{ item.quantity }}</td>
                      <td>{{ formatCurrency(item.unitPrice) }}</td>
                      <td>{{ formatCurrency(item.totalPrice) }}</td>
                    </tr>
                  </template>
                </tbody>

                </table>
            
                <!-- Totals -->
                <div class="alert alert-success mt-4">
                  <div class="row">
                    <div class="col-md-6">
                      <strong>Gesamtkosten Dienstleistung:</strong> {{ formatCurrency(billing.timeTotal) }}
                    </div>
                    <div class="col-md-6">
                      <strong>Gesamtkosten Material:</strong> {{ formatCurrency(billing.materialTotal) }}
                    </div>
                  </div>
                  <hr>
                  <div class="row">
                    <div class="col-12">
                      <h4 class="mb-0"><strong>Gesamtsumme:</strong> {{ formatCurrency(billing.total) }}</h4>
                    </div>
                  </div>
                </div>
              </div>
            </div>

        `
    }).mount('#billing-app');
});
