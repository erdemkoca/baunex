import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('time-tracking-overview-app');
    if (!el) {
        console.error('Element #time-tracking-overview-app not found!');
        return;
    }

    const overviewData = JSON.parse(el.dataset.overview || '{}');

    const app = createApp({
        data() {
            return {
                overview: overviewData,
                loading: false
            };
        },
        computed: {
            kpis() {
                return this.overview.kpis || {};
            },
            alerts() {
                return this.overview.alerts || [];
            },
            quickActions() {
                return this.overview.quickActions || [];
            },
            calendarDays() {
                return this.overview.calendarDays || [];
            },
            recentActivities() {
                return this.overview.recentActivities || [];
            },
            teamMembers() {
                return this.overview.teamMembers || [];
            },
            trends() {
                return this.overview.trends || [];
            }
        },
        methods: {
            formatHours(hours) {
                return hours.toFixed(1) + 'h';
            },
            formatDate(date) {
                return new Date(date).toLocaleDateString('de-CH');
            },
            getAlertClass(severity) {
                return {
                    'alert-danger': severity === 'HIGH',
                    'alert-warning': severity === 'MEDIUM',
                    'alert-info': severity === 'LOW'
                };
            },
            getBalanceClass(balance) {
                return {
                    'text-success': balance > 0,
                    'text-danger': balance < 0,
                    'text-muted': balance === 0
                };
            },
            getCalendarDayClass(day) {
                const classes = ['calendar-day'];
                
                if (day.isWeekend) classes.push('weekend');
                if (day.isPublicHoliday) classes.push('public-holiday');
                if (day.hasHolidays) classes.push('holiday');
                if (day.hasTimeEntries) classes.push('has-entries');
                
                return classes.join(' ');
            },
            navigateToQuickAction(action) {
                window.location.href = action.url;
            },
            handleAlertAction(alert) {
                if (alert.actionUrl) {
                    window.location.href = alert.actionUrl;
                }
            },
            refreshData() {
                this.loading = true;
                // TODO: Implement refresh logic
                setTimeout(() => {
                    this.loading = false;
                }, 1000);
            }
        },
        template: `
        <div class="container-fluid">
            <!-- Loading Indicator -->
            <div v-if="loading" class="text-center my-4">
                <div class="spinner-border" role="status">
                    <span class="visually-hidden">Laden...</span>
                </div>
            </div>

            <!-- Header with Refresh Button -->
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2>Zeiterfassung Übersicht</h2>
                <button @click="refreshData" class="btn btn-outline-secondary" :disabled="loading">
                    <i class="bi bi-arrow-clockwise me-2"></i>Aktualisieren
                </button>
            </div>

            <!-- Alerts Section -->
            <div v-if="alerts.length > 0" class="mb-4">
                <h4>Wichtige Hinweise</h4>
                <div v-for="alert in alerts" :key="alert.id" 
                     :class="['alert', 'alert-dismissible', 'fade', 'show', getAlertClass(alert.severity)]">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <h5 class="alert-heading">{{ alert.title }}</h5>
                            <p class="mb-0">{{ alert.message }}</p>
                        </div>
                        <button v-if="alert.actionLabel" 
                                @click="handleAlertAction(alert)"
                                class="btn btn-sm btn-outline-light">
                            {{ alert.actionLabel }}
                        </button>
                    </div>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </div>

            <!-- KPI Cards -->
            <div class="row mb-4">
                <div class="col-md-3 mb-3">
                    <div class="card text-center">
                        <div class="card-body">
                            <h5 class="card-title">Wochensaldo</h5>
                            <h3 :class="getBalanceClass(kpis.weeklyBalance)">
                                {{ formatHours(kpis.weeklyBalance) }}
                            </h3>
                            <p class="card-text text-muted">Gearbeitet: {{ formatHours(kpis.workedHoursThisWeek) }}</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="card text-center">
                        <div class="card-body">
                            <h5 class="card-title">Kumuliertes Saldo</h5>
                            <h3 :class="getBalanceClass(kpis.cumulativeBalance)">
                                {{ formatHours(kpis.cumulativeBalance) }}
                            </h3>
                            <p class="card-text text-muted">Gesamtstundenkonto</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="card text-center">
                        <div class="card-body">
                            <h5 class="card-title">Verbleibende Urlaubstage</h5>
                            <h3 class="text-primary">{{ kpis.remainingVacationDays }}</h3>
                            <p class="card-text text-muted">Genehmigt: {{ kpis.approvedAbsenceDays }} Tage</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="card text-center">
                        <div class="card-body">
                            <h5 class="card-title">Offene Anträge</h5>
                            <h3 class="text-warning">{{ kpis.pendingVacationRequests }}</h3>
                            <p class="card-text text-muted">Fehlende Einträge: {{ kpis.missingTimeEntries }}</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Quick Actions -->
            <div class="row mb-4">
                <div class="col-12">
                    <h4>Quick Actions</h4>
                    <div class="d-flex gap-3 flex-wrap">
                        <button v-for="action in quickActions" 
                                :key="action.id"
                                @click="navigateToQuickAction(action)"
                                :class="['btn', 'btn-' + action.color, 'btn-lg']">
                            <i :class="['bi', action.icon, 'me-2']"></i>
                            {{ action.title }}
                        </button>
                    </div>
                </div>
            </div>

            <div class="row">
                <!-- Mini Calendar -->
                <div class="col-md-4 mb-4">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0">Kalenderwoche {{ overview.currentWeek }}</h5>
                        </div>
                        <div class="card-body">
                            <div class="calendar-mini">
                                <div class="calendar-header d-flex justify-content-between mb-2">
                                    <span>Mo</span>
                                    <span>Di</span>
                                    <span>Mi</span>
                                    <span>Do</span>
                                    <span>Fr</span>
                                    <span>Sa</span>
                                    <span>So</span>
                                </div>
                                <div class="calendar-grid">
                                    <div v-for="(day, index) in calendarDays" 
                                         :key="index"
                                         :class="['calendar-day', getCalendarDayClass(day)]"
                                         :title="formatDate(day.date) + ' - ' + formatHours(day.workedHours) + ' gearbeitet'">
                                        <div class="day-number">{{ new Date(day.date).getDate() }}</div>
                                        <div v-if="day.hasTimeEntries" class="day-indicator entries"></div>
                                        <div v-if="day.hasHolidays" class="day-indicator holiday"></div>
                                        <div v-if="day.isPublicHoliday" class="day-indicator public-holiday"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Recent Activities -->
                <div class="col-md-8 mb-4">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0">Letzte Aktivitäten</h5>
                        </div>
                        <div class="card-body">
                            <div v-if="recentActivities.length === 0" class="text-muted">
                                Keine Aktivitäten vorhanden.
                            </div>
                            <div v-else class="activity-timeline">
                                <div v-for="activity in recentActivities" 
                                     :key="activity.id"
                                     class="activity-item">
                                    <div class="activity-icon">
                                        <i class="bi bi-clock"></i>
                                    </div>
                                    <div class="activity-content">
                                        <div class="activity-title">{{ activity.title }}</div>
                                        <div class="activity-description">{{ activity.description }}</div>
                                        <div class="activity-time">{{ formatDate(activity.timestamp) }}</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Team Overview -->
            <div class="row mb-4">
                <div class="col-12">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0">Team-Übersicht</h5>
                        </div>
                        <div class="card-body">
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th>Mitarbeiter</th>
                                            <th>Gearbeitet (KW)</th>
                                            <th>Erwartet (KW)</th>
                                            <th>Saldo</th>
                                            <th>Ausstehende Genehmigungen</th>
                                            <th>Anstehende Urlaube</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr v-for="member in teamMembers" :key="member.employeeId">
                                            <td>{{ member.employeeName }}</td>
                                            <td>{{ formatHours(member.workedHoursThisWeek) }}</td>
                                            <td>{{ formatHours(member.expectedHoursThisWeek) }}</td>
                                            <td :class="getBalanceClass(member.balance)">
                                                {{ formatHours(member.balance) }}
                                            </td>
                                            <td>
                                                <span v-if="member.pendingApprovals > 0" 
                                                      class="badge bg-warning">
                                                    {{ member.pendingApprovals }}
                                                </span>
                                                <span v-else class="text-muted">-</span>
                                            </td>
                                            <td>
                                                <div v-for="vacation in member.upcomingVacations" 
                                                     :key="vacation.id"
                                                     class="small">
                                                    {{ formatDate(vacation.startDate) }} - {{ formatDate(vacation.endDate) }}
                                                    <span class="badge bg-info ms-1">{{ vacation.type }}</span>
                                                </div>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Trends -->
            <div class="row">
                <div class="col-12">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0">Trends (letzte 4 Wochen)</h5>
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-3 mb-3" v-for="trend in trends" :key="trend.label">
                                    <div class="text-center">
                                        <h6>{{ trend.label }}</h6>
                                        <div class="trend-stats">
                                            <div class="small text-muted">Gearbeitet: {{ formatHours(trend.workedHours) }}</div>
                                            <div class="small text-muted">Erwartet: {{ formatHours(trend.expectedHours) }}</div>
                                            <div :class="['fw-bold', getBalanceClass(trend.balance)]">
                                                Saldo: {{ formatHours(trend.balance) }}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        `
    });

    app.mount('#time-tracking-overview-app');
}); 