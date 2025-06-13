import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.prod.js';

document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('project-contacts-app');
    if (!el) {
        console.error('Element #project-contacts-app not found');
        return;
    }
    const project = JSON.parse(el.dataset.project || '{}');
    const contacts = JSON.parse(el.dataset.contacts || '[]');
    const customers = JSON.parse(el.dataset.customers || '[]');

    createApp({
        data() {
            return {
                project,
                contacts,
                customers,
                newContact: {
                    firstName: '',
                    lastName: '',
                    email: '',
                    phone: '',
                    position: '',
                    customerId: project.customerId
                }
            };
        },
        methods: {
            async saveContact() {
                try {
                    const response = await fetch(`/api/customers/${this.newContact.customerId}/contacts`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(this.newContact)
                    });

                    if (!response.ok) {
                        throw new Error('Failed to save contact');
                    }

                    const savedContact = await response.json();
                    this.contacts.push(savedContact);
                    this.resetForm();
                } catch (error) {
                    console.error('Error saving contact:', error);
                    alert('Fehler beim Speichern des Kontakts');
                }
            },
            resetForm() {
                this.newContact = {
                    firstName: '',
                    lastName: '',
                    email: '',
                    phone: '',
                    position: '',
                    customerId: this.project.customerId
                };
            }
        },
        template: `
            <div class="card">
                <div class="card-body">
                    <h5 class="mb-4">Kontakte</h5>
                    
                    <!-- Contact List -->
                    <div class="table-responsive mb-4">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Position</th>
                                    <th>E-Mail</th>
                                    <th>Telefon</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="contact in contacts" :key="contact.id">
                                    <td>{{ contact.firstName }} {{ contact.lastName }}</td>
                                    <td>{{ contact.position }}</td>
                                    <td>{{ contact.email }}</td>
                                    <td>{{ contact.phone }}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <!-- Add Contact Form -->
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0">Neuer Kontakt</h5>
                        </div>
                        <div class="card-body">
                            <form @submit.prevent="saveContact">
                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">Vorname</label>
                                        <input type="text" class="form-control" v-model="newContact.firstName" required>
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">Nachname</label>
                                        <input type="text" class="form-control" v-model="newContact.lastName" required>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">Position</label>
                                        <input type="text" class="form-control" v-model="newContact.position">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">E-Mail</label>
                                        <input type="email" class="form-control" v-model="newContact.email">
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">Telefon</label>
                                        <input type="tel" class="form-control" v-model="newContact.phone">
                                    </div>
                                </div>
                                <button type="submit" class="btn btn-primary">Kontakt speichern</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        `
    }).mount('#project-contacts-app');
});
