(() => {
  if (window.__notificationBellLoaded) return;
  window.__notificationBellLoaded = true;

  const escape = value => String(value ?? '').replace(/[&<>"']/g,
    char => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char]));
  const stylesheet = document.createElement('link');
  stylesheet.rel = 'stylesheet';
  stylesheet.href = '/assets/css/notifications.css';
  document.head.append(stylesheet);

  function csrf() {
    const metaHeader = document.querySelector('meta[name="_csrf_header"]');
    const metaToken = document.querySelector('meta[name="_csrf"]');
    if (metaHeader && metaToken) return {[metaHeader.content]: metaToken.content};
    const token = document.cookie.split('; ').find(part => part.startsWith('XSRF-TOKEN='));
    return token ? {'X-XSRF-TOKEN': decodeURIComponent(token.slice('XSRF-TOKEN='.length))} : {};
  }

  function mount() {
    const topbar = document.querySelector('.topbar-right');
    if (!topbar || topbar.querySelector('.notification-bell')) return;
    const holder = document.createElement('div');
    holder.className = 'notification-bell';
    holder.innerHTML = `<button type="button" class="notification-trigger" aria-label="Mở thông báo" aria-expanded="false" aria-controls="notification-popover">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9ZM10 21h4"/></svg>
      <span class="notification-count" hidden></span></button>
      <section id="notification-popover" class="notification-popover" aria-label="Thông báo" hidden>
        <div class="notification-heading"><strong>Thông báo</strong><span class="notification-unread-label"></span></div>
        <div class="notification-items" role="list"><p class="notification-empty">Đang tải thông báo…</p></div>
      </section>`;
    topbar.prepend(holder);
    const button = holder.querySelector('.notification-trigger');
    const panel = holder.querySelector('.notification-popover');
    button.addEventListener('click', () => {
      panel.hidden = !panel.hidden;
      button.setAttribute('aria-expanded', String(!panel.hidden));
      if (!panel.hidden) void refresh();
    });
    document.addEventListener('click', event => {
      if (!holder.contains(event.target)) {
        panel.hidden = true;
        button.setAttribute('aria-expanded', 'false');
      }
    });
    holder.addEventListener('click', async event => {
      const item = event.target.closest('[data-announcement-id]');
      if (!item) return;
      try {
        const response = await fetch(`/api/notifications/${item.dataset.announcementId}/read`, {
          method: 'POST', credentials: 'same-origin', headers: csrf()
        });
        if (!response.ok) throw new Error('Không thể đánh dấu đã đọc.');
        await refresh();
      } catch (error) {
        holder.querySelector('.notification-items').insertAdjacentHTML('afterbegin',
          `<p class="notification-error" role="alert">${escape(error.message)}</p>`);
      }
    });
  }

  let lastSignature = null;
  let loading = false;
  async function refresh() {
    if (loading) return;
    const holder = document.querySelector('.notification-bell');
    if (!holder) return;
    loading = true;
    try {
      const response = await fetch('/api/notifications', {credentials: 'same-origin', cache: 'no-store'});
      if (!response.ok) throw new Error('Không thể tải thông báo.');
      const payload = await response.json();
      const feed = payload.data;
      const unread = Number(feed.unreadCount || 0);
      const badge = holder.querySelector('.notification-count');
      badge.hidden = unread === 0;
      badge.textContent = unread > 99 ? '99+' : String(unread);
      holder.querySelector('.notification-unread-label').textContent = unread ? `${unread} chưa đọc` : 'Đã đọc hết';
      holder.querySelector('.notification-items').innerHTML = feed.items.length
        ? feed.items.map(item => `<button type="button" role="listitem" data-announcement-id="${item.id}" class="notification-item ${item.read ? '' : 'unread'}">
            <span class="notification-item-title">${escape(item.title)}</span>
            <span class="notification-item-content">${escape(item.content)}</span>
            <small>${escape(item.sender)}${item.createdAt ? ` · ${new Date(item.createdAt).toLocaleString('vi-VN')}` : ''}</small>
          </button>`).join('')
        : '<p class="notification-empty">Chưa có thông báo.</p>';
      const signature = feed.items.map(item => `${item.id}:${item.title}:${item.content}`).join('|');
      if (lastSignature !== null && lastSignature !== signature)
        document.dispatchEvent(new CustomEvent('announcements:updated'));
      lastSignature = signature;
    } catch (error) {
      if (!holder.querySelector('.notification-item'))
        holder.querySelector('.notification-items').innerHTML = `<p class="notification-error" role="alert">${escape(error.message)}</p>`;
    } finally {
      loading = false;
    }
  }

  function init() {
    mount();
    void refresh();
    window.setInterval(() => { if (!document.hidden) void refresh(); }, 30000);
    document.addEventListener('visibilitychange', () => { if (!document.hidden) void refresh(); });
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
  else init();
})();
