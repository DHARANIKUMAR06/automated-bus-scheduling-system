// DTC Bus Fleet Operations Client Logic

let busModal = null;

document.addEventListener('DOMContentLoaded', () => {
    busModal = new bootstrap.Modal(document.getElementById('busModal'));
    
    // Check Permissions (Only Admin/Manager can write data)
    const user = getLoggedUser();
    const addBtn = document.getElementById('add-bus-btn');
    if (user && user.role !== 'ROLE_ADMIN' && user.role !== 'ROLE_DEPOT_MANAGER') {
        if (addBtn) addBtn.style.display = 'none';
    }
    
    loadBuses();
    
    // Bind Submit action
    document.getElementById('bus-form').addEventListener('submit', handleFormSubmit);
});

async function loadBuses() {
    const searchVal = document.getElementById('search-bus-input').value;
    const tbody = document.getElementById('bus-table-body');
    tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4"><span class="spinner-border spinner-border-sm" role="status"></span> Loading fleet data...</td></tr>`;
    
    try {
        let endpoint = '/api/buses';
        if (searchVal.trim()) {
            endpoint += `?search=${encodeURIComponent(searchVal)}`;
        }
        
        const buses = await apiRequest(endpoint);
        tbody.innerHTML = '';
        
        if (buses.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4 text-secondary">No buses found in fleet registry.</td></tr>`;
            return;
        }
        
        const user = getLoggedUser();
        const canWrite = user && (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_DEPOT_MANAGER');
        const isAdmin = user && user.role === 'ROLE_ADMIN';
        
        buses.forEach(bus => {
            const tr = document.createElement('tr');
            
            let statusBadge = 'badge-active';
            if (bus.status === 'MAINTENANCE') statusBadge = 'badge-maintenance';
            if (bus.status === 'OUT_OF_SERVICE') statusBadge = 'badge-outofservice';
            
            let actionsHtml = `<span class="text-secondary small">Unauthorized</span>`;
            if (canWrite) {
                actionsHtml = `
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="openEditModal(${bus.id})"><i class="bi bi-pencil"></i></button>
                    ${isAdmin ? `<button class="btn btn-sm btn-outline-danger" onclick="deleteBus(${bus.id})"><i class="bi bi-trash"></i></button>` : ''}
                `;
            }
            
            tr.innerHTML = `
                <td>${bus.id}</td>
                <td class="fw-bold text-primary">${bus.busNumber}</td>
                <td>${bus.model}</td>
                <td>${bus.capacity}</td>
                <td><span class="badge bg-secondary">${bus.fuelType}</span></td>
                <td><span class="badge-status ${statusBadge}">${bus.status}</span></td>
                <td>${bus.depotName || 'Unassigned'}</td>
                <td>${bus.mileageKm.toFixed(1)} km</td>
                <td>${actionsHtml}</td>
            `;
            tbody.appendChild(tr);
        });
        
    } catch (error) {
        showToast('Load Error', 'Unable to fetch bus list.', 'danger');
        tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4 text-danger">Error loading data.</td></tr>`;
    }
}

function openCreateModal() {
    document.getElementById('bus-form').reset();
    document.getElementById('bus-id').value = '';
    document.getElementById('busModalTitle').innerText = 'Add New Bus';
    // Clear disabled status
    document.getElementById('bus-number').disabled = false;
}

async function openEditModal(id) {
    try {
        const bus = await apiRequest(`/api/buses/${id}`);
        document.getElementById('bus-id').value = bus.id;
        document.getElementById('bus-number').value = bus.busNumber;
        // Lock bus number on update to avoid key conflicts
        document.getElementById('bus-number').disabled = true;
        
        document.getElementById('bus-model').value = bus.model;
        document.getElementById('bus-capacity').value = bus.capacity;
        document.getElementById('bus-fuel').value = bus.fuelType;
        document.getElementById('bus-status').value = bus.status;
        document.getElementById('bus-depot').value = bus.depotId || '1';
        document.getElementById('bus-mileage').value = bus.mileageKm;
        
        document.getElementById('busModalTitle').innerText = 'Edit Bus';
        busModal.show();
    } catch (error) {
        showToast('Load Error', 'Unable to fetch bus details.', 'danger');
    }
}

async function handleFormSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('bus-id').value;
    
    const payload = {
        busNumber: document.getElementById('bus-number').value,
        model: document.getElementById('bus-model').value,
        capacity: parseInt(document.getElementById('bus-capacity').value),
        fuelType: document.getElementById('bus-fuel').value,
        status: document.getElementById('bus-status').value,
        depotId: parseInt(document.getElementById('bus-depot').value),
        mileageKm: parseFloat(document.getElementById('bus-mileage').value)
    };
    
    try {
        if (id) {
            // Update
            await apiRequest(`/api/buses/${id}`, {
                method: 'PUT',
                body: JSON.stringify(payload)
            });
            showToast('Bus Updated', 'Vehicle details successfully saved.', 'success');
        } else {
            // Create
            await apiRequest('/api/buses', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            showToast('Bus Created', 'Vehicle successfully registered.', 'success');
        }
        
        busModal.hide();
        loadBuses();
    } catch (error) {
        showToast('Operation Failed', error.message || 'Unable to save bus details.', 'danger');
    }
}

async function deleteBus(id) {
    if (!confirm('Are you sure you want to remove this vehicle from fleet operations?')) return;
    
    try {
        await apiRequest(`/api/buses/${id}`, {
            method: 'DELETE'
        });
        showToast('Bus Removed', 'Vehicle successfully deleted.', 'success');
        loadBuses();
    } catch (error) {
        showToast('Deletion Failed', error.message || 'Unable to delete bus.', 'danger');
    }
}
