import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('project-catalog-app');
    const projectId = el.dataset.projectId;
    const catalogItems = JSON.parse(el.dataset.catalogItems || '[]');
    const currentItems = JSON.parse(el.dataset.currentItems || '[]');

    createApp({
        data() {
            return {
                projectId,
                catalogItems,
                items: currentItems,
                newItem: { catalogItemId: '', itemName: '', quantity: 1, unitPrice: 0 }
            };
        },
        methods: {
            async addCatalogItem() {
                const form = new FormData();
                form.append('catalogItemId', this.newItem.catalogItemId);
                form.append('itemName', this.newItem.itemName);
                form.append('quantity', this.newItem.quantity);
                form.append('unitPrice', this.newItem.unitPrice);
                const res = await fetch(`/projects/${this.projectId}/catalog/save`, {
                    method: 'POST', body: form
                });
                if (res.ok) {
                    const dto = await res.json();
                    this.items.push(dto);
                    this.newItem = { catalogItemId: '', itemName: '', quantity: 1, unitPrice: 0 };
                } else {
                    alert('Fehler beim Hinzufügen des Artikels');
                }
            },
            async removeCatalogItem(item) {
                if (!confirm('Diesen Artikel löschen?')) return;
                const res = await fetch(`/projects/${this.projectId}/catalog/${item.id}/delete`, {
                    method: 'POST'
                });
                if (res.ok) {
                    this.items = this.items.filter(ci => ci.id !== item.id);
                } else {
                    alert('Fehler beim Löschen des Artikels');
                }
            },
            formatCurrency(v) {
                return v.toFixed(2) + ' CHF';
            }
        },
        template: `
      <div class="card mb-4">
        <div class="card-body">
          <h5>Katalogartikel</h5>
          <div class="row g-3 mb-4">
            <div class="col-md-4">
              <label class="form-label">Aus Katalog auswählen (optional)</label>
              <select v-model="newItem.catalogItemId" class="form-select">
                <option value="">-- Aus Katalog auswählen --</option>
                <option v-for="ci in catalogItems" :key="ci.id" :value="ci.id">
                  {{ ci.name }} ({{ formatCurrency(ci.unitPrice) }})
                </option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label">Oder eigenen Artikel</label>
              <input v-model="newItem.itemName" type="text" class="form-control" placeholder="Artikelname">
            </div>
            <div class="col-md-2">
              <label class="form-label">Menge</label>
              <input v-model.number="newItem.quantity" type="number" min="1" class="form-control">
            </div>
            <div class="col-md-2">
              <label class="form-label">Einzelpreis (CHF)</label>
              <input v-model.number="newItem.unitPrice" type="number" step="0.01" class="form-control">
            </div>
          </div>
          <button class="btn btn-success mb-4" @click="addCatalogItem">
            <i class="bi bi-plus-circle me-1"></i>Artikel hinzufügen
          </button>

          <p v-if="items.length === 0" class="text-muted">Noch keine Artikel hinzugefügt.</p>
          <table v-else class="table table-bordered align-middle">
            <thead>
              <tr>
                <th>Artikel</th><th>Menge</th><th>Einzelpreis</th><th>Gesamt</th><th>Aktionen</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in items" :key="item.id">
                <td>{{ item.itemName }}</td>
                <td>{{ item.quantity }}</td>
                <td>{{ formatCurrency(item.unitPrice) }}</td>
                <td>{{ formatCurrency(item.totalPrice) }}</td>
                <td>
                  <button class="btn btn-sm btn-outline-danger" @click="removeCatalogItem(item)">
                    <i class="bi bi-trash"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    `
    }).mount(el);
});
