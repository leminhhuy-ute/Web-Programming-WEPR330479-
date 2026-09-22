const APP_CONTEXT = location.pathname.startsWith("/topic-management/")
    ? "/topic-management"
    : "";
const api = (path) => APP_CONTEXT + path;

function formatDateTime(value) {
    if (value == null || value === "") return "—";

    const pad = (number) => String(number).padStart(2, "0");
    const formatParts = (year, month, day, hour = 0, minute = 0) =>
        `${pad(day)}/${pad(month)}/${year} ${pad(hour)}:${pad(minute)}`;

    if (Array.isArray(value)) {
        const [year, month, day, hour = 0, minute = 0] = value.map(Number);
        if (![year, month, day, hour, minute].every(Number.isFinite)) return "—";
        return formatParts(year, month, day, hour, minute);
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "—";
    return formatParts(
        date.getFullYear(),
        date.getMonth() + 1,
        date.getDate(),
        date.getHours(),
        date.getMinutes(),
    );
}

function cookie(name) {
    return document.cookie
        .split("; ")
        .find((v) => v.startsWith(name + "="))
        ?.split("=")
        .slice(1)
        .join("=");
}

async function request(path, options = {}) {
    const headers = { Accept: "application/json", ...(options.headers || {}) };
    if (options.method && !["GET", "HEAD"].includes(options.method)) {
        if (!cookie("XSRF-TOKEN")) await request("/api/auth/csrf");
        headers["Content-Type"] = "application/json";
        const csrf = decodeURIComponent(cookie("XSRF-TOKEN") || "");
        if (csrf) headers["X-XSRF-TOKEN"] = csrf;
    }
    const response = await fetch(api(path), {
        credentials: "same-origin",
        ...options,
        headers,
    });
    if (response.status === 401) {
        if (options.redirectOnUnauthorized === false) {
            let body = null;
            try {
                body = await response.json();
            } catch { }
            throw new Error(body?.message || "Tên đăng nhập/email hoặc mật khẩu không đúng.");
        }
        location.href = api("/login.html");
        throw new Error("Phiên đăng nhập đã hết hạn.");
    }
    let body = null;
    try {
        body = await response.json();
    } catch { }
    if (!response.ok || body?.success === false)
        throw new Error(body?.message || "Yêu cầu không thành công.");
    return body?.data;
}

let authMePromise = null;
async function getCurrentUser(forceRefresh = false) {
    if (forceRefresh || !authMePromise) {
        authMePromise = request("/api/auth/me").catch((err) => {
            authMePromise = null;
            throw err;
        });
    }
    return authMePromise;
}

async function requireDean() {
    const user = await getCurrentUser();
    if (user.role !== "DEAN") {
        document.body.innerHTML =
            '<main class="main-content"><h1>403</h1><p>Bạn không có quyền quản trị.</p></main>';
        throw new Error("Forbidden");
    }
    if (typeof window.renderCurrentUser === "function") {
        window.renderCurrentUser(user);
    }
    return user;
}

document.addEventListener("click", async (event) => {
    const logoutButton = event.target.closest("[data-logout]");
    if (!logoutButton) return;

    event.preventDefault();
    try {
        authMePromise = null;
        await request("/api/auth/logout", { method: "POST" });
    } finally {
        location.href = api("/login.html");
    }
});

// Compatibility facade for the lecturer module; all roles share one HTTP client.
const App = {
    getContextPath: () => APP_CONTEXT,
    getCurrentUser,
    fetchApi: async (url, options = {}) => ({success: true, data: await request(url, options)}),
    logout: async () => { await request('/api/auth/logout', {method:'POST'}); location.href=api('/login.html'); },
    formatDate: formatDateTime
};
request('/api/auth/csrf').catch(() => {});
function escapeHtml(value) { return String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
