import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

const mount = document.getElementById('company-settings-app');
const company = JSON.parse(mount.dataset.company || '{}');
const today   = mount.dataset.currentDate;

createApp({
    data() {
        return {
            company,
            tabs: [
                { key: 'general', label: 'Grundinformationen'  },
                { key: 'contact', label: 'Kontakt'             },
                { key: 'bank',    label: 'Bankverbindung'      },
                { key: 'tax',     label: 'Steuerinformationen' },
                { key: 'invoice', label: 'Rechnungseinstellungen' }
            ],
            activeTab: 'general',
            saving: false
        }
    },
    methods: {
        switchTab(key) {
            this.activeTab = key;
        },
        async save() {
            this.saving = true;
            try {
                const res = await fetch('/api/company', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(this.company)
                });
                if (!res.ok) throw new Error(await res.text());
                alert('Gespeichert!');
            } catch (e) {
                console.error(e);
                alert('Fehler: ' + e.message);
            } finally {
                this.saving = false;
            }
        }
    },
    template: `
    <div class="container-fluid">
      <ul class="nav nav-tabs mb-3">
        <li class="nav-item" v-for="t in tabs" :key="t.key">
          <a href="#" class="nav-link"
             :class="{ active: activeTab===t.key }"
             @click.prevent="switchTab(t.key)">
            {{ t.label }}
          </a>
        </li>
      </ul>

      <div class="tab-content">
        <!-- General -->
        <div v-if="activeTab==='general'">
          <div class="mb-3">
            <label class="form-label">Firmenname</label>
            <input v-model="company.name" class="form-control" required>
          </div>
          <div class="mb-3">
            <label class="form-label">Strasse</label>
            <input v-model="company.street" class="form-control" required>
          </div>
          <div class="row">
            <div class="col-md-4 mb-3">
              <label class="form-label">PLZ</label>
              <input v-model="company.zipCode" class="form-control" required>
            </div>
            <div class="col-md-8 mb-3">
              <label class="form-label">Ort</label>
              <input v-model="company.city" class="form-control" required>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-label">Land</label>
            <input v-model="company.country" class="form-control" required>
          </div>
        </div>

        <!-- Contact -->
        <div v-if="activeTab==='contact'">
          <div class="mb-3">
            <label class="form-label">Telefon</label>
            <input v-model="company.phone" class="form-control">
          </div>
          <div class="mb-3">
            <label class="form-label">E-Mail</label>
            <input v-model="company.email" type="email" class="form-control">
          </div>
          <div class="mb-3">
            <label class="form-label">Website</label>
            <input v-model="company.website" type="url" class="form-control">
          </div>
        </div>

        <!-- Bank -->
        <div v-if="activeTab==='bank'">
          <div class="mb-3">
            <label class="form-label">IBAN</label>
            <input v-model="company.iban" class="form-control">
          </div>
          <div class="mb-3">
            <label class="form-label">BIC</label>
            <input v-model="company.bic" class="form-control">
          </div>
          <div class="mb-3">
            <label class="form-label">Bank</label>
            <input v-model="company.bankName" class="form-control">
          </div>
        </div>

        <!-- Tax -->
        <div v-if="activeTab==='tax'">
          <div class="mb-3">
            <label class="form-label">MWST-Nummer</label>
            <input v-model="company.vatNumber" class="form-control">
          </div>
          <div class="mb-3">
            <label class="form-label">Steuernummer</label>
            <input v-model="company.taxNumber" class="form-control">
          </div>
          <div class="mb-3">
            <label class="form-label">Standard-MWST-Satz (%)</label>
            <input v-model.number="company.defaultVatRate" type="number" step="0.1" class="form-control">
          </div>
        </div>

        <!-- Invoice -->
        <div v-if="activeTab==='invoice'">
          <div class="mb-3">
            <label class="form-label">Logo (URL oder Base64)</label>
            <input v-model="company.logo" class="form-control">
          </div>
          <div class="mb-3">
            <label class="form-label">Standard-Fußzeile</label>
            <textarea v-model="company.defaultInvoiceFooter" class="form-control" rows="3"></textarea>
          </div>
          <div class="mb-3">
            <label class="form-label">Standard-AGB</label>
            <textarea v-model="company.defaultInvoiceTerms" class="form-control" rows="3"></textarea>
          </div>
        </div>
      </div>

      <div class="mt-4">
        <button @click="save" :disabled="saving" class="btn btn-primary">
          {{ saving ? 'Speichern…' : 'Speichern' }}
        </button>
      </div>
    </div>
  `
}).mount('#company-settings-app');
