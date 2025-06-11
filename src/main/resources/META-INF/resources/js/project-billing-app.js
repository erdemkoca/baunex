import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('billing-app');
    const billing = JSON.parse(el.dataset.billing || '{}');

    createApp({
        data() {
            return { billing };
        },
        computed: {
            hasEntries() {
                return billing.timeEntries && billing.timeEntries.length > 0;
            },
            hasMaterials() {
                const m = billing.materials || [];
                const t = billing.timeEntries?.flatMap(e => e.catalogItems) || [];
                return m.length + t.length > 0;
            }
        },
        methods: {
            formatDate(d) {
                return d ? new Date(d).toLocaleDateString('de-CH') : '—';
            },
            formatCurrency(v) {
                return v?.toFixed(2) + ' CHF';
            }
        },
        template: `
    <div class="card">
      <div class="card-body">
        <h5 class="mb-4">Zeiterfassungen</h5>
        <p v-if="!hasEntries" class="text-muted">Keine Zeiterfassungen vorhanden.</p>
        <div v-else class="accordion mb-3" id="billingTimeEntriesAccordion">
          <div v-for="e in billing.timeEntries" :key="e.id" class="accordion-item">
            <h2 class="accordion-header">
              <button class="accordion-button collapsed" type="button"
                      data-bs-toggle="collapse"
                      :data-bs-target="'#billingEntry' + e.id"
                      aria-expanded="false">
                <div class="d-flex justify-content-between w-100 me-3">
                  <div>
                    <strong>{{ e.employeeEmail }}</strong> – {{ formatDate(e.date) }}
                  </div>
                  <div>
                    <span class="badge bg-primary me-2">{{ e.hoursWorked }} Stunden</span>
                    <span v-if="!e.billable" class="badge bg-secondary">Nicht verrechenbar</span>
                    <span class="badge bg-success ms-2">
                      {{ e.costBreakdown 
                          ? formatCurrency(e.costBreakdown.totalServiceCost)
                          : formatCurrency(e.cost) }}
                    </span>
                  </div>
                </div>
              </button>
            </h2>
            <div :id="'billingEntry' + e.id"
                 class="accordion-collapse collapse"
                 data-bs-parent="#billingTimeEntriesAccordion">
              <div class="accordion-body">
                <div class="row mb-3">
                  <div class="col-md-6">
                    <h6>Grundinformationen</h6>
                    <p><strong>Notiz:</strong> {{ e.notes || '—' }}</p>
                    <p><strong>Stundensatz:</strong> {{ e.hourlyRate != null ? formatCurrency(e.hourlyRate) : '—' }}</p>
                  </div>
                  <div class="col-md-6">
                    <h6>Status</h6>
                    <span :class="['badge', e.invoiced ? 'bg-success' : 'bg-warning']">
                      {{ e.invoiced ? 'Fakturiert' : 'Nicht fakturiert' }}
                    </span>
                  </div>
                </div>
                <div class="row mb-3">
                  <div class="col-12">
                    <h6>Kostenaufschlüsselung</h6>
                    <table class="table table-sm">
                      <tbody>
                        <tr>
                          <td>Zeitkosten</td>
                          <td class="text-end">{{ formatCurrency(e.costBreakdown.timeCost) }}</td>
                        </tr>
                        <tr v-if="e.costBreakdown.nightSurcharge != null">
                          <td>Nachtzuschlag</td>
                          <td class="text-end">{{ formatCurrency(e.costBreakdown.nightSurcharge) }}</td>
                        </tr>
                        <tr v-if="e.costBreakdown.weekendSurcharge != null">
                          <td>Wochenendzuschlag</td>
                          <td class="text-end">{{ formatCurrency(e.costBreakdown.weekendSurcharge) }}</td>
                        </tr>
                        <tr v-if="e.costBreakdown.holidaySurcharge != null">
                          <td>Feiertagszuschlag</td>
                          <td class="text-end">{{ formatCurrency(e.costBreakdown.holidaySurcharge) }}</td>
                        </tr>
                        <tr v-if="e.costBreakdown.travelTimeCost != null">
                          <td>Reisezeit ({{ e.travelTimeMinutes }} min)</td>
                          <td class="text-end">{{ formatCurrency(e.costBreakdown.travelTimeCost) }}</td>
                        </tr>
                        <tr v-if="e.costBreakdown.waitingTimeCost != null">
                          <td>Wartezeit ({{ e.waitingTimeMinutes }} min)</td>
                          <td class="text-end">{{ formatCurrency(e.costBreakdown.waitingTimeCost) }}</td>
                        </tr>
                        <tr v-if="e.costBreakdown.disposalCost != null">
                          <td>Entsorgungskosten</td>
                          <td class="text-end">{{ formatCurrency(e.costBreakdown.disposalCost) }}</td>
                        </tr>
                        <tr class="table-primary">
                          <td><strong>Gesamtkosten Dienstleistung</strong></td>
                          <td class="text-end">
                            <strong>{{ formatCurrency(e.costBreakdown.totalServiceCost) }}</strong>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <h5 class="mt-5 mb-4">Alle Katalogartikel</h5>
        <p v-if="!hasMaterials" class="text-muted">Keine Katalogartikel vorhanden.</p>
        <div v-else class="table-responsive">
          <table class="table table-striped">
            <thead>
              <tr>
                <th>Quelle</th><th>Artikel</th><th>Menge</th><th>Einzelpreis</th><th>Gesamt</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="m in billing.materials" :key="'mat-'+m.id">
                <td>Projekt</td>
                <td>{{ m.itemName }}</td>
                <td>{{ m.quantity }}</td>
                <td>{{ formatCurrency(m.unitPrice) }}</td>
                <td>{{ formatCurrency(m.totalPrice) }}</td>
              </tr>
              <tr v-for="e in billing.timeEntries" :key="'te-'+e.id" v-for="item in e.catalogItems">
                <td>Zeiterfassung ({{ formatDate(e.date) }})</td>
                <td>{{ item.itemName }}</td>
                <td>{{ item.quantity }}</td>
                <td>{{ formatCurrency(item.unitPrice) }}</td>
                <td>{{ formatCurrency(item.totalPrice) }}</td>
              </tr>
            </tbody>
            <tfoot>
              <tr class="table-primary">
                <td colspan="4"><strong>Gesamt Katalogartikel</strong></td>
                <td class="text-end">
                  <strong>{{ formatCurrency(billing.costBreakdown.totalCatalogItemsAndMaterials) }}</strong>
                </td>
              </tr>
            </tfoot>
          </table>
        </div>

        <div class="mt-5">
          <div class="alert alert-success">
            <div class="row">
              <div class="col-md-6">
                <strong>Gesamtkosten Dienstleistung:</strong> {{ formatCurrency(billing.timeTotal) }}
              </div>
              <div class="col-md-6">
                <strong>Gesamtkosten Katalogartikel:</strong> {{ formatCurrency(billing.costBreakdown.totalCatalogItemsAndMaterials) }}
              </div>
            </div>
            <hr />
            <div class="row">
              <div class="col-12">
                <h4 class="mb-0"><strong>Gesamtsumme:</strong> {{ formatCurrency(billing.total) }}</h4>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    `
    }).mount(el);
});
