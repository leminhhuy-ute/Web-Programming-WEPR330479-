// Main Javascript Utilities for HCMUTE Topic Management System

function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'fixed bottom-5 right-5 z-50 flex flex-col space-y-2';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    const isSuccess = type === 'success';
    const bgColor = isSuccess ? 'bg-emerald-600' : 'bg-rose-600';
    const icon = isSuccess ? 'fa-circle-check' : 'fa-circle-exclamation';

    toast.className = `${bgColor} text-white px-4 py-3 rounded-xl shadow-xl flex items-center space-x-3 text-sm transition-all duration-300 transform translate-y-2 opacity-0`;
    toast.innerHTML = `
        <i class="fa-solid ${icon} text-lg"></i>
        <span>${message}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.remove('translate-y-2', 'opacity-0');
    }, 10);

    setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-y-2');
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

async function apiFetch(url, options = {}) {
    try {
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
        const headers = {
            'Content-Type': 'application/json',
            ...(csrfMeta && csrfHeader && csrfMeta.content ? { [csrfHeader.content]: csrfMeta.content } : {}),
            ...options.headers
        };

        const res = await fetch(url, {
            ...options,
            headers: headers
        });
        const data = await res.json();
        if (!res.ok) {
            throw new Error(data.message || 'Lỗi xử lý yêu cầu.');
        }
        return data;
    } catch (err) {
        showToast(err.message, 'error');
        throw err;
    }
}

function quickSwitchUser(username) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/login';

    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    if (csrfMeta && csrfMeta.content) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = '_csrf';
        csrfInput.value = csrfMeta.content;
        form.appendChild(csrfInput);
    }

    const uInput = document.createElement('input');
    uInput.type = 'hidden';
    uInput.name = 'username';
    uInput.value = username;

    const pInput = document.createElement('input');
    pInput.type = 'hidden';
    pInput.name = 'password';
    pInput.value = 'Password@123';

    form.appendChild(uInput);
    form.appendChild(pInput);
    document.body.appendChild(form);
    form.submit();
}
