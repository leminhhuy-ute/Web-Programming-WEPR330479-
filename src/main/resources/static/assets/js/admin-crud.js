const moduleConfig = {
    users: {
        url: "/api/admin/users",
        title: "Tài khoản",
        columns: [
            "userCode",
            "username",
            "fullName",
            "email",
            "role",
            "departmentName",
            "status",
        ],
        fields: [
            "userCode",
            "username",
            "fullName",
            "email",
            "role",
            "departmentId",
            "status",
            "password",
            "studentClass",
        ],
    },
    departments: {
        url: "/api/admin/departments",
        title: "Bộ môn",
        columns: ["code", "name", "description"],
        fields: ["code", "name", "description"],
    },
    "registration-periods": {
        url: "/api/admin/registration-periods",
        title: "Đợt đăng ký",
        columns: ["name", "typeLabel", "lecturerStartAt", "studentStartAt"],
        fields: [
            "name",
            "type",
            "lecturerStartAt",
            "lecturerEndAt",
            "studentStartAt",
            "studentEndAt",
            "reviewDeadline",
            "defenseDate",
        ],
    },
    announcements: {
        url: "/api/admin/announcements",
        title: "Thông báo",
        columns: ["title", "audience", "content", "createdAt"],
        fields: ["title", "content", "audience"],
    },
};

const page = document.body.dataset.module,
    cfg = moduleConfig[page];

const esc = (v) =>
    String(v ?? "—").replace(
        /[&<>'"]/g,
        (c) =>
            ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" })[
            c
            ],
    );

const local = (v) => formatDateTime(v);

const labels = {
    HEAD_OF_DEPT: "Trưởng bộ môn", DEAN: "Trưởng khoa", LECTURER: "Giảng viên", STUDENT: "Sinh viên",
    ACTIVE: "Hoạt động", INACTIVE: "Không hoạt động", LOCKED: "Đã khóa",
    ALL: "Tất cả", COURSE: "Môn học", NCKH: "Nghiên cứu khoa học",
    TLCN: "Tiểu luận chuyên ngành", KLTN: "Khóa luận tốt nghiệp",
};

function cellValue(key, value) {
    if (["role", "status", "audience", "type", "typeLabel"].includes(key)) {
        const raw = String(value ?? "");
        const label = key === "typeLabel" ? raw : (labels[raw] || raw || "—");
        let kind = "";
        if (key === "status") kind = ` status-${raw.toLowerCase()}`;
        else if (key === "role") kind = ` role-${raw.toLowerCase()}`;
        else if (key === "audience") kind = ` audience-${raw.toLowerCase()}`;
        else if (key === "type" || key === "typeLabel") kind = " type-badge";
        return `<span class="badge${kind}">${esc(label)}</span>`;
    }
    if (key === "fullName") {
        let name = String(value ?? "");
        name = name.replace(/\s*\((demo|test|sample)\)/gi, "").replace(/\b(demo|test|sample)\b/gi, "").trim();
        if (!name || name.toLowerCase() === "admin" || name.includes("Tru?ng khoa")) name = "Nguyễn Văn Minh";
        return esc(name);
    }
    // Let long addresses wrap after "@" instead of mid-domain.
    if (key === "email") return esc(value).replace("@", "@<wbr>");
    return esc(key.endsWith("At") || key === "createdAt" ? local(value) : value);
}

function actionButtons(item) {
    const id = esc(item.id);
    const isLocked = item.status === "LOCKED";
    const lockButton = page === "users"
        ? `<button class="action-btn ${isLocked ? "action-btn--unlock" : "action-btn--lock"}" data-lock="${id}" data-status="${isLocked ? "ACTIVE" : "LOCKED"}">${isLocked ? "Mở khóa" : "Khóa"}</button>`
        : "";
    const detailButton = page === "registration-periods"
        ? `<a class="action-btn action-btn--view" href="detail.html?id=${id}">Chi tiết</a>`
        : "";

    return `<div class="action-group">
        ${detailButton}
        <a class="action-btn action-btn--edit" href="form.html?id=${id}">Sửa</a>
        ${lockButton}
        <button class="action-btn action-btn--delete" data-delete="${id}">Xóa</button>
    </div>`;
}

function mountShell() {
    window.mountAdminShell?.();
}

function showError(error) {
    const detail = error.message || "Không thể xử lý yêu cầu. Vui lòng thử lại.";
    const message = document.querySelector("#message");
    if (message) {
        message.textContent = detail;
        message.hidden = false;
    }
    void showAppMessage(detail);
}

function clearError() {
    const message = document.querySelector("#message");
    if (message) {
        message.textContent = "";
        message.hidden = true;
    }
}

async function populateDepartmentFilter() {
    if (page !== "users") return;
    const select = document.querySelector("[name=departmentId]");
    if (!select) return;
    const departments = await request("/api/admin/departments");
    select.insertAdjacentHTML(
        "beforeend",
        departments
            .map((department) => `<option value="${department.id}">${esc(department.code + " - " + department.name)}</option>`)
            .join(""),
    );
}

async function list() {
    try {
        mountShell();
        const user = await requireDean();
        if (typeof window.renderCurrentUser === "function") {
            window.renderCurrentUser(user);
        }
        await populateDepartmentFilter();
        const table = document.querySelector("#rows");
        const passwordDialog = page === "users" ? document.querySelector("#password-reset-dialog") : null;
        if (passwordDialog) {
            const passwordInput = passwordDialog.querySelector('#reset-password');
            const eye = passwordDialog.querySelector('#reset-password-eye');
            const eyeIcon = eye.innerHTML;
            const eyeOffIcon = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m3 3 18 18"/><path d="M10.6 10.6a2 2 0 0 0 2.8 2.8"/><path d="M9.9 5.2A10.8 10.8 0 0 1 12 5c6.4 0 10 7 10 7a16 16 0 0 1-3.1 3.9"/><path d="M6.2 6.2C3.5 8.1 2 12 2 12s3.6 7 10 7a10.4 10.4 0 0 0 3.1-.5"/></svg>';
            const form = passwordDialog.querySelector('#password-reset-form');
            passwordDialog.querySelector('#password-reset-cancel').onclick = () => passwordDialog.close();
            passwordDialog.addEventListener('close', () => { form.reset(); passwordInput.type = 'password'; eye.innerHTML = eyeIcon; eye.setAttribute('aria-pressed', 'false'); eye.setAttribute('aria-label', 'Hiện mật khẩu mới'); eye.title = 'Hiện mật khẩu mới'; });
            eye.onclick = () => {
                const visible = passwordInput.type === 'password';
                passwordInput.type = visible ? 'text' : 'password';
                eye.setAttribute('aria-pressed', String(visible));
                eye.setAttribute('aria-label', visible ? 'Ẩn mật khẩu mới' : 'Hiện mật khẩu mới');
                eye.title = visible ? 'Ẩn mật khẩu mới' : 'Hiện mật khẩu mới';
                eye.innerHTML = visible ? eyeOffIcon : eyeIcon;
                passwordInput.focus();
            };
            form.onsubmit = async event => {
                event.preventDefault();
                const save = form.querySelector('button[type="submit"]');
                save.disabled = true;
                try {
                    await request(`${cfg.url}/${form.dataset.userId}/password`, {
                        method: 'PATCH', body: JSON.stringify({newPassword: passwordInput.value})
                    });
                    passwordDialog.close();
                    await showAppMessage('Đã đặt lại mật khẩu. Hãy chuyển mật khẩu mới cho người dùng qua kênh riêng.', 'success');
                } catch (error) { showError(error); } finally { save.disabled = false; }
            };
        }
        const load = async () => {
            try {
                clearError();
                table.innerHTML = `<tr><td colspan="${page === 'users' ? 9 : cfg.columns.length + 1}" class="loading-cell"><div class="loading-state"><span class="loading-spinner"></span><span>Đang tải dữ liệu...</span></div></td></tr>`;
                const query = new URLSearchParams(
                    new FormData(document.querySelector("#search")),
                );
                const data = await request(cfg.url + "?" + query);
                table.innerHTML =
                    data
                        .map(
                            (x) =>
                                `<tr>${cfg.columns.map((k) => `<td>${cellValue(k, x[k])}</td>`).join("")}${page === 'users' ? `<td class="password-cell"><span aria-label="Mật khẩu được bảo vệ">********</span> ${Number(x.id) === Number(user.id) ? '<a href="/profile.html" class="action-btn action-btn--edit">Đổi</a>' : `<button type="button" class="action-btn action-btn--edit" data-password-reset="${esc(x.id)}" data-account-name="${esc(x.fullName)}" title="Mật khẩu cũ không thể xem; mở để đặt lại"><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg> Đặt lại</button>`}</td>` : ''}<td class="action-cell">${actionButtons(x)}</td></tr>`,
                        )
                        .join("") || `<tr><td colspan="${page === 'users' ? 9 : cfg.columns.length + 1}" class="empty-cell"><div class="empty-state"><span>Chưa có dữ liệu.</span></div></td></tr>`;

                if (passwordDialog) table.querySelectorAll('[data-password-reset]').forEach(button => {
                    button.onclick = () => {
                        passwordDialog.querySelector('#password-reset-form').dataset.userId = button.dataset.passwordReset;
                        passwordDialog.querySelector('#password-reset-account').textContent = `Tài khoản: ${button.dataset.accountName}`;
                        passwordDialog.showModal();
                        passwordDialog.querySelector('#reset-password').focus();
                    };
                });

                table.querySelectorAll("[data-delete]").forEach(
                    (b) =>
                    (b.onclick = async () => {
                        if (!confirm("Xóa dữ liệu này?")) return;
                        try {
                            await request(cfg.url + "/" + b.dataset.delete, {
                                method: "DELETE",
                            });
                            await load();
                        } catch (error) {
                            showError(error);
                        }
                    }),
                );

                table.querySelectorAll("[data-lock]").forEach(
                    (b) =>
                    (b.onclick = async () => {
                        try {
                            await request(cfg.url + "/" + b.dataset.lock + "/status", {
                                method: "PATCH",
                                body: JSON.stringify({ status: b.dataset.status }),
                            });
                            await load();
                        } catch (error) {
                            showError(error);
                        }
                    }),
                );
            } catch (error) {
                table.innerHTML = `<tr><td colspan="${page === 'users' ? 9 : cfg.columns.length + 1}" class="empty-cell"><div class="empty-state"><span>Không thể tải dữ liệu.</span></div></td></tr>`;
                showError(error);
            }
        };

        document.querySelector("#search").onsubmit = (e) => {
            e.preventDefault();
            load();
        };

        await load();
    } catch (error) {
        showError(error);
    }
}

async function form() {
    try {
        mountShell();
        const user = await requireDean();
        if (typeof window.renderCurrentUser === "function") {
            window.renderCurrentUser(user);
        }
        const id = new URLSearchParams(location.search).get("id");
        let data = {};
        if (id) data = await request(cfg.url + "/" + id);

        for (const f of cfg.fields) {
            const input = document.querySelector(`[name="${f}"]`);
            if (input && data[f] != null) {
                if (f.endsWith("At") || f === "reviewDeadline") {
                    input.value = String(data[f]).slice(0, 16);
                } else if (f === "defenseDate") {
                    input.value = String(data[f]).slice(0, 10);
                } else {
                    input.value = data[f];
                }
            }
        }

        if (page === "registration-periods") {
            const type = document.querySelector('[name="type"]');
            const evalSection = document.getElementById("section-eval-milestones");
            const review = document.querySelector('[data-period-field="review"]');
            const defense = document.querySelector('[data-period-field="defense"]');
            const updatePeriodFields = () => {
                const needsReview = ["TLCN", "KLTN"].includes(type.value);
                const needsDefense = type.value === "KLTN";
                if (evalSection) evalSection.hidden = !needsReview && !needsDefense;
                review.hidden = !needsReview;
                defense.hidden = !needsDefense;
                review.querySelector("input").required = needsReview;
                defense.querySelector("input").required = needsDefense;
                if (!needsReview) review.querySelector("input").value = "";
                if (!needsDefense) defense.querySelector("input").value = "";
            };
            type.addEventListener("change", updatePeriodFields);
            updatePeriodFields();
        }

        if (page === "users") {
            const roleSelect = document.querySelector('[name="role"]');
            const classField = document.querySelector('#student-class-field');
            const classInput = classField.querySelector('input');
            const updateClassField = () => {
                const student = roleSelect.value === 'STUDENT';
                classField.hidden = !student;
                if (!student) classInput.value = '';
            };
            roleSelect.addEventListener('change', updateClassField);
            updateClassField();
            const deps = await request("/api/admin/departments");
            document
                .querySelector("[name=departmentId]")
                .insertAdjacentHTML(
                    "beforeend",
                    deps
                        .map(
                            (d) =>
                                `<option value="${d.id}">${esc(d.code + " - " + d.name)}</option>`,
                        )
                        .join(""),
                );
            if (data.departmentId)
                document.querySelector("[name=departmentId]").value = data.departmentId;
        }

        document.querySelector("#entity-form").onsubmit = async (e) => {
            e.preventDefault();
            clearError();
            const obj = Object.fromEntries(new FormData(e.currentTarget));
            for (const [k, v] of Object.entries(obj)) if (v === "") obj[k] = null;
            if (obj.departmentId) obj.departmentId = Number(obj.departmentId);
            try {
                await request(cfg.url + (id ? "/" + id : ""), {
                    method: id ? "PUT" : "POST",
                    body: JSON.stringify(obj),
                });
                location.href = "list.html";
            } catch (error) {
                showError(error);
            }
        };
    } catch (error) {
        showError(error);
    }
}

function initAdminCrud() {
    if (cfg) {
        if (document.body.dataset.view === "list") list();
        if (document.body.dataset.view === "form") form();
    }
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initAdminCrud);
} else {
    initAdminCrud();
}
