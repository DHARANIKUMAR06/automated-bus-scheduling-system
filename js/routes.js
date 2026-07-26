// DTC Route & Stop Sequencing Client Logic

let routeModal = null;

document.addEventListener('DOMContentLoaded', () => {
    routeModal = new bootstrap.Modal(document.getElementById('routeModal'));
    
    // Check Permissions (Only Admin/Scheduler can write route configurations)
    const user = getLoggedUser();
    const addBtn = document.getElementById('add-route-btn');
    if (user && user.role !== 'ROLE_ADMIN' && user.role !== 'ROLE_SCHEDULER') {
        if (addBtn) addBtn.style.display = 'none';
    }
    
    loadRoutes();
    
    document.getElementById('route-form').addEventListener('submit', handleFormSubmit);
});

async function loadRoutes() {
    const searchVal = document.getElementById('search-route-input').value;
    const tbody = document.getElementById('route-table-body');
    tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4"><span class="spinner-border spinner-border-sm" role="status"></span> Loading routes...</td></tr>`;

    try {
        let endpoint = '/api/routes';
        if (searchVal.trim()) {
            endpoint += `?search=${encodeURIComponent(searchVal)}`;
        }

        const routes = await apiRequest(endpoint);
        tbody.innerHTML = '';

        if (routes.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4 text-secondary">No routes configured.</td></tr>`;
            return;
        }

        const user = getLoggedUser();
        const canWrite = user && (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_SCHEDULER');
        const isAdmin = user && user.role === 'ROLE_ADMIN';

        routes.forEach(route => {
            const tr = document.createElement('tr');
            
            let statusBadge = 'badge-available';
            if (route.status === 'TEMPORARILY_CLOSED') statusBadge = 'badge-outofservice';

            let actionsHtml = `<span class="text-secondary small">Unauthorized</span>`;
            if (canWrite) {
                actionsHtml = `
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="openEditModal(${route.id})"><i class="bi bi-pencil"></i></button>
                    ${isAdmin ? `<button class="btn btn-sm btn-outline-danger" onclick="deleteRoute(${route.id})"><i class="bi bi-trash"></i></button>` : ''}
                `;
            }

            tr.innerHTML = `
                <td>${route.id}</td>
                <td class="fw-bold text-primary">${route.routeNumber}</td>
                <td>${route.startLocation}</td>
                <td>${route.endLocation}</td>
                <td>${route.distanceKm} km</td>
                <td>${route.estimatedDurationMins} mins</td>
                <td><span class="badge-status ${statusBadge}">${route.status.replace('_', ' ')}</span></td>
                <td><span class="badge bg-secondary">${route.stops.length} stops</span></td>
                <td>${actionsHtml}</td>
            `;
            tbody.appendChild(tr);
        });

    } catch (error) {
        showToast('Load Error', 'Unable to fetch routes list.', 'danger');
        tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4 text-danger">Error loading data.</td></tr>`;
    }
}

function openCreateModal() {
    document.getElementById('route-form').reset();
    document.getElementById('route-id').value = '';
    document.getElementById('stops-container').innerHTML = '';
    document.getElementById('routeModalTitle').innerText = 'Create New Route';
    
    // Add two default stop entries for Start and End terminals
    addStopRow('Start Terminal', 0.0, 0);
    addStopRow('End Terminal', 10.0, 30);
}

function addStopRow(name = '', distance = '', time = '') {
    const container = document.getElementById('stops-container');
    const rowId = 'stop-row-' + Date.now() + Math.random().toString(36).substr(2, 4);
    
    const row = document.createElement('div');
    row.className = 'row mb-2 align-items-center stop-item-row';
    row.id = rowId;
    
    row.innerHTML = `
        <div class="col-md-5">
            <input type="text" class="form-control form-control-custom stop-name-input" placeholder="Stop Name" value="${name}" required>
        </div>
        <div class="col-md-3">
            <input type="number" step="0.1" class="form-control form-control-custom stop-dist-input" placeholder="Dist from start (KM)" value="${distance}" required>
        </div>
        <div class="col-md-3">
            <input type="number" class="form-control form-control-custom stop-time-input" placeholder="Time offset (Mins)" value="${time}" required>
        </div>
        <div class="col-md-1">
            <button type="button" class="btn btn-sm btn-outline-danger" onclick="document.getElementById('${rowId}').remove()"><i class="bi bi-trash"></i></button>
        </div>
    `;
    container.appendChild(row);
}

async function openEditModal(id) {
    try {
        const route = await apiRequest(`/api/routes/${id}`);
        document.getElementById('route-id').value = route.id;
        document.getElementById('route-number').value = route.routeNumber;
        document.getElementById('route-status').value = route.status;
        document.getElementById('route-start').value = route.startLocation;
        document.getElementById('route-end').value = route.endLocation;
        document.getElementById('route-distance').value = route.distanceKm;
        document.getElementById('route-duration').value = route.estimatedDurationMins;

        // Render existing stops
        const container = document.getElementById('stops-container');
        container.innerHTML = '';
        route.stops.forEach(s => {
            addStopRow(s.stopName, s.distanceFromStart, s.estimatedTimeFromStart);
        });

        document.getElementById('routeModalTitle').innerText = 'Edit Route';
        routeModal.show();
    } catch (error) {
        showToast('Load Error', 'Unable to fetch route details.', 'danger');
    }
}

async function handleFormSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('route-id').value;

    // Gather and sequence stops data
    const stopRows = document.querySelectorAll('.stop-item-row');
    const stopsList = [];
    let seq = 1;
    
    stopRows.forEach(row => {
        const name = row.querySelector('.stop-name-input').value;
        const dist = parseFloat(row.querySelector('.stop-dist-input').value);
        const time = parseInt(row.querySelector('.stop-time-input').value);
        
        stopsList.push({
            stopName: name,
            sequenceNumber: seq++,
            distanceFromStart: dist,
            estimatedTimeFromStart: time
        });
    });

    if (stopsList.length < 2) {
        showToast('Validation Error', 'A route must contain at least 2 stops (start & end terminals).', 'danger');
        return;
    }

    const payload = {
        routeNumber: document.getElementById('route-number').value,
        status: document.getElementById('route-status').value,
        startLocation: document.getElementById('route-start').value,
        endLocation: document.getElementById('route-end').value,
        distanceKm: parseFloat(document.getElementById('route-distance').value),
        estimatedDurationMins: parseInt(document.getElementById('route-duration').value),
        stops: stopsList
    };

    try {
        if (id) {
            await apiRequest(`/api/routes/${id}`, {
                method: 'PUT',
                body: JSON.stringify(payload)
            });
            showToast('Success', 'Route updated successfully.', 'success');
        } else {
            await apiRequest('/api/routes', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            showToast('Success', 'New route registered successfully.', 'success');
        }

        routeModal.hide();
        loadRoutes();
    } catch (error) {
        showToast('Failed', error.message || 'Unable to save route.', 'danger');
    }
}

async function deleteRoute(id) {
    if (!confirm('Are you sure you want to delete this route? This will wipe out all corresponding stops and timetables!')) return;

    try {
        await apiRequest(`/api/routes/${id}`, {
            method: 'DELETE'
        });
        showToast('Success', 'Route successfully deleted.', 'success');
        loadRoutes();
    } catch (error) {
        showToast('Deletion Failed', error.message || 'Unable to delete route.', 'danger');
    }
}
