// DTC Reports Download Center Client Logic

document.addEventListener('DOMContentLoaded', () => {
    // Set default date for operations sheet
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('ops-date').value = today;
});

async function downloadReport(module, format) {
    showToast('Download Started', `Preparing your ${module} report in ${format.toUpperCase()} format...`, 'info');
    
    try {
        const blob = await apiRequest(`/api/reports/${module}/${format}`, {
            method: 'GET',
            responseType: 'blob'
        });
        
        const extension = format === 'excel' ? 'xlsx' : 'pdf';
        const filename = `${module}_report_${new Date().toISOString().split('T')[0]}.${extension}`;
        
        triggerDownload(blob, filename);
        showToast('Download Complete', 'Report successfully saved.', 'success');
    } catch (error) {
        showToast('Download Failed', 'Unable to generate report files.', 'danger');
    }
}

async function downloadOperationsReport(format) {
    const targetDate = document.getElementById('ops-date').value;
    if (!targetDate) {
        showToast('Validation Error', 'Please select a date for the operations sheet.', 'danger');
        return;
    }
    
    showToast('Download Started', `Preparing Operations Sheet for ${targetDate}...`, 'info');
    
    try {
        const blob = await apiRequest(`/api/reports/operations/${format}?date=${targetDate}`, {
            method: 'GET',
            responseType: 'blob'
        });
        
        const extension = format === 'excel' ? 'xlsx' : 'pdf';
        const filename = `daily_operations_${targetDate}.${extension}`;
        
        triggerDownload(blob, filename);
        showToast('Download Complete', 'Operations report successfully saved.', 'success');
    } catch (error) {
        showToast('Download Failed', 'Unable to generate operations sheet.', 'danger');
    }
}

function triggerDownload(blob, filename) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
}
