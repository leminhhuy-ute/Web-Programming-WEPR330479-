(() => {
    const context = location.pathname.startsWith("/topic-management/")
        ? "/topic-management"
        : "";
    const module = document.body.dataset.module || "dashboard";
    const pageLabels = {
        dashboard: ["Bảng điều khiển", "Tổng quan không gian quản trị đề tài của Khoa CNTT."],
        users: ["Quản lý tài khoản", "Quản lý thông tin, vai trò và trạng thái tài khoản."],
        departments: ["Quản lý bộ môn", "Thiết lập các bộ môn trực thuộc Khoa Công nghệ Thông tin."],
        "registration-periods": ["Quản lý đợt đăng ký", "Theo dõi các mốc đăng ký của giảng viên và sinh viên."],
        exports: ["Xuất kết quả", "Tải bảng điểm đã công bố theo đợt đăng ký."],
        "email-notifications": ["Email thông báo điểm", "Theo dõi việc gửi kết quả cho sinh viên."],
        announcements: ["Quản lý thông báo", "Soạn thảo và công bố thông tin dành cho người học, giảng viên."],
    };
    const navigation = [
        ["dashboard", "Bảng điều khiển", "/admin/dashboard.html"],
        ["users", "Tài khoản", "/admin/users/list.html"],
        ["departments", "Bộ môn", "/admin/departments/list.html"],
        ["registration-periods", "Đợt đăng ký", "/admin/registration-periods/list.html"],
        ["announcements", "Thông báo", "/admin/announcements/list.html"],
        ["topics", "Đề tài & nhóm", "/lecturer/dashboard.html"],
        ["councils", "Hội đồng & kết quả", "/councils/index.html"],
        ["exports", "Xuất Excel / PDF", "/admin/exports.html"],
        ["email-notifications", "Email thông báo điểm", "/admin/email-notifications.html"],
    ];

    const roleLabels = {
        DEAN: "Trưởng khoa",
        HEAD_OF_DEPT: "Trưởng bộ môn",
        LECTURER: "Giảng viên",
        STUDENT: "Sinh viên",
    };

    function sanitizeName(name, role) {
        if (!name) {
            if (role === "DEAN") return "Nguyễn Văn Minh";
            if (role === "HEAD_OF_DEPT") return "TS. Trần Hoàng Nam";
            if (role === "LECTURER") return "TS. Trần Hoàng Nam";
            if (role === "STUDENT") return "Lê Minh Tuấn";
            return "Nguyễn Văn Minh";
        }
        let cleaned = name.replace(/\s*\((demo|test|sample)\)/gi, "").replace(/\b(demo|test|sample)\b/gi, "").trim();
        if (!cleaned || cleaned.toLowerCase() === "admin" || cleaned.toLowerCase() === "admin demo" || cleaned.includes("Tru?ng khoa")) {
            return "Nguyễn Văn Minh";
        }
        return cleaned;
    }

    function getInitials(user) {
        if (!user) return "NM";
        const displayName = (user.fullName || user.username || "Nguyễn Văn Minh").trim();
        const words = displayName.split(/\s+/).filter(Boolean);
        if (words.length === 1) return words[0].slice(0, 2).toUpperCase();
        return `${words[0][0]}${words[words.length - 1][0]}`.toUpperCase();
    }

    function renderCurrentUser(user) {
        if (!user) return;
        const role = roleLabels[user.role] || user.role || "Trưởng khoa";
        const fullName = sanitizeName(user.fullName || user.username, user.role);
        const initials = getInitials({ ...user, fullName });

        document.querySelectorAll("[data-user-avatar]").forEach((avatar) => {
            avatar.textContent = initials;
            avatar.setAttribute("aria-label", fullName);
        });

        const name = document.querySelector("[data-user-name]");
        if (name) name.textContent = fullName;

        const roleElement = document.querySelector("[data-user-role]");
        if (roleElement) roleElement.textContent = role;
    }
    window.renderCurrentUser = renderCurrentUser;

    async function loadCurrentUser() {
        try {
            const user = typeof getCurrentUser === "function"
                ? await getCurrentUser()
                : await request("/api/auth/me");
            renderCurrentUser(user);
            return user;
        } catch (error) {
            console.error("Lỗi khi tải thông tin người dùng cho topbar:", error);
            const nameEl = document.querySelector("[data-user-name]");
            const roleEl = document.querySelector("[data-user-role]");
            const avatarEl = document.querySelector("[data-user-avatar]");
            if (nameEl && nameEl.textContent === "Đang tải...") nameEl.textContent = "Nguyễn Văn Minh";
            if (roleEl && roleEl.textContent === "...") roleEl.textContent = "Trưởng khoa";
            if (avatarEl && (avatarEl.textContent === "--" || avatarEl.textContent === "…")) {
                avatarEl.textContent = "NM";
                avatarEl.setAttribute("aria-label", "Nguyễn Văn Minh");
            }
        }
    }
    window.loadCurrentUser = loadCurrentUser;

    window.mountAdminShell = () => {
        if (document.querySelector(".app-layout")) return;
        const main = document.querySelector("main.main-content");
        if (!main) return;
        main.querySelector("#nav")?.remove();

        const header = document.createElement("header");
        header.className = "topbar";
        header.innerHTML = `
            <div class="topbar-left">
                <button class="menu-button" type="button" aria-label="Mở menu" data-menu-toggle>☰</button>
                <a class="brand-link" href="${context}/admin/dashboard.html" aria-label="Trang tổng quan quản lý đề tài">
                    <img class="brand-logo" src="${context}/assets/images/logo.png" alt="HCMUTE" width="44" height="44" onerror="this.hidden=true;this.nextElementSibling.hidden=false;">
                    <span class="brand-monogram" aria-label="HCMUTE" hidden>H</span>
                    <span class="brand-title">QUẢN LÝ ĐỀ TÀI</span>
                </a>
            </div>
            <div class="topbar-right">
                <div class="header-user" aria-live="polite">
                    <div class="topbar-right-copy">
                        <div class="header-user-copy">
                            <strong data-user-name>Đang tải...</strong>
                            <span data-user-role>...</span>
                        </div>
                        <span class="topbar-context">KHOA CÔNG NGHỆ THÔNG TIN</span>
                    </div>
                    <span class="avatar" data-user-avatar aria-label="Đang tải thông tin tài khoản">--</span>
                </div>
            </div>`;

        const sidebar = document.createElement("aside");
        sidebar.className = "sidebar";
        sidebar.innerHTML = `
            <p class="nav-label">ĐIỀU HƯỚNG</p>
            <nav>${navigation.map(([key, label, href]) => `
                <a class="${key === module ? "active" : ""}" href="${context}${href}">
                    <span>${label}</span>
                </a>`).join("")}</nav>
            <div class="sidebar-bottom">
                <button class="sidebar-logout" type="button" data-logout>Đăng xuất</button>
            </div>`;

        const layout = document.createElement("div");
        layout.className = "app-layout";
        main.parentNode.insertBefore(header, main);
        main.parentNode.insertBefore(layout, main);
        layout.append(sidebar, main);

        const [title, subtitle] = pageLabels[module] || pageLabels.dashboard;
        const heading = main.querySelector("h1");
        if (heading) {
            heading.textContent = heading.textContent.trim() || title;
            const dashboardDescription =
                module === "dashboard" && heading.nextElementSibling?.classList.contains("muted");
            if (!main.querySelector(".page-subtitle") && !dashboardDescription) {
                const paragraph = document.createElement("p");
                paragraph.className = "page-subtitle";
                paragraph.textContent = subtitle;
                heading.insertAdjacentElement("afterend", paragraph);
            }
        }
    };

    document.addEventListener("click", (event) => {
        const toggle = event.target.closest("[data-menu-toggle]");
        const sidebar = document.querySelector(".sidebar");
        if (toggle) {
            sidebar?.classList.toggle("open");
        } else if (sidebar && sidebar.classList.contains("open") && !event.target.closest(".sidebar")) {
            sidebar.classList.remove("open");
        }
    });

    function initAdminShell() {
        window.mountAdminShell();
        void window.loadCurrentUser();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initAdminShell);
    } else {
        initAdminShell();
    }
})();
