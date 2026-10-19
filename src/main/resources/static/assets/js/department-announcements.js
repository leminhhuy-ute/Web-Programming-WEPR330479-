document.addEventListener('DOMContentLoaded', async () => {
  const message = document.querySelector('#announcement-message');
  const form = document.querySelector('#department-announcement-form');
  try {
    const user = await App.getCurrentUser();
    if (user.role !== 'DEAN') {
      form.hidden = true;
      message.textContent = 'Chỉ Trưởng khoa được gửi thông báo tại trang này.';
      message.hidden = false;
      return;
    }
  } catch (error) {
    message.textContent = error.message;
    message.hidden = false;
    return;
  }
  form.addEventListener('submit', async event => {
    event.preventDefault();
    const button = form.querySelector('button[type="submit"]');
    button.disabled = true;
    message.hidden = true;
    try {
      await request('/api/notifications/department', {
        method: 'POST', body: JSON.stringify({
          title: form.elements.title.value,
          content: form.elements.content.value,
          audience: 'STUDENT'
        })
      });
      form.reset();
      message.textContent = 'Đã gửi thông báo cho sinh viên thuộc bộ môn.';
      message.hidden = false;
    } catch (error) {
      message.textContent = error.message;
      message.hidden = false;
      void showAppMessage(error.message);
    } finally {
      button.disabled = false;
    }
  });
});
