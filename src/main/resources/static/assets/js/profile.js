'use strict';

const roleNames = {
  STUDENT: 'Sinh viên',
  LECTURER: 'Giảng viên',
  DEAN: 'Trưởng khoa'
};

const $profile = (selector) => document.querySelector(selector);

function profileMessage(selector, message, error = false) {
  const element = $profile(selector);
  element.textContent = message;
  element.classList.toggle('error', error);
  element.hidden = !message;
}

function profileNavigation(role) {
  const home = role === 'STUDENT' ? '/student'
    : role === 'DEAN' ? '/admin/dashboard.html' : '/lecturer/dashboard.html';
  const studentLinks = [
    ['Bảng điều khiển', '/student'],
    ['Nhóm của tôi', '/student#group'],
    ['Kho đề tài', '/student#topics'],
    ['Đề tài đang thực hiện', '/student#project'],
    ['Nộp báo cáo', '/student#reports'],
    ['Kết quả đánh giá', '/student#results']
  ];
  const deanLinks = DEAN_NAVIGATION
    .filter(([key]) => key !== 'profile')
    .map(([, label, href]) => [label, href]);
  const lecturerLinks = [
    ['Bảng điều khiển', home],
    ['Tất cả đề tài', '/lecturer/topics/list.html'],
    ['Đề xuất đề tài', '/lecturer/topics/form.html'],
    ['Nhóm sinh viên hướng dẫn', '/lecturer/topics/student-groups.html'],
    ['Hội đồng & chấm điểm', '/councils/index.html']
  ];
  const links = role === 'STUDENT' ? studentLinks : role === 'DEAN' ? deanLinks : lecturerLinks;
  $profile('#brand-home').href = api(home);
  const nav = $profile('#profile-nav');
  nav.replaceChildren();
  for (const [label, href] of [...links, ['Hồ sơ cá nhân', '/profile.html']]) {
    const anchor = document.createElement('a');
    anchor.href = api(href);
    anchor.textContent = label;
    if (href === '/profile.html') {
      anchor.classList.add('active');
      anchor.setAttribute('aria-current', 'page');
    }
    nav.append(anchor);
  }
}

function renderProfile(profile) {
  const role = roleNames[profile.role] || profile.role;
  $profile('#header-name').textContent = profile.fullName;
  $profile('#header-role').textContent = role;
  const parts = profile.fullName.trim().split(/\s+/);
  $profile('#header-avatar').textContent = (parts[0][0] + (parts.length > 1 ? parts[parts.length - 1][0] : '')).toUpperCase();
  $profile('#profile-email').value = profile.email;
  $profile('#profile-role').value = role;
  $profile('#profile-full-name').value = profile.fullName;
  $profile('#profile-code').value = profile.userCode;
  $profile('#code-field').firstChild.textContent = profile.role === 'STUDENT' ? 'MSSV' : 'Mã cán bộ';
  $profile('#department-field').hidden = profile.role === 'STUDENT' || !profile.departmentName;
  $profile('#profile-department').value = profile.departmentName || '';
}

document.addEventListener('DOMContentLoaded', async () => {
  const menuToggle = $profile('[data-menu-toggle]');
  const sidebar = $profile('.sidebar');
  menuToggle.addEventListener('click', () => sidebar.classList.toggle('open'));
  document.addEventListener('click', event => {
    if (sidebar.classList.contains('open') && !sidebar.contains(event.target) && !menuToggle.contains(event.target)) sidebar.classList.remove('open');
  });

  try {
    const profile = await request('/api/profile');
    profileNavigation(profile.role);
    renderProfile(profile);
  } catch (error) {
    profileMessage('#profile-message', error.message, true);
    $profile('#profile-fields').hidden = true;
    $profile('#password-form').hidden = true;
    return;
  }

  $profile('#password-form').addEventListener('submit', async event => {
    event.preventDefault();
    const button = event.currentTarget.querySelector('button[type="submit"]');
    const currentPassword = $profile('#current-password').value;
    const newPassword = $profile('#new-password').value;
    const confirmPassword = $profile('#confirm-password').value;
    profileMessage('#password-message', '');
    if (newPassword !== confirmPassword) {
      profileMessage('#password-message', 'Xác nhận mật khẩu mới không khớp.', true);
      return;
    }
    button.disabled = true;
    try {
      await request('/api/profile/password', {
        method: 'POST',
        body: JSON.stringify({currentPassword, newPassword, confirmPassword})
      });
      event.currentTarget.reset();
      profileMessage('#password-message', 'Đổi mật khẩu thành công. Đang chuyển đến trang đăng nhập...');
      setTimeout(() => { location.href = api('/login.html'); }, 1800);
    } catch (error) {
      profileMessage('#password-message', error.message, true);
      button.disabled = false;
    }
  });
});
