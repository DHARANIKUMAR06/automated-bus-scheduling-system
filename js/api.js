// DTC Bus Scheduling System API Wrapper & Utilities

const API_BASE_URL = (window.location.hostname.endsWith('github.io'))
    ? 'https://9864d29122e05b.lhr.life'
    : window.location.origin;

// Retrieve authentication token
function getAuthToken() {
    return localStorage.getItem('dtc_jwt_token');
}

// Retrieve current logged in user details
function getLoggedUser() {
    const userStr = localStorage.getItem('dtc_user');
    return userStr ? JSON.parse(userStr) : null;
}

// Check if user is logged in, if not redirect to login page
function checkAuthentication() {
    const token = getAuthToken();
    if (!token && !window.location.pathname.endsWith('login.html')) {
        window.location.href = '/login.html';
    }
    return token;
}

// Unified API Request Wrapper
async function apiRequest(endpoint, options = {}) {
    const token = getAuthToken();
    
    // Set headers
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    const config = {
        ...options,
        headers
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
        
        // Auto logout if token expires/invalid
        if (response.status === 401) {
            localStorage.removeItem('dtc_jwt_token');
            localStorage.removeItem('dtc_user');
            if (!window.location.pathname.endsWith('login.html')) {
                showToast('Session Expired', 'Please login again.', 'danger');
                setTimeout(() => {
                    window.location.href = '/login.html';
                }, 1500);
            }
            throw new Error('Unauthorized');
        }
        
        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.message || `Request failed with status ${response.status}`);
        }
        
        // For report downloads that return binary blobs
        if (options.responseType === 'blob') {
            return await response.blob();
        }
        
        // Handle empty success responses
        if (response.status === 204) {
            return null;
        }
        
        return await response.json();
    } catch (error) {
        console.error(`API Error on ${endpoint}:`, error);
        throw error;
    }
}

// Elegant Dynamic Javascript Toast Notification System
function showToast(title, message, type = 'success') {
    // Create toast container if not exists
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    
    const toast = document.createElement('div');
    toast.className = `glass-panel animated-fade p-3 mb-2 d-flex align-items-center gap-3`;
    toast.style.width = '350px';
    toast.style.background = 'var(--bg-secondary)';
    toast.style.borderLeft = `5px solid var(--color-${type})`;
    toast.style.boxShadow = '0 10px 25px rgba(0,0,0,0.15)';
    
    let iconClass = 'bi-check-circle-fill';
    if (type === 'danger') iconClass = 'bi-exclamation-triangle-fill';
    if (type === 'warning') iconClass = 'bi-exclamation-circle-fill';
    if (type === 'info') iconClass = 'bi-info-circle-fill';
    
    toast.innerHTML = `
        <i class="bi ${iconClass}" style="font-size: 1.4rem; color: var(--color-${type})"></i>
        <div style="flex: 1">
            <h6 class="m-0 fw-bold" style="font-size: 0.9rem">${title}</h6>
            <p class="m-0 text-secondary" style="font-size: 0.8rem">${message}</p>
        </div>
        <button class="btn-close" style="font-size: 0.7rem" onclick="this.parentElement.remove()"></button>
    `;
    
    container.appendChild(toast);
    
    // Auto-remove after 4 seconds
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-10px)';
        toast.style.transition = 'all 0.3s';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Global UI Setup helper (Dark Mode state, Profile info, Sidebar highlights)
document.addEventListener('DOMContentLoaded', () => {
    // 1. Enforce Authentication
    const isLogin = window.location.pathname.endsWith('login.html');
    if (!isLogin) {
        const token = checkAuthentication();
        if (!token) return;
    }
    
    // 2. Configure Theme (Light/Dark Toggle)
    const activeTheme = localStorage.getItem('dtc_theme') || 'light';
    document.documentElement.setAttribute('data-theme', activeTheme);
    
    const themeBtn = document.getElementById('theme-toggle-btn');
    if (themeBtn) {
        themeBtn.innerHTML = activeTheme === 'dark' ? '<i class="bi bi-sun-fill"></i>' : '<i class="bi bi-moon-fill"></i>';
        themeBtn.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-theme');
            const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('dtc_theme', newTheme);
            themeBtn.innerHTML = newTheme === 'dark' ? '<i class="bi bi-sun-fill"></i>' : '<i class="bi bi-moon-fill"></i>';
        });
    }
    
    // 3. Inject Profile details into Sidebar
    const user = getLoggedUser();
    if (user && document.getElementById('profile-user-name')) {
        document.getElementById('profile-user-name').innerText = user.username;
        document.getElementById('profile-user-role').innerText = user.role.replace('ROLE_', '');
        document.getElementById('profile-avatar-letter').innerText = user.username.charAt(0).toUpperCase();
    }
    
    // 4. Highlight active sidebar link
    const path = window.location.pathname;
    const links = document.querySelectorAll('.sidebar-menu li');
    links.forEach(li => {
        const a = li.querySelector('a');
        if (a && path.includes(a.getAttribute('href'))) {
            li.className = 'active';
        } else {
            li.classList.remove('active');
        }
    });
});

// Logout implementation
function logoutUser() {
    localStorage.removeItem('dtc_jwt_token');
    localStorage.removeItem('dtc_user');
    showToast('Logged Out', 'Redirecting to login page...', 'info');
    setTimeout(() => {
        window.location.href = '/login.html';
    }, 1000);
}
