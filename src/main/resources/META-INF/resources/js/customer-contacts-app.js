import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('customer-contacts-app');
    const contacts = JSON.parse(el.dataset.contacts || '[]');

    createApp({
        data() {
            return { contacts };
        },
        template: `
      <div class="card mb-4">
        <div class="card-body">
          <h5>Kundenkontakte</h5>
          <p v-if="contacts.length === 0" class="text-muted">Keine Kontakte für diesen Kunden.</p>
          <ul v-else class="list-group">
            <li v-for="c in contacts" :key="c.id"
                class="list-group-item d-flex justify-content-between align-items-center">
              <div>
                <strong>{{ c.role || 'Kontakt' }}</strong> – {{ c.firstName }} {{ c.lastName }}
              </div>
              <span v-if="c.isPrimary" class="badge bg-primary">Primär</span>
            </li>
          </ul>
        </div>
      </div>
    `
    }).mount(el);
});
