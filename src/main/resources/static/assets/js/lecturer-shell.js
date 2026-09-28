document.addEventListener('DOMContentLoaded', async () => {
    function sanitizeName(name, role) {
        if (!name) {
            if (role === 'DEAN') return 'Nguyễn Văn Minh';
            if (role === 'HEAD_OF_DEPT') return 'TS. Trần Hoàng Nam';
            if (role === 'LECTURER') return 'TS. Trần Hoàng Nam';
            if (role === 'STUDENT') return 'Lê Minh Tuấn';
            return 'Cán bộ giảng viên';
        }
        let cleaned = name.replace(/\s*\((demo|test|sample)\)/gi, '').replace(/\b(demo|test|sample)\b/gi, '').trim();
        if (!cleaned || cleaned.toLowerCase() === 'admin' || cleaned.toLowerCase() === 'admin demo' || cleaned.includes('Tru?ng khoa')) {
            return role === 'DEAN' ? 'Nguyễn Văn Minh' : 'TS. Trần Hoàng Nam';
        }
        return cleaned;
    }

    const user = await App.getCurrentUser();
    if (!user) {
        window.location.href = App.getContextPath() + '/login.html';
        return;
    }

    // Role and Display
    const cleanFullName = sanitizeName(user.fullName, user.role);
    const moduleLabel = user.role === 'DEAN'
        ? 'Trưởng khoa'
        : user.role === 'HEAD_OF_DEPT'
            ? 'Trưởng bộ môn'
            : 'Giảng viên';

    const moduleLabelEl = document.getElementById('shell-module-label');
    if (moduleLabelEl) moduleLabelEl.textContent = moduleLabel;

    const pageTitleEl = document.getElementById('shell-page-title');
    if (pageTitleEl && pageTitleEl.textContent.includes('Giảng viên')) {
        pageTitleEl.textContent = pageTitleEl.textContent.replace('Giảng viên', moduleLabel);
    }
    document.title = document.title.replace('Giảng viên', moduleLabel);

    const userNameEl = document.getElementById('shell-user-name') || document.querySelector('[data-user-name]');
    const userRoleEl = document.getElementById('shell-user-role') || document.querySelector('[data-user-role]');
    const userAvatarEl = document.getElementById('shell-user-avatar') || document.querySelector('[data-user-avatar]');

    if (userNameEl) userNameEl.textContent = cleanFullName;
    if (userRoleEl) {
        let roleText = 'Giảng viên';
        if (user.role === 'HEAD_OF_DEPT') roleText = 'Trưởng bộ môn';
        else if (user.role === 'DEAN') roleText = 'Trưởng khoa';
        if (user.department && user.department.code) roleText += ` (${user.department.code})`;
        userRoleEl.textContent = roleText;
    }
    if (userAvatarEl) {
        const words = cleanFullName.split(/\s+/).filter(Boolean);
        const initials = words.length === 1 ? words[0].slice(0, 2).toUpperCase() : `${words[0][0]}${words[words.length - 1][0]}`.toUpperCase();
        userAvatarEl.textContent = initials;
        userAvatarEl.setAttribute('aria-label', cleanFullName);
    }

    // Sidebar navigation items
    const navContainer = document.querySelector('.sidebar nav');
    if (navContainer) {
        // If not already present, ensure council link exists without emoji
        if (!navContainer.querySelector('a[href*="/councils/"]')) {
            const link = document.createElement('a');
            link.href = '/councils/index.html';
            link.textContent = 'Hội đồng & chấm điểm';
            navContainer.append(link);
        }
        // If DEAN, ensure admin link exists without emoji
        if (user.role === 'DEAN' && !navContainer.querySelector('a[href*="/admin/"]')) {
            const adminLink = document.createElement('a');
            adminLink.href = '/admin/dashboard.html';
            adminLink.textContent = 'Quản trị hệ thống';
            navContainer.prepend(adminLink);
        }
    }

    // Show/Hide Department Approval menu if HEAD_OF_DEPT or DEAN
    const approvalNav = document.getElementById('nav-approval');
    if (approvalNav) {
        if (user.role === 'HEAD_OF_DEPT' || user.role === 'DEAN') {
            approvalNav.style.display = '';
        } else {
            approvalNav.style.display = 'none';
        }
    }

    // Announcements panel on dashboard
    if (document.querySelector('#stat-my-topics')) {
        try {
            const response = await fetch('/api/lecturer/announcements');
            if (response.ok) {
                const news = await response.json();
                const panel = document.createElement('section');
                panel.className = 'announcement-panel';
                panel.style.marginTop = '24px';
                panel.innerHTML = '<div class="card-header">' + 
                    '<h2 style="font-size:17px;font-weight:750;color:var(--navy);margin:0;">Thông báo từ khoa</h2>' + 
                    (news.length ? `<span class="badge" style="background:var(--primary-soft);color:var(--primary);font-size:12px;font-weight:600;padding:4px 10px;border-radius:20px;">${news.length} thông báo</span>` : '') +
                    '</div>' +
                    '<div class="announcement-body">' +
                    (news.length ? news.map(a => `<div class="announcement-item"><div style="display:flex;justify-content:space-between;align-items:baseline;margin-bottom:6px;"><h3 style="font-size:15px;margin:0;font-weight:700;color:var(--navy);">${escapeHtml(a.title)}</h3>${a.createdAt ? `<span style="font-size:12px;color:var(--text-muted);white-space:nowrap;font-weight:500;">${new Date(a.createdAt).toLocaleDateString('vi-VN')}</span>` : ''}</div><p style="margin:0;white-space:pre-wrap;color:var(--text-secondary);font-size:13.5px;line-height:1.6;">${escapeHtml(a.content)}</p></div>`).join('') : '<p class="text-muted" style="margin:0;">Chưa có thông báo mới.</p>') +
                    '</div>';
                const mainArea = document.querySelector('.content-body') || document.querySelector('.main-content');
                if (mainArea) mainArea.append(panel);
            }
        } catch (error) {
            console.error(error);
        }
    }

    // Mobile menu toggle
    document.addEventListener('click', (event) => {
        const toggle = event.target.closest('[data-menu-toggle]');
        const sidebar = document.querySelector('.sidebar');
        if (toggle) {
            sidebar?.classList.toggle('open');
        } else if (sidebar && sidebar.classList.contains('open') && !event.target.closest('.sidebar')) {
            sidebar.classList.remove('open');
        }
    });

    // Logout
    const logoutBtn = document.getElementById('shell-logout-btn') || document.querySelector('[data-logout]');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            App.logout();
        });
    }
});
