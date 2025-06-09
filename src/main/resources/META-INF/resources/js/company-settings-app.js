import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

const el = document.getElementById('company-settings-app');
const initial = JSON.parse(el.dataset.company || '{}');

createApp({
    data() {
        return {
            company: { ...initial }
        }
    },
    methods: {
        async save() {
            try {
                const resp = await fetch('/api/company', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(this.company)
                });
                if (!resp.ok) throw new Error(`${resp.status} ${resp.statusText}`);
                const saved = await resp.json();
                this.company = saved;
                alert('Einstellungen gespeichert');
            } catch (err) {
                console.error(err);
                alert('Speichern fehlgeschlagen: ' + err.message);
            }
        }
    },
    template: `
    <form @submit.prevent="save">
      <div class="row">
        <div class="col-md-6">
          <h4>Grundinformationen</h4>
          <div class="mb-3">
            <label class="form-label">Firmenname</label>
            <input v-model="company.name" required class="form-control"/>
          </div>
          <div class="mb-3">
            <label class="form-label">Strasse</label>
            <input v-model="company.street" required class="form-control"/>
          </div>
          <div class="row">
            <div class="col-md-4 mb-3">
              <label class="form-label">PLZ</label>
              <input v-model="company.zipCode" required class="form-control"/>
            </div>
            <div class="col-md-8 mb-3">
              <label class="form-label">Ort</label>
              <input v-model="company.city" required class="form-control"/>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-label">Land</label>
            <input v-model="company.country" required class="form-control"/>
          </div>
        </div>
        <div class="col-md-6">
          <h4>Kontakt</h4>
          <div class="mb-3">
            <label class="form-label">Telefon</label>
            <input v-model="company.phone" class="form-control"/>
          </div>
          <div class="mb-3">
            <label class="form-label">E-Mail</label>
            <input v-model="company.email" type="email" class="form-control"/>
          </div>
          <div class="mb-3">
            <label class="form-label">Website</label>
            <input v-model="company.website" type="url" class="form-control"/>
          </div>
        </div>
      </div>

      <div class="row mt-4">
        <div class="col-md-6">
          <h4>Bankverbindung</h4>
          <div class="mb-3">
            <label class="form-label">IBAN</label>
            <input v-model="company.iban" class="form-control"/>
          </div>
          <div class="mb-3">
            <label class="form-label">BIC</label>
            <input v-model="company.bic" class="form-control"/>
          </div>
          <div class="mb-3">
            <label class="form-label">Bank</label>
            <input v-model="company.bankName" class="form-control"/>
          </div>
        </div>
        <div class="col-md-6">
          <h4>Steuerinfos</h4>
          <div class="mb-3">
            <label class="form-label">MWST-Nummer</label>
            <input v-model="company.vatNumber" class="form-control"/>
          </div>
          <div class="mb-3">
            <label class="form-label">Steuernummer</label>
            <input v-model="company.taxNumber" class="form-control"/>
          </div>
          <div class="mb-3">
            <label class="form-label">Standard-MWST-Satz (%)</label>
            <input v-model.number="company.defaultVatRate" type="number" step="0.1" class="form-control"/>
          </div>
        </div>
      </div>

      <div class="row mt-4">
        <div class="col-12">
          <h4>Rechnungseinstellungen</h4>
          <div class="mb-3">
            <label class="form-label">Logo (URL oder Base64)</label>
            <input v-model="company.logo" class="form-control"/>
          </div>
          <div class="mb-3">
            <label class="form-label">Standard-Fußzeile</label>
            <textarea v-model="company.defaultInvoiceFooter" rows="3" class="form-control"/>
          </div>
          <div class="mb-3">
            <label class="form-label">Standard-AGB</label>
            <textarea v-model="company.defaultInvoiceTerms" rows="3" class="form-control"/>
          </div>
        </div>
      </div>

      <div class="mt-4">
        <button type="submit" class="btn btn-primary">Speichern</button>
      </div>
    </form>
  `
}).mount('#company-settings-app');
