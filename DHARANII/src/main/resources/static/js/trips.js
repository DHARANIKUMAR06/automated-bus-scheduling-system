// DTC Live Operations & Dispatch Tracker Client Logic

let tripModal = null;

document.addEventListener('DOMContentLoaded', () => {
    tripModal = new bootstrap.Modal(document.getElementById('tripModal'));
    
    // Set default date to today
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('trip-filter-date').value = today;

    loadTrips();
    
    // Auto-refresh active operations list every 15 seconds
    setInterval(() => {
        const onlyLive = document.getElementById('only-live-checkbox').checked;
        if (onlyLive) loadTrips();
    }, 15000);

    document.getElementById('trip-form').addEventListener('submit', handleStatusSubmit);
});

async function loadTrips() {
    const dateVal = document.getElementById('trip-filter-date').value;
    const onlyLive = document.getElementById('only-live-checkbox').checked;
    
    const tbody = document.getElementById('trip-table-body');
    tbody.innerHTML = `<tr><td colspan="10" class="text-center py-4"><span class="spinner-border spinner-border-sm" role="status"></span> Loading live positions...</td></tr>`;

    try {
        let endpoint = `/api/trips?date=${dateVal}`;
        if (onlyLive) {
            endpoint = '/api/trips/live';
        }

        const trips = await apiRequest(endpoint);
        tbody.innerHTML = '';

        if (trips.length === 0) {
            tbody.innerHTML = `<tr><td colspan="10" class="text-center py-4 text-secondary">No active operations matching this filter.</td></tr>`;
            return;
        }

        trips.forEach(t => {
            const tr = document.createElement('tr');
            
            let statusBadge = 'badge-available'; // green
            if (t.status === 'SCHEDULED') statusBadge = 'badge-warning'; // yellow
            if (t.status === 'EN_ROUTE') statusBadge = 'badge-active'; // blue
            if (t.status === 'DELAYED') statusBadge = 'badge-delayed'; // orange
            if (t.status === 'CANCELLED') statusBadge = 'badge-outofservice'; // red
            if (t.status === 'COMPLETED') statusBadge = 'badge-completed';

            let delayHtml = `<span class="text-success fw-bold">On Time</span>`;
            if (t.delayMinutes > 0) {
                delayHtml = `<span class="text-danger fw-bold">+${t.delayMinutes} mins</span>`;
            }

            const formatTime = (timeStr) => timeStr ? timeStr.substring(0, 5) : 'N/A';
            const formatDateTime = (dtStr) => dtStr ? dtStr.split('T')[1].substring(0, 5) : '--:--';

            // Action Buttons based on Status
            let actionBtnHtml = '';
            if (t.status === 'SCHEDULED') {
                actionBtnHtml = `<button class="btn btn-sm btn-success me-1" onclick="startTrip(${t.id})"><i class="bi bi-play-fill"></i> Start</button>`;
            } else if (t.status === 'EN_ROUTE' || t.status === 'DELAYED') {
                actionBtnHtml = `
                    <button class="btn btn-sm btn-danger me-1" onclick="endTrip(${t.id})"><i class="bi bi-stop-fill"></i> End</button>
                    <button class="btn btn-sm btn-outline-primary" onclick="openStatusModal(${t.id})"><i class="bi bi-broadcast"></i> Report</button>
                `;
            } else {
                actionBtnHtml = `<span class="text-secondary small">Locked</span>`;
            }

            tr.innerHTML = `
                <td>#${t.id}</td>
                <td class="fw-bold text-primary">${t.routeNumber}</td>
                <td class="fw-bold">${t.busNumber}</td>
                <td>${t.driverName}</td>
                <td>${formatTime(t.scheduledDepartureTime)} / ${formatTime(t.scheduledArrivalTime)}</td>
                <td>${formatDateTime(t.actualDepartureTime)} / ${formatDateTime(t.actualArrivalTime)}</td>
                <td>${delayHtml}</td>
                <td class="text-secondary small">${t.currentStop || 'N/A'}</td>
                <td><span class="badge-status ${statusBadge}">${t.status.replace('_', ' ')}</span></td>
                <td>${actionBtnHtml}</td>
            `;
            tbody.appendChild(tr);
        });

    } catch (error) {
        showToast('Load Error', 'Unable to fetch operational trips list.', 'danger');
        tbody.innerHTML = `<tr><td colspan="10" class="text-center py-4 text-danger">Error loading data.</td></tr>`;
    }
}

async function startTrip(id) {
    try {
        await apiRequest(`/api/trips/${id}/start`, {
            method: 'PUT'
        });
        showToast('Trip Started', 'Vehicle status updated to running.', 'success');
        loadTrips();
    } catch (error) {
        showToast('Operation Failed', error.message || 'Unable to start trip.', 'danger');
    }
}

async function endTrip(id) {
    try {
        await apiRequest(`/api/trips/${id}/end`, {
            method: 'PUT'
        });
        showToast('Trip Completed', 'Bus returned and marked available.', 'success');
        loadTrips();
    } catch (error) {
        showToast('Operation Failed', error.message || 'Unable to complete trip.', 'danger');
    }
}

async function openStatusModal(id) {
    try {
        const trip = await apiRequest(`/api/trips/${id}`);
        document.getElementById('trip-id').value = trip.id;
        document.getElementById('trip-status').value = trip.status;
        document.getElementById('trip-delay').value = trip.delayMinutes;
        document.getElementById('trip-stop').value = trip.currentStop || '';
        document.getElementById('trip-notes').value = trip.notes || '';
        
        tripModal.show();
    } catch (error) {
        showToast('Error', 'Unable to fetch live status.', 'danger');
    }
}

async function handleStatusSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('trip-id').value;
    const status = document.getElementById('trip-status').value;
    const delay = document.getElementById('trip-delay').value;
    const stop = document.getElementById('trip-stop').value;
    const notes = document.getElementById('trip-notes').value;

    try {
        await apiRequest(`/api/trips/${id}/status?status=${status}&delayMinutes=${delay}&currentStop=${encodeURIComponent(stop)}&notes=${encodeURIComponent(notes)}`, {
            method: 'PUT'
        });
        
        showToast('Position Reported', 'Dispatch desk synchronized.', 'success');
        tripModal.hide();
        loadTrips();
    } catch (error) {
        showToast('Failed', error.message || 'Unable to sync position.', 'danger');
    }
}
