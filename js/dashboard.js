// DTC Dashboard Logic

document.addEventListener('DOMContentLoaded', () => {
    loadDashboardData();
    
    // Auto-refresh dashboard stats every 30 seconds
    setInterval(loadDashboardData, 30000);
});

let fuelChartInstance = null;
let tripChartInstance = null;

async function loadDashboardData() {
    try {
        const stats = await apiRequest('/api/dashboard/stats');
        
        // Update DOM Counters
        document.getElementById('stat-total-buses').innerText = stats.totalBuses;
        document.getElementById('stat-active-buses').innerText = stats.activeBuses;
        document.getElementById('stat-total-drivers').innerText = stats.totalDrivers;
        document.getElementById('stat-today-trips').innerText = stats.todayTrips;
        document.getElementById('stat-completed-trips').innerText = stats.completedTrips;
        document.getElementById('stat-delayed-trips').innerText = stats.delayedTrips;
        document.getElementById('stat-total-conductors').innerText = stats.totalConductors;
        
        // Notification Badge Count
        const badge = document.getElementById('dashboard-notif-count');
        if (badge) {
            badge.innerText = stats.activeNotifications;
            badge.style.display = stats.activeNotifications > 0 ? 'flex' : 'none';
        }

        // Render Fuel Distribution Chart
        renderFuelChart(stats.busFuelTypeDistribution);

        // Render Trip Status Chart
        renderTripChart(stats.tripStatusDistribution);

    } catch (error) {
        showToast('Load Error', 'Unable to fetch dashboard metrics.', 'danger');
    }
}

function renderFuelChart(distribution) {
    const ctx = document.getElementById('fuelTypeChart').getContext('2d');
    const labels = Object.keys(distribution);
    const data = Object.values(distribution);
    
    if (fuelChartInstance) {
        fuelChartInstance.destroy();
    }
    
    fuelChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: [
                    '#3b82f6', // CNG - Blue
                    '#10b981', // ELECTRIC - Emerald Green
                    '#f59e0b'  // DIESEL - Amber
                ],
                borderWidth: 1,
                borderColor: 'var(--border-color)'
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: 'var(--text-primary)',
                        font: { family: 'Outfit', size: 12 }
                    }
                }
            },
            cutout: '65%'
        }
    });
}

function renderTripChart(distribution) {
    const ctx = document.getElementById('tripStatusChart').getContext('2d');
    const labels = Object.keys(distribution);
    const data = Object.values(distribution);

    if (tripChartInstance) {
        tripChartInstance.destroy();
    }

    tripChartInstance = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels.map(l => l.replace('_', ' ')),
            datasets: [{
                data: data,
                backgroundColor: [
                    '#60a5fa', // SCHEDULED
                    '#3b82f6', // EN_ROUTE
                    '#10b981', // COMPLETED
                    '#ef4444', // DELAYED
                    '#64748b'  // CANCELLED
                ],
                borderWidth: 1,
                borderColor: 'var(--border-color)'
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: 'var(--text-primary)',
                        font: { family: 'Outfit', size: 12 }
                    }
                }
            }
        }
    });
}
