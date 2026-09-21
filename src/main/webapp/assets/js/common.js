const App = {
    getContextPath() {
        const path = window.location.pathname;
        return path.substring(0, path.indexOf('/', 1)) || '';
    },

    async fetchApi(url, options = {}) {
        options.credentials = 'same-origin';
        options.headers = options.headers || {};
        if (!options.headers['Content-Type'] && !(options.body instanceof FormData)) {
            options.headers['Content-Type'] = 'application/json';
        }

        // Add CSRF token if method is POST, PUT, DELETE
        const method = (options.method || 'GET').toUpperCase();
        if (['POST', 'PUT', 'DELETE'].includes(method)) {
            const csrfToken = this.getCookie('XSRF-TOKEN');
            if (csrfToken) {
                options.headers['X-XSRF-TOKEN'] = csrfToken;
            }
        }

        const response = await fetch(url, options);
        if (response.status === 401) {
            window.location.href = this.getContextPath() + '/login.html';
            throw new Error('Chưa đăng nhập');
        }

        const data = await response.json();
        if (!response.ok || !data.success) {
            throw new Error(data.message || 'Đã có lỗi xảy ra');
        }
        return data;
    },

    getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
        return null;
    },

    async getCurrentUser() {
        if (this._currentUser) return this._currentUser;
        try {
            const res = await this.fetchApi(this.getContextPath() + '/api/auth/me');
            this._currentUser = res.data;
            return this._currentUser;
        } catch (e) {
            return null;
        }
    },

    async logout() {
        try {
            await this.fetchApi(this.getContextPath() + '/api/auth/logout', { method: 'POST' });
        } finally {
            window.location.href = this.getContextPath() + '/login.html';
        }
    },

    formatDate(dateString) {
        if (!dateString) return '—';
        const d = new Date(dateString);
        return d.toLocaleDateString('vi-VN') + ' ' + d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    }
};
