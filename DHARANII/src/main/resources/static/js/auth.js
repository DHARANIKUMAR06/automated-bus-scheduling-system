// DTC Authentication & User Session Operations

// Global helpers for Login/Register tabs and quick fill
function switchAuthTab(tab) {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const loginBtn = document.getElementById('tab-login-btn');
    const registerBtn = document.getElementById('tab-register-btn');

    if (tab === 'login') {
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
        loginBtn.classList.add('active');
        registerBtn.classList.remove('active');
    } else {
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
        loginBtn.classList.remove('active');
        registerBtn.classList.add('active');
    }
}

function fillCredentials(username, password) {
    switchAuthTab('login');
    const userField = document.getElementById('username');
    const passField = document.getElementById('password');
    if (userField && passField) {
        userField.value = username;
        passField.value = password;
        showToast('Credentials Loaded', `Loaded credentials for user '${username}'`, 'info');
    }
}

function togglePasswordVisibility(inputId, el) {
    const input = document.getElementById(inputId);
    if (!input) return;
    const icon = el.querySelector('i');
    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'bi bi-eye-slash-fill';
    } else {
        input.type = 'password';
        icon.className = 'bi bi-eye-fill';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // Handle Login Form Submission
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const usernameInput = document.getElementById('username');
            const passwordInput = document.getElementById('password');
            
            const submitBtn = loginForm.querySelector('button[type="submit"]');
            const originalBtnText = submitBtn.innerHTML;
            submitBtn.disabled = true;
            submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Authenticating...`;
            
            try {
                const response = await apiRequest('/api/auth/login', {
                    method: 'POST',
                    body: JSON.stringify({
                        username: usernameInput.value.trim(),
                        password: passwordInput.value
                    })
                });
                
                // Store JWT and Profile data
                localStorage.setItem('dtc_jwt_token', response.token);
                localStorage.setItem('dtc_user', JSON.stringify({
                    id: response.id,
                    username: response.username,
                    email: response.email,
                    role: response.role
                }));
                
                showToast('Login Successful', `Welcome back, ${response.username}!`, 'success');
                
                setTimeout(() => {
                    window.location.href = '/dashboard.html';
                }, 800);
            } catch (error) {
                showToast('Authentication Failed', error.message || 'Invalid username or password.', 'danger');
                passwordInput.value = '';
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalBtnText;
            }
        });
    }

    // Handle Registration Form Submission
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const fullName = document.getElementById('reg-fullName').value.trim();
            const username = document.getElementById('reg-username').value.trim();
            const email = document.getElementById('reg-email').value.trim();
            const phone = document.getElementById('reg-phone').value.trim();
            const role = document.getElementById('reg-role').value;
            const password = document.getElementById('reg-password').value;
            const confirmPassword = document.getElementById('reg-confirmPassword').value;

            if (password !== confirmPassword) {
                showToast('Validation Error', 'Passwords do not match.', 'danger');
                return;
            }

            if (password.length < 6) {
                showToast('Validation Error', 'Password must be at least 6 characters.', 'danger');
                return;
            }

            const submitBtn = registerForm.querySelector('button[type="submit"]');
            const originalBtnText = submitBtn.innerHTML;
            submitBtn.disabled = true;
            submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Creating Account...`;

            try {
                const response = await apiRequest('/api/auth/register', {
                    method: 'POST',
                    body: JSON.stringify({
                        fullName,
                        username,
                        email,
                        phone,
                        role,
                        password
                    })
                });

                // Store JWT and Profile data
                localStorage.setItem('dtc_jwt_token', response.token);
                localStorage.setItem('dtc_user', JSON.stringify({
                    id: response.id,
                    username: response.username,
                    email: response.email,
                    role: response.role
                }));

                showToast('Account Created!', `Welcome, ${response.username}! Account created & signed in.`, 'success');

                setTimeout(() => {
                    window.location.href = '/dashboard.html';
                }, 1000);
            } catch (error) {
                showToast('Registration Failed', error.message || 'Unable to complete registration.', 'danger');
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalBtnText;
            }
        });
    }

    // Handle Change Password Form Submission (on Settings Page)
    const changePasswordForm = document.getElementById('change-password-form');
    if (changePasswordForm) {
        changePasswordForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const oldPassword = document.getElementById('old-password');
            const newPassword = document.getElementById('new-password');
            const confirmPassword = document.getElementById('confirm-password');

            if (newPassword.value !== confirmPassword.value) {
                showToast('Validation Error', 'New passwords do not match.', 'danger');
                return;
            }

            try {
                await apiRequest('/api/auth/change-password', {
                    method: 'POST',
                    body: JSON.stringify({
                        oldPassword: oldPassword.value,
                        newPassword: newPassword.value
                    })
                });

                showToast('Success', 'Password updated successfully!', 'success');
                changePasswordForm.reset();
            } catch (error) {
                showToast('Update Failed', error.message || 'Unable to update password.', 'danger');
            }
        });
    }
});
