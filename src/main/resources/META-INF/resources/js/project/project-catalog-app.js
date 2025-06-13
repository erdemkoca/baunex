import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.prod.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('project-catalog-app');
    if (!el) {
        console.error('Element #project-catalog-app not found');
        return;
    }

    // Aus dem einen data-Attribut holen wir das gesamte Projekt-Objekt
    const project      = JSON.parse(el.dataset.project      || '{}');
    const catalogItems = JSON.parse(el.dataset.catalogItems || '[]');

    // Debug logging
    console.log('Project data:', project);
    console.log('Project catalog items:', project.catalogItems);
    console.log('Available catalog items:', catalogItems);

    createApp({
        data() {
            return {
                project,
                catalogItems,
                // Hier nutzen wir direkt project.catalogItems
                items: project.catalogItems || [],
                newItem: {
                    catalogItemId: '',
                    itemName: '',
                    quantity: 1,
                    unitPrice: 0
                }
            };
        },
        methods: {
            formatCurrency(value) {
                return new Intl.NumberFormat('de-CH', {
                    style: 'currency',
                    currency: 'CHF'
                }).format(value);
            },
            async saveItem() {
                try {
                    const resp = await fetch(
                        `/projects/${this.project.id}/catalog/save`,
                        {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                            body: new URLSearchParams({
                                itemName: this.newItem.itemName,
                                quantity: this.newItem.quantity,
                                unitPrice: this.newItem.unitPrice,
                                catalogItemId: this.newItem.catalogItemId || ''
                            })
                        }
                    );
                    if (!resp.ok) throw new Error('Fehler beim Speichern');
                    window.location.reload();
                } catch (e) {
                    console.error(e);
                    alert('Fehler beim Speichern des Artikels');
                }
            },
            async deleteItem(itemId) {
                if (!confirm('Möchten Sie diesen Artikel wirklich löschen?')) return;
                try {
                    const resp = await fetch(
                        `/projects/${this.project.id}/catalog/${itemId}/delete`
                    );
                    if (!resp.ok) throw new Error('Fehler beim Löschen');
                    window.location.reload();
                } catch (e) {
                    console.error(e);
                    alert('Fehler beim Löschen des Artikels');
                }
            },
            selectCatalogItem(item) {
                if (!item) return;
                this.newItem.catalogItemId = item.id;
                this.newItem.itemName      = item.name;
                this.newItem.unitPrice     = item.unitPrice;
            },
            resetForm() {
                this.newItem = {
                    catalogItemId: '',
                    itemName: '',
                    quantity: 1,
                    unitPrice: 0
                };
            }
        },
        template: `
      <div class="card">
        <div class="card-body">
          <h5 class="mb-4">Katalogartikel</h5>

          <!-- Bestehende Einträge -->
          <div class="table-responsive mb-4">
            <table class="table">
              <thead>
                <tr>
                  <th>Bezeichnung</th>
                  <th>Menge</th>
                  <th>Einzelpreis</th>
                  <th>Gesamtpreis</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in items" :key="item.id">
                  <td>{{ item.itemName }}</td>
                  <td>{{ item.quantity }}</td>
                  <td>{{ formatCurrency(item.unitPrice) }}</td>
                  <td>{{ formatCurrency(item.totalPrice) }}</td>
                  <td>
                    <button class="btn btn-sm btn-danger"
                            @click="deleteItem(item.id)">
                      <i class="bi bi-trash"></i>
                    </button>
                  </td>
                </tr>
                <tr v-if="!items.length">
                  <td colspan="5" class="text-center text-muted">
                    Noch keine Katalogartikel vorhanden.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Formular zum Hinzufügen -->
          <div class="card">
            <div class="card-header">
              <h5 class="mb-0">Neuer Artikel</h5>
            </div>
            <div class="card-body">
              <form @submit.prevent="saveItem">
                <div class="row mb-3">
                  <div class="col-md-6">
                    <label class="form-label">Artikel aus Katalog</label>
                    <select class="form-select"
                            v-model="newItem.catalogItemId"
                            @change="selectCatalogItem(
                              catalogItems.find(i => i.id === newItem.catalogItemId)
                            )">
                      <option value="">-- Neuer Artikel--</option>
                      <option v-for="i in catalogItems" :key="i.id" :value="i.id">
                        {{ i.name }} ({{ formatCurrency(i.unitPrice) }})
                      </option>
                    </select>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Bezeichnung</label>
                    <input type="text" class="form-control"
                           v-model="newItem.itemName" required>
                  </div>
                </div>
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Menge</label>
                    <input type="number" class="form-control"
                           v-model="newItem.quantity" min="1" required>
                  </div>
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Einzelpreis (CHF)</label>
                    <input type="number" class="form-control"
                           v-model="newItem.unitPrice" step="0.01" required>
                  </div>
                </div>
                <button type="submit" class="btn btn-primary">
                  Artikel speichern
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    `
    }).mount('#project-catalog-app');
});
