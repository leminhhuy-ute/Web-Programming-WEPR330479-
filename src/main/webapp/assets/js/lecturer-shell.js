document.addEventListener('DOMContentLoaded', async () => {
    const user = await App.getCurrentUser();
    if (!user) {
        window.location.href = App.getContextPath() + '/login.html';
        return;
    }

    const userNameEl = document.getElementById('shell-user-name');
    const userRoleEl = document.getElementById('shell-user-role');
    const userAvatarEl = document.getElementById('shell-user-avatar');

    if (userNameEl) userNameEl.textContent = user.fullName;
    if (userRoleEl) {
        let roleText = 'Giảng viên';
        if (user.role === 'HEAD_OF_DEPT') roleText = 'Trưởng bộ môn';
        else if (user.role === 'DEAN') roleText = 'Trưởng khoa / Quản trị';
        if (user.department) roleText += ` (${user.department.code})`;
        userRoleEl.textContent = roleText;
    }
    if (userAvatarEl) {
        const initials = user.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
        userAvatarEl.textContent = initials;
    }

    // Show/Hide Department Approval menu if HEAD_OF_DEPT or DEAN
    const approvalNav = document.getElementById('nav-approval');
    if (approvalNav) {
        if (user.role === 'HEAD_OF_DEPT' || user.role === 'DEAN') {
            approvalNav.style.display = 'flex';
        } else {
            approvalNav.style.display = 'none';
        }
    }

    const logoutBtn = document.getElementById('shell-logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            App.logout();
        });
    }
});
