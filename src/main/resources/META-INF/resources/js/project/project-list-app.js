import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('project-list-app');
    const projects = JSON.parse(el.dataset.projects || '[]');

    createApp({
        data() {
            return {
                projects,
                // Falls Du noch Filtern/Sortieren willst:
                filterStatus: 'ALL',
                statuses: [
                    { key: 'ALL',    label: 'Alle',          badge: 'secondary' },
                    { key: 'PLANNED',      label: 'Geplant',       badge: 'secondary' },
                    { key: 'IN_PROGRESS',  label: 'In Bearbeitung',badge: 'warning' },
                    { key: 'COMPLETED',    label: 'Abgeschlossen', badge: 'success' },
                    { key: 'CANCELLED',    label: 'Abgebrochen',   badge: 'danger' }
                ]
            }
        },
        computed: {
            filteredProjects() {
                if (this.filterStatus === 'ALL') return this.projects;
                return this.projects.filter(p => p.status === this.filterStatus);
            }
        },
        methods: {
            navigateTo(id) {
                window.location.href = `/projects/${id}`;
            },
            async deleteProject(id) {
                if (!confirm('Wirklich löschen?')) return;
                const res = await fetch(`/projects/${id}/delete`, { method: 'POST' });
                if (res.ok) {
                    this.projects = this.projects.filter(p => p.id !== id);
                } else {
                    alert('Fehler beim Löschen');
                }
            },
            formatDate(d) {
                return d ? new Date(d).toLocaleDateString('de-CH') : '—';
            }
        },
        template: `
      <div class="mb-4 d-flex justify-content-between align-items-center">
        <h4 class="mb-0">Alle Projekte</h4>
        <a href="/projects/new" class="btn btn-primary">
          <i class="bi bi-plus-circle me-2"></i>Neues Projekt
        </a>
      </div>

      <!-- optionaler Status-Filter -->
      <div class="mb-3">
        <div class="btn-group">
          <button v-for="s in statuses"
                  :key="s.key"
                  :class="['btn','btn-outline-'+s.badge, { active: filterStatus===s.key }]"
                  @click="filterStatus=s.key">
            {{ s.label }}
          </button>
        </div>
      </div>

      <div class="card mb-4">
        <div class="card-body p-0">
          <div v-if="filteredProjects.length===0" class="text-center text-muted py-4">
            Keine Projekte gefunden.
          </div>
          <div v-else class="table-responsive">
            <table class="table table-hover mb-0">
              <thead class="table-light">
                <tr>
                  <th>Nr.</th><th>Name</th><th>Kunde</th>
                  <th>Start</th><th>Ende</th><th>Budget</th><th>Status</th>
                  <th class="text-end">Aktionen</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in filteredProjects" :key="p.id" class="align-middle" style="cursor:pointer"
                    @click="navigateTo(p.id)">
                  <td>{{ p.projectNumberFormatted }}</td>
                  <td>{{ p.name }}</td>
                  <td>{{ p.customerName }}</td>
                  <td>{{ formatDate(p.startDate) }}</td>
                  <td>{{ formatDate(p.endDate) }}</td>
                  <td>{{ p.budget }} CHF</td>
                  <td>
                    <span :class="['badge','bg-'+(statuses.find(s=>s.key===p.status)?.badge)]">
                      {{ statuses.find(s=>s.key===p.status)?.label || p.status }}
                    </span>
                  </td>
                  <td class="text-end">
                    <button @click.stop="deleteProject(p.id)" 
                            class="btn btn-sm btn-outline-danger" 
                            title="Löschen">
                      <i class="bi bi-trash"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Platzhalter für Notizen -->
      <div class="card">
        <div class="card-header">Kürzliche Notizen (Platzhalter)</div>
        <div class="card-body">
          <p class="text-muted">Hier könnten Ihre neuesten Projekt-Notizen auftauchen.</p>
        </div>
      </div>
    `
    }).mount(el);
});
