// DTC Conductor Management Client Logic

let conductorModal = null;

document.addEventListener('DOMContentLoaded', () => {
    conductorModal = new bootstrap.Modal(document.getElementById('conductorModal'));
    
    // Check Permissions (Only Admin/Manager can write data)
    const user = getLoggedUser();
    const addBtn = document.getElementById('add-conductor-btn');
    if (user && user.role !== 'ROLE_ADMIN' && user.role !== 'ROLE_DEPOT_MANAGER') {
        if (addBtn) addBtn.style.display = 'none';
    }
    
    loadConductors();
    
    document.getElementById('conductor-form').addEventListener('submit', handleFormSubmit);
});

async function loadConductors() {
    const searchVal = document.getElementById('search-conductor-input').value;
    const tbody = document.getElementById('conductor-table-body');
    tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4"><span class="spinner-border spinner-border-sm" role="status"></span> Loading conductors...</td></tr>`;

    try {
        let endpoint = '/api/conductors';
        if (searchVal.trim()) {
            endpoint += `?search=${encodeURIComponent(searchVal)}`;
        }

        const conductors = await apiRequest(endpoint);
        tbody.innerHTML = '';

        if (conductors.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4 text-secondary">No conductors registered.</td></tr>`;
            return;
        }

        const user = getLoggedUser();
        const canWrite = user && (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_DEPOT_MANAGER');
        const isAdmin = user && user.role === 'ROLE_ADMIN';

        conductors.forEach(c => {
            const tr = document.createElement('tr');
            
            let statusBadge = 'badge-available';
            if (c.status === 'ON_TRIP') statusBadge = 'badge-active';
            if (c.status === 'LEAVE') statusBadge = 'badge-warning';

            let actionsHtml = `<span class="text-secondary small">Unauthorized</span>`;
            if (canWrite) {
                actionsHtml = `
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="openEditModal(${c.id})"><i class="bi bi-pencil"></i></button>
                    ${isAdmin ? `<button class="btn btn-sm btn-outline-danger" onclick="deleteConductor(${c.id})"><i class="bi bi-trash"></i></button>` : ''}
                `;
            }

            tr.innerHTML = `
                <td>${c.id}</td>
                <td class="fw-bold">${c.fullName}</td>
                <td class="text-secondary">${c.employeeId}</td>
                <td><span class="badge-status ${statusBadge}">${c.status}</span></td>
                <td>${c.depotName || 'Unassigned'}</td>
                <td>${c.phone || 'N/A'}</td>
                <td>${c.email || 'N/A'}</td>
                <td>${actionsHtml}</td>
            `;
            tbody.appendChild(tr);
        });

    } catch (error) {
        showToast('Load Error', 'Unable to fetch conductors list.', 'danger');
        tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4 text-danger">Error loading data.</td></tr>`;
    }
}

function openCreateModal() {
    document.getElementById('conductor-form').reset();
    document.getElementById('conductor-id').value = '';
    document.getElementById('conductor-username').disabled = false;
    document.getElementById('conductor-password').required = true;
    document.getElementById('credentials-section').style.display = 'block';
    document.getElementById('conductorModalTitle').innerText = 'Add New Conductor';
}

async function openEditModal(id) {
    try {
        const c = await apiRequest(`/api/conductors/${id}`);
        document.getElementById('conductor-id').value = c.id;
        document.getElementById('conductor-name').value = c.fullName;
        document.getElementById('conductor-employee-id').value = c.employeeId;
        document.getElementById('conductor-email').value = c.email || '';
        document.getElementById('conductor-phone').value = c.phone || '';
        document.getElementById('conductor-status').value = c.status;
        document.getElementById('conductor-depot').value = c.depotId || '1';

        // Hide account credential fields on update
        document.getElementById('credentials-section').style.display = 'none';
        document.getElementById('conductor-username').disabled = true;
        document.getElementById('conductor-password').required = false;

        document.getElementById('conductorModalTitle').innerText = 'Edit Conductor';
        conductorModal.show();
    } catch (error) {
        showToast('Load Error', 'Unable to fetch conductor details.', 'danger');
    }
}

async function handleFormSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('conductor-id').value;

    const payload = {
        fullName: document.getElementById('conductor-name').value,
        employeeId: document.getElementById('conductor-employee-id').value,
        email: document.getElementById('conductor-email').value,
        phone: document.getElementById('conductor-phone').value,
        status: document.getElementById('conductor-status').value,
        depotId: parseInt(document.getElementById('conductor-depot').value)
    };

    if (!id) {
        payload.username = document.getElementById('conductor-username').value;
        payload.password = document.getElementById('conductor-password').value;
    }

    try {
        if (id) {
            await apiRequest(`/api/conductors/${id}`, {
                method: 'PUT',
                body: JSON.stringify(payload)
            });
            showToast('Success', 'Conductor details successfully updated.', 'success');
        } else {
            await apiRequest('/api/conductors', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            showToast('Success', 'Conductor successfully registered.', 'success');
        }

        conductorModal.hide();
        loadConductors();
    } catch (error) {
        showToast('Failed', error.message || 'Unable to save conductor.', 'danger');
    }
}

async function deleteConductor(id) {
    if (!confirm('Are you sure you want to delete this conductor record? All login logs will be cleared.')) return;

    try {
        await apiRequest(`/api/conductors/${id}`, {
            method: 'DELETE'
        });
        showToast('Success', 'Conductor record deleted successfully.', 'success');
        loadConductors();
    } catch (error) {
        showToast('Deletion Failed', error.message || 'Unable to delete conductor.', 'danger');
    }
}
