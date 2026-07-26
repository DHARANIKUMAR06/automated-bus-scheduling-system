// DTC Timetable & Scheduler Operations Client Logic

let scheduleModal = null;
let autoModal = null;

document.addEventListener('DOMContentLoaded', () => {
    scheduleModal = new bootstrap.Modal(document.getElementById('scheduleModal'));
    autoModal = new bootstrap.Modal(document.getElementById('autoModal'));
    
    // Set default date to today
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('filter-date').value = today;
    document.getElementById('sched-date').value = today;
    document.getElementById('auto-date').value = today;

    // Check Permissions (Only Admin/Scheduler can write schedules)
    const user = getLoggedUser();
    const actionsPanel = document.getElementById('scheduler-actions');
    if (user && user.role !== 'ROLE_ADMIN' && user.role !== 'ROLE_SCHEDULER') {
        if (actionsPanel) actionsPanel.style.display = 'none';
    }

    loadSchedules();
    loadDropdownResources();
    
    document.getElementById('schedule-form').addEventListener('submit', handleManualSubmit);
    document.getElementById('auto-schedule-form').addEventListener('submit', handleAutoSubmit);
});

async function loadSchedules() {
    const dateVal = document.getElementById('filter-date').value;
    const tbody = document.getElementById('schedule-table-body');
    tbody.innerHTML = `<tr><td colspan="13" class="text-center py-4"><span class="spinner-border spinner-border-sm" role="status"></span> Loading timetables...</td></tr>`;

    try {
        const schedules = await apiRequest(`/api/schedules?date=${dateVal}`);
        tbody.innerHTML = '';

        if (schedules.length === 0) {
            tbody.innerHTML = `<tr><td colspan="13" class="text-center py-4 text-secondary">No schedules allocated for this date. Run auto-scheduler to populate.</td></tr>`;
            return;
        }

        const user = getLoggedUser();
        const isAdmin = user && user.role === 'ROLE_ADMIN';

        schedules.forEach(s => {
            const tr = document.createElement('tr');
            
            let statusBadge = 'badge-active';
            if (s.status === 'CANCELLED') statusBadge = 'badge-outofservice';

            let actionsHtml = `<span class="text-secondary small">Unauthorized</span>`;
            if (isAdmin) {
                actionsHtml = `
                    <button class="btn btn-sm btn-outline-danger" onclick="cancelSchedule(${s.id})" title="Cancel Schedule Slot"><i class="bi bi-x-circle"></i></button>
                `;
            }

            tr.innerHTML = `
                <td>${s.id}</td>
                <td class="fw-bold text-primary">${s.routeNumber}</td>
                <td>${s.startLocation}</td>
                <td>${s.endLocation}</td>
                <td class="fw-bold">${s.busNumber}</td>
                <td>${s.driverName}</td>
                <td>${s.conductorName}</td>
                <td class="text-success fw-bold">${s.departureTime.substring(0, 5)}</td>
                <td class="text-danger fw-bold">${s.arrivalTime.substring(0, 5)}</td>
                <td><span class="badge bg-secondary">${s.shiftType}</span></td>
                <td>${s.isPeakHour ? '<span class="badge bg-danger">Peak</span>' : '<span class="badge bg-light text-dark">Off-Peak</span>'}</td>
                <td><span class="badge-status ${statusBadge}">${s.status}</span></td>
                <td>${actionsHtml}</td>
            `;
            tbody.appendChild(tr);
        });

    } catch (error) {
        showToast('Load Error', 'Unable to fetch schedules list.', 'danger');
        tbody.innerHTML = `<tr><td colspan="13" class="text-center py-4 text-danger">Error loading data.</td></tr>`;
    }
}

async function loadDropdownResources() {
    try {
        // Load Routes
        const routes = await apiRequest('/api/routes');
        const routeSelects = [document.getElementById('sched-route'), document.getElementById('auto-route')];
        routeSelects.forEach(sel => {
            if (sel) {
                sel.innerHTML = routes.map(r => `<option value="${r.id}">Route ${r.routeNumber} (${r.startLocation} to ${r.endLocation})</option>`).join('');
            }
        });

        // Load Buses (only active ones)
        const buses = await apiRequest('/api/buses');
        const busSelect = document.getElementById('sched-bus');
        if (busSelect) {
            busSelect.innerHTML = buses.map(b => `<option value="${b.id}">${b.busNumber} - ${b.model} (${b.status})</option>`).join('');
        }

        // Load Drivers (only available ones)
        const drivers = await apiRequest('/api/drivers');
        const driverSelect = document.getElementById('sched-driver');
        if (driverSelect) {
            driverSelect.innerHTML = drivers.map(d => `<option value="${d.id}">${d.fullName} (Exp: ${d.licenseExpiryDate})</option>`).join('');
        }

        // Load Conductors (only available ones)
        const conductors = await apiRequest('/api/conductors');
        const conductorSelect = document.getElementById('sched-conductor');
        if (conductorSelect) {
            conductorSelect.innerHTML = conductors.map(c => `<option value="${c.id}">${c.fullName} - ${c.employeeId}</option>`).join('');
        }

    } catch (error) {
        console.error('Failed to load dropdown resources:', error);
    }
}

function openCreateModal() {
    document.getElementById('schedule-form').reset();
    document.getElementById('schedule-id').value = '';
    document.getElementById('sched-date').value = document.getElementById('filter-date').value;
    document.getElementById('scheduleModalTitle').innerText = 'Manual Schedule Allocation';
}

function openAutoModal() {
    document.getElementById('auto-schedule-form').reset();
    document.getElementById('auto-date').value = document.getElementById('filter-date').value;
}

async function handleManualSubmit(e) {
    e.preventDefault();

    const payload = {
        scheduleDate: document.getElementById('sched-date').value,
        routeId: parseInt(document.getElementById('sched-route').value),
        busId: parseInt(document.getElementById('sched-bus').value),
        driverId: parseInt(document.getElementById('sched-driver').value),
        conductorId: parseInt(document.getElementById('sched-conductor').value),
        departureTime: document.getElementById('sched-dep').value + ':00',
        arrivalTime: document.getElementById('sched-arr').value + ':00',
        shiftType: document.getElementById('sched-shift').value,
        isPeakHour: document.getElementById('sched-peak').checked
    };

    try {
        await apiRequest('/api/schedules', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        showToast('Success', 'Manual schedule allocated and live trip initialized!', 'success');
        scheduleModal.hide();
        loadSchedules();
    } catch (error) {
        showToast('Allocation Failed', error.message || 'Overlap or shift hours validation check failed.', 'danger');
    }
}

async function handleAutoSubmit(e) {
    e.preventDefault();
    
    const targetDate = document.getElementById('auto-date').value;
    const routeId = document.getElementById('auto-route').value;
    
    const submitBtn = e.target.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status"></span> Generating...`;

    try {
        const results = await apiRequest(`/api/schedules/auto-generate?date=${targetDate}&routeId=${routeId}`, {
            method: 'POST'
        });
        
        showToast('Algorithm Complete', `Auto-generated ${results.length} conflict-free schedule slots!`, 'success');
        autoModal.hide();
        // Update dashboard filter date and reload
        document.getElementById('filter-date').value = targetDate;
        loadSchedules();
    } catch (error) {
        showToast('Scheduling Failed', error.message || 'Insufficient available drivers or buses at terminal depot.', 'danger');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'Run Algorithm';
    }
}

async function cancelSchedule(id) {
    if (!confirm('Are you sure you want to cancel this schedule? This will set its status and corresponding trip to CANCELLED.')) return;

    try {
        await apiRequest(`/api/schedules/${id}`, {
            method: 'DELETE'
        });
        showToast('Schedule Cancelled', 'Slot cancelled successfully.', 'success');
        loadSchedules();
    } catch (error) {
        showToast('Failed', error.message || 'Unable to cancel schedule.', 'danger');
    }
}
