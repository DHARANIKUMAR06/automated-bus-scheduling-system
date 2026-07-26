// DTC Driver Management Client Logic

let driverModal = null;

document.addEventListener('DOMContentLoaded', () => {
    driverModal = new bootstrap.Modal(document.getElementById('driverModal'));
    
    // Check Permissions (Only Admin/Manager can write data)
    const user = getLoggedUser();
    const addBtn = document.getElementById('add-driver-btn');
    if (user && user.role !== 'ROLE_ADMIN' && user.role !== 'ROLE_DEPOT_MANAGER') {
        if (addBtn) addBtn.style.display = 'none';
    }
    
    loadDrivers();
    checkLicenseExpirations();
    
    document.getElementById('driver-form').addEventListener('submit', handleFormSubmit);
});

async function loadDrivers() {
    const searchVal = document.getElementById('search-driver-input').value;
    const tbody = document.getElementById('driver-table-body');
    tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4"><span class="spinner-border spinner-border-sm" role="status"></span> Loading drivers...</td></tr>`;

    try {
        let endpoint = '/api/drivers';
        if (searchVal.trim()) {
            endpoint += `?search=${encodeURIComponent(searchVal)}`;
        }

        const drivers = await apiRequest(endpoint);
        tbody.innerHTML = '';

        if (drivers.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4 text-secondary">No drivers registered.</td></tr>`;
            return;
        }

        const user = getLoggedUser();
        const canWrite = user && (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_DEPOT_MANAGER');
        const isAdmin = user && user.role === 'ROLE_ADMIN';

        drivers.forEach(driver => {
            const tr = document.createElement('tr');
            
            let statusBadge = 'badge-available';
            if (driver.status === 'ON_TRIP') statusBadge = 'badge-active';
            if (driver.status === 'LEAVE') statusBadge = 'badge-warning';
            if (driver.status === 'SUSPENDED') statusBadge = 'badge-outofservice';

            // Check if license is expiring (under 30 days)
            const expiry = new Date(driver.licenseExpiryDate);
            const now = new Date();
            const timeDiff = expiry.getTime() - now.getTime();
            const daysDiff = Math.ceil(timeDiff / (1000 * 3600 * 24));
            
            let nameHtml = driver.fullName;
            if (daysDiff <= 30) {
                nameHtml += ` <span class="badge bg-danger ms-1" style="font-size: 0.65rem" title="License expiring in ${daysDiff} days!"><i class="bi bi-exclamation-triangle"></i> Alert</span>`;
            }

            let actionsHtml = `<span class="text-secondary small">Unauthorized</span>`;
            if (canWrite) {
                actionsHtml = `
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="openEditModal(${driver.id})"><i class="bi bi-pencil"></i></button>
                    ${isAdmin ? `<button class="btn btn-sm btn-outline-danger" onclick="deleteDriver(${driver.id})"><i class="bi bi-trash"></i></button>` : ''}
                `;
            }

            tr.innerHTML = `
                <td>${driver.id}</td>
                <td class="fw-bold">${nameHtml}</td>
                <td class="text-secondary">${driver.licenseNumber}</td>
                <td><span class="${daysDiff <= 30 ? 'text-danger fw-bold' : ''}">${driver.licenseExpiryDate}</span></td>
                <td>${driver.experienceYears} yrs</td>
                <td><span class="badge-status ${statusBadge}">${driver.status}</span></td>
                <td>${driver.depotName || 'Unassigned'}</td>
                <td>${driver.phone || 'N/A'}</td>
                <td>${actionsHtml}</td>
            `;
            tbody.appendChild(tr);
        });

    } catch (error) {
        showToast('Load Error', 'Unable to fetch drivers list.', 'danger');
        tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4 text-danger">Error loading data.</td></tr>`;
    }
}

async function checkLicenseExpirations() {
    try {
        const expiring = await apiRequest('/api/drivers/license-expiring');
        const banner = document.getElementById('expiry-warning-banner');
        if (banner && expiring.length > 0) {
            banner.classList.remove('d-none');
            document.getElementById('expiry-warning-message').innerText = 
                `The following drivers have licenses expiring within 30 days: ${expiring.map(d => d.fullName).join(', ')}. Please initiate renewals.`;
        }
    } catch (error) {
        console.error('License warning check failed:', error);
    }
}

function openCreateModal() {
    document.getElementById('driver-form').reset();
    document.getElementById('driver-id').value = '';
    document.getElementById('driver-username').disabled = false;
    document.getElementById('driver-password').required = true;
    document.getElementById('credentials-section').style.display = 'block';
    document.getElementById('driverModalTitle').innerText = 'Add New Driver';
}

async function openEditModal(id) {
    try {
        const driver = await apiRequest(`/api/drivers/${id}`);
        document.getElementById('driver-id').value = driver.id;
        document.getElementById('driver-name').value = driver.fullName;
        document.getElementById('driver-email').value = driver.email || '';
        document.getElementById('driver-phone').value = driver.phone || '';
        document.getElementById('driver-license').value = driver.licenseNumber;
        document.getElementById('driver-expiry').value = driver.licenseExpiryDate;
        document.getElementById('driver-experience').value = driver.experienceYears;
        document.getElementById('driver-status').value = driver.status;
        document.getElementById('driver-depot').value = driver.depotId || '1';
        document.getElementById('driver-bus').value = driver.assignedBusId || '';

        // Hide account credential fields on update
        document.getElementById('credentials-section').style.display = 'none';
        document.getElementById('driver-username').disabled = true;
        document.getElementById('driver-password').required = false;

        document.getElementById('driverModalTitle').innerText = 'Edit Driver';
        driverModal.show();
    } catch (error) {
        showToast('Load Error', 'Unable to fetch driver details.', 'danger');
    }
}

async function handleFormSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('driver-id').value;

    const payload = {
        fullName: document.getElementById('driver-name').value,
        email: document.getElementById('driver-email').value,
        phone: document.getElementById('driver-phone').value,
        licenseNumber: document.getElementById('driver-license').value,
        licenseExpiryDate: document.getElementById('driver-expiry').value,
        experienceYears: parseInt(document.getElementById('driver-experience').value),
        status: document.getElementById('driver-status').value,
        depotId: parseInt(document.getElementById('driver-depot').value),
        assignedBusId: document.getElementById('driver-bus').value ? parseInt(document.getElementById('driver-bus').value) : null
    };

    if (!id) {
        // Adding creation fields
        payload.username = document.getElementById('driver-username').value;
        payload.password = document.getElementById('driver-password').value;
    }

    try {
        if (id) {
            await apiRequest(`/api/drivers/${id}`, {
                method: 'PUT',
                body: JSON.stringify(payload)
            });
            showToast('Success', 'Driver updated successfully.', 'success');
        } else {
            await apiRequest('/api/drivers', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            showToast('Success', 'Driver account registered successfully.', 'success');
        }

        driverModal.hide();
        loadDrivers();
    } catch (error) {
        showToast('Failed', error.message || 'Unable to save driver.', 'danger');
    }
}

async function deleteDriver(id) {
    if (!confirm('Are you sure you want to delete this driver? All portal login records will also be erased.')) return;

    try {
        await apiRequest(`/api/drivers/${id}`, {
            method: 'DELETE'
        });
        showToast('Success', 'Driver account successfully deleted.', 'success');
        loadDrivers();
    } catch (error) {
        showToast('Deletion Failed', error.message || 'Unable to delete driver.', 'danger');
    }
}
