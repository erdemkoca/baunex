import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('time-tracking-overview-app');
    if (!el) {
        console.error('Element #time-tracking-overview-app not found!');
        return;
    }

    const employees = JSON.parse(el.dataset.employees || '[]');
    const projects = JSON.parse(el.dataset.projects || '[]');
    const pendingApprovals = JSON.parse(el.dataset.pendingApprovals || '[]');
    const recentTimeEntries = JSON.parse(el.dataset.recentTimeEntries || '[]');
    const weeklyStats = JSON.parse(el.dataset.weeklyStats || '[]');
    const pendingHolidays = JSON.parse(el.dataset.pendingHolidays || '[]');
    const projectStats = JSON.parse(el.dataset.projectStats || '[]');

    const app = createApp({
        data() {
            return {
                employees,
                projects,
                pendingApprovals,
                recentTimeEntries,
                weeklyStats,
                pendingHolidays,
                projectStats,
                loading: false
            };
        },
        computed: {
            totalPendingApprovals() {
                return this.pendingApprovals.length;
            },
            totalPendingHolidays() {
                return this.pendingHolidays.length;
            },
            totalWeeklyHours() {
                return this.weeklyStats.reduce((sum, stat) => sum + stat.totalWorked, 0);
            },
            totalExpectedHours() {
                return this.weeklyStats.reduce((sum, stat) => sum + stat.totalExpected, 0);
            },
            activeProjects() {
                return this.projectStats.filter(stat => stat.totalHours > 0).length;
            },
            totalProjectHours() {
                return this.projectStats.reduce((sum, stat) => sum + stat.totalHours, 0);
            }
        },
        methods: {
            formatDate(date) {
                return new Date(date).toLocaleDateString('de-CH');
            },
            formatHours(hours) {
                return hours.toFixed(1) + 'h';
            },
            getStatusClass(status) {
                return {
                    'text-success': status === 'approved',
                    'text-warning': status === 'pending',
                    'text-danger': status === 'rejected'
                };
            },
            navigateToTimeEntry(id) {
                window.location.href = `/timetracking/${id}`;
            },
            navigateToCalendar() {
                window.location.href = '/timetracking/calendar';
            },
            navigateToReports() {
                window.location.href = '/timetracking/reports';
            },
            navigateToAbsences() {
                window.location.href = '/timetracking/absences';
            }
        }
    });

    app.mount('#time-tracking-overview-app');
}); 