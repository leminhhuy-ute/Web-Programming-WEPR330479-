(function () {
  'use strict';

  const eye = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>';
  const eyeOff = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m3 3 18 18"/><path d="M10.6 10.6a2 2 0 0 0 2.8 2.8"/><path d="M9.9 5.2A10.8 10.8 0 0 1 12 5c6.4 0 10 7 10 7a16 16 0 0 1-3.1 3.9"/><path d="M6.2 6.2C3.5 8.1 2 12 2 12s3.6 7 10 7a10.4 10.4 0 0 0 3.1-.5"/></svg>';

  document.querySelectorAll('input[type="password"]').forEach((input, index) => {
    let wrapper = input.parentElement;
    if (!wrapper.classList.contains('input-icon-wrap')) {
      wrapper = document.createElement('div');
      wrapper.className = 'password-toggle-wrap';
      input.before(wrapper);
      wrapper.append(input);
    } else {
      wrapper.classList.add('password-toggle-wrap');
    }

    if (!input.id) input.id = `password-field-${index + 1}`;

    const toggle = document.createElement('button');
    toggle.type = 'button';
    toggle.className = 'password-visibility-toggle';
    toggle.setAttribute('aria-controls', input.id);
    toggle.setAttribute('aria-label', 'Hiện mật khẩu');
    toggle.setAttribute('aria-pressed', 'false');
    toggle.title = 'Hiện mật khẩu';
    toggle.innerHTML = eye;
    toggle.addEventListener('click', () => {
      const visible = input.type === 'password';
      input.type = visible ? 'text' : 'password';
      toggle.setAttribute('aria-label', visible ? 'Ẩn mật khẩu' : 'Hiện mật khẩu');
      toggle.setAttribute('aria-pressed', String(visible));
      toggle.title = visible ? 'Ẩn mật khẩu' : 'Hiện mật khẩu';
      toggle.innerHTML = visible ? eyeOff : eye;
      input.focus();
    });
    wrapper.append(toggle);
  });
})();
