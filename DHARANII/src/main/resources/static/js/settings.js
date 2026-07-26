// DTC Settings & Admin Utilities Client Logic

document.addEventListener('DOMContentLoaded', () => {
    const user = getLoggedUser();
    
    // Check if user is Admin to show database panels
    if (user && user.role === 'ROLE_ADMIN') {
        const backupPanel = document.getElementById('admin-backup-panel');
        const auditPanel = document.getElementById('admin-audit-panel');
        
        if (backupPanel) backupPanel.classList.remove('d-none');
        if (auditPanel) auditPanel.classList.remove('d-none');
        
        loadAuditLogs();
    }
});

async function loadAuditLogs() {
    const tbody = document.getElementById('audit-table-body');
    if (!tbody) return;
    tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4"><span class="spinner-border spinner-border-sm" role="status"></span> Loading logs...</td></tr>`;

    try {
        const logs = await apiRequest('/api/audit-logs');
        tbody.innerHTML = '';

        if (logs.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-secondary">No audit logs found.</td></tr>`;
            return;
        }

        logs.forEach(log => {
            const tr = document.createElement('tr');
            
            // Format timestamp nicely
            const ts = new Date(log.timestamp).toLocaleString('en-IN');
            
            let actionBadge = 'bg-primary';
            if (log.action.includes('DELETE') || log.action.includes('CANCEL')) actionBadge = 'bg-danger';
            if (log.action.includes('SUCCESS') || log.action.includes('CREATE')) actionBadge = 'bg-success';
            if (log.action.includes('BACKUP') || log.action.includes('RESTORE')) actionBadge = 'bg-info';

            tr.innerHTML = `
                <td class="small text-secondary">${ts}</td>
                <td class="fw-bold">${log.username}</td>
                <td><span class="badge ${actionBadge}">${log.action}</span></td>
                <td class="small text-secondary">${log.ipAddress || '127.0.0.1'}</td>
                <td class="small" style="max-width: 300px;">${log.details || ''}</td>
            `;
            tbody.appendChild(tr);
        });

    } catch (error) {
        showToast('Load Error', 'Unable to fetch audit logs.', 'danger');
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-danger">Error loading data.</td></tr>`;
    }
}

async function downloadDatabaseBackup() {
    showToast('Backup In Progress', 'Creating SQL archive data...', 'info');
    
    try {
        const response = await fetch(`${window.location.origin}/api/backup/download`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('dtc_jwt_token')}`
            }
        });
        
        if (!response.ok) throw new Error('Backup request failed');
        
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `dtc_db_backup_${new Date().toISOString().split('T')[0]}.sql`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
        
        showToast('Backup Completed', 'SQL backup file downloaded.', 'success');
        loadAuditLogs();
    } catch (error) {
        showToast('Backup Failed', 'Unable to dump MySQL tables.', 'danger');
    }
}

function restoreDatabaseBackup(event) {
    const file = event.target.files[0];
    if (!file) return;
    
    const reader = new FileReader();
    reader.onload = async (e) => {
        const sqlContent = e.target.result;
        showToast('Restore In Progress', 'Parsing backup script...', 'info');
        
        try {
            await apiRequest('/api/backup/restore', {
                method: 'POST',
                headers: {
                    'Content-Type': 'text/plain'
                },
                body: sqlContent
            });
            
            showToast('Restore Successful', 'Database records synchronized.', 'success');
            document.getElementById('restore-file').value = '';
            loadAuditLogs();
        } catch (error) {
            showToast('Restore Failed', error.message || 'Syntax error in SQL restoration script.', 'danger');
        }
    };
    reader.readAsText(file);
}
