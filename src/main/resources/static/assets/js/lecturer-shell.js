document.addEventListener('DOMContentLoaded', async () => {
    const user = await App.getCurrentUser();
    const navigation = document.querySelector('.sidebar-nav ul');
    if (navigation) {
        const item = document.createElement('li');
        const link = document.createElement('a');
        link.href='/councils/index.html';link.textContent='Hội đồng & chấm điểm';
        item.append(link);navigation.append(item);
    }
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

    if (document.querySelector('#stat-my-topics')) {
        try {
            const response=await fetch('/api/lecturer/announcements');
            if (!response.ok) throw new Error('Không thể tải thông báo');
            const news=await response.json();
            const panel=document.createElement('section');panel.className='card';
            panel.innerHTML='<h2>Thông báo từ khoa</h2>'+(news.length?news.map(a=>'<h3>'+escapeHtml(a.title)+'</h3><p style="white-space:pre-wrap">'+escapeHtml(a.content)+'</p>').join(''):'<p>Chưa có thông báo mới.</p>');
            document.querySelector('.content-body').append(panel);
        } catch(error) { console.error(error); }
    }
    const logoutBtn = document.getElementById('shell-logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            App.logout();
        });
    }
});
