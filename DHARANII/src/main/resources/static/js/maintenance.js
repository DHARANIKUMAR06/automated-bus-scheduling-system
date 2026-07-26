// DTC Fleet Maintenance Ledger Client Logic

let maintModal = null;

document.addEventListener('DOMContentLoaded', () => {
    maintModal = new bootstrap.Modal(document.getElementById('maintModal'));
    
    // Check Permissions (Only Admin/Manager can schedule maintenance)
    const user = getLoggedUser();
    const addBtn = document.getElementById('add-maint-btn');
    if (user && user.role !== 'ROLE_ADMIN' && user.role !== 'ROLE_DEPOT_MANAGER') {
        if (addBtn) addBtn.style.display = 'none';
    }

    loadMaintenanceRecords();
    loadBusDropdown();
    
    document.getElementById('maint-form').addEventListener('submit', handleFormSubmit);
    
    // Dynamically show/hide technician notes based on status (completed status requires notes)
    document.getElementById('maint-status').addEventListener('change', (e) => {
        const notesSec = document.getElementById('tech-notes-section');
        const notesInput = document.getElementById('maint-notes');
        if (e.target.value === 'COMPLETED') {
            notesInput.required = true;
        } else {
            notesInput.required = false;
        }
    });
});

async function loadMaintenanceRecords() {
    const tbody = document.getElementById('maint-table-body');
    tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4"><span class="spinner-border spinner-border-sm" role="status"></span> Loading maintenance history...</td></tr>`;

    try {
        const records = await apiRequest('/api/maintenance');
        tbody.innerHTML = '';

        if (records.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4 text-secondary">No vehicle maintenance logs recorded.</td></tr>`;
            return;
        }

        const user = getLoggedUser();
        const canWrite = user && (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_DEPOT_MANAGER');
        const isAdmin = user && user.role === 'ROLE_ADMIN';

        records.forEach(r => {
            const tr = document.createElement('tr');
            
            let statusBadge = 'badge-warning'; // yellow (scheduled)
            if (r.status === 'IN_PROGRESS') statusBadge = 'badge-active'; // blue (running)
            if (r.status === 'COMPLETED') statusBadge = 'badge-completed'; // green (done)

            let actionsHtml = `<span class="text-secondary small">Unauthorized</span>`;
            if (canWrite) {
                actionsHtml = `
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="openEditModal(${r.id})"><i class="bi bi-pencil"></i></button>
                    ${isAdmin ? `<button class="btn btn-sm btn-outline-danger" onclick="deleteRecord(${r.id})"><i class="bi bi-trash"></i></button>` : ''}
                `;
            }

            tr.innerHTML = `
                <td>#${r.id}</td>
                <td class="fw-bold text-primary">${r.busNumber}</td>
                <td>${r.serviceDate}</td>
                <td><span class="badge bg-secondary">${r.serviceType}</span></td>
                <td class="small text-secondary" style="max-width: 200px;">${r.description || 'N/A'}</td>
                <td class="fw-bold">₹${r.cost.toLocaleString('en-IN')}</td>
                <td><span class="badge-status ${statusBadge}">${r.status.replace('_', ' ')}</span></td>
                <td class="small text-secondary" style="max-width: 200px;">${r.technicianNotes || 'None'}</td>
                <td>${actionsHtml}</td>
            `;
            tbody.appendChild(tr);
        });

    } catch (error) {
        showToast('Load Error', 'Unable to fetch maintenance history.', 'danger');
        tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4 text-danger">Error loading data.</td></tr>`;
    }
}

async function loadBusDropdown() {
    try {
        const buses = await apiRequest('/api/buses');
        const select = document.getElementById('maint-bus');
        if (select) {
            select.innerHTML = buses.map(b => `<option value="${b.id}">${b.busNumber} (${b.model})</option>`).join('');
        }
    } catch (error) {
        console.error('Failed to load buses dropdown:', error);
    }
}

function openCreateModal() {
    document.getElementById('maint-form').reset();
    document.getElementById('maint-id').value = '';
    document.getElementById('maintModalTitle').innerText = 'Schedule Maintenance';
    document.getElementById('maint-notes').required = false;
}

async function openEditModal(id) {
    try {
        const r = await apiRequest(`/api/maintenance/${id}`);
        document.getElementById('maint-id').value = r.id;
        document.getElementById('maint-bus').value = r.busId;
        document.getElementById('maint-date').value = r.serviceDate;
        document.getElementById('maint-type').value = r.serviceType;
        document.getElementById('maint-cost').value = r.cost;
        document.getElementById('maint-status').value = r.status;
        document.getElementById('maint-desc').value = r.description || '';
        document.getElementById('maint-notes').value = r.technicianNotes || '';

        if (r.status === 'COMPLETED') {
            document.getElementById('maint-notes').required = true;
        } else {
            document.getElementById('maint-notes').required = false;
        }

        document.getElementById('maintModalTitle').innerText = 'Update Service Entry';
        maintModal.show();
    } catch (error) {
        showToast('Load Error', 'Unable to fetch maintenance record.', 'danger');
    }
}

async function handleFormSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('maint-id').value;

    const payload = {
        busId: parseInt(document.getElementById('maint-bus').value),
        serviceDate: document.getElementById('maint-date').value,
        serviceType: document.getElementById('maint-type').value,
        cost: parseFloat(document.getElementById('maint-cost').value),
        status: document.getElementById('maint-status').value,
        description: document.getElementById('maint-desc').value,
        technicianNotes: document.getElementById('maint-notes').value
    };

    try {
        if (id) {
            await apiRequest(`/api/maintenance/${id}`, {
                method: 'PUT',
                body: JSON.stringify(payload)
            });
            showToast('Success', 'Service entry successfully updated.', 'success');
        } else {
            await apiRequest('/api/maintenance', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            showToast('Success', 'Vehicle maintenance task scheduled.', 'success');
        }

        maintModal.hide();
        loadMaintenanceRecords();
    } catch (error) {
        showToast('Failed', error.message || 'Unable to save service details.', 'danger');
    }
}

async function deleteRecord(id) {
    if (!confirm('Are you sure you want to delete this maintenance entry?')) return;

    try {
        await apiRequest(`/api/maintenance/${id}`, {
            method: 'DELETE'
        });
        showToast('Success', 'Record deleted successfully.', 'success');
        loadMaintenanceRecords();
    } catch (error) {
        showToast('Deletion Failed', error.message || 'Unable to delete record.', 'danger');
    }
}
