document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    const messageEl = document.getElementById('message');

    if (loginForm) {
        // Fetch CSRF cookie on login page load
        App.fetchApi(App.getContextPath() + '/api/auth/csrf').catch(() => {});

        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            messageEl.hidden = true;

            const formData = new FormData(loginForm);
            const payload = {
                username: formData.get('username'),
                password: formData.get('password')
            };

            try {
                const res = await App.fetchApi(App.getContextPath() + '/api/auth/login', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });
                
                // Redirection based on role
                const user = res.data;
                if (user.role === 'LECTURER' || user.role === 'HEAD_OF_DEPT' || user.role === 'DEAN') {
                    window.location.href = App.getContextPath() + '/lecturer/dashboard.html';
                } else {
                    window.location.href = App.getContextPath() + '/index.html';
                }
            } catch (err) {
                messageEl.textContent = err.message;
                messageEl.hidden = false;
            }
        });
    }
});
