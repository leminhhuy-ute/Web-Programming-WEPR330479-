document.addEventListener('DOMContentLoaded', async () => {
    try {
        const statistics = await request('/api/admin/dashboard');
        document.querySelectorAll('[data-stat]').forEach(element => {
            element.textContent = statistics[element.dataset.stat] ?? '0';
        });
    } catch (error) {
        document.querySelectorAll('[data-stat]').forEach(element => element.textContent = '—');
        console.error('Không thể tải thống kê quản trị:', error);
    }
});
