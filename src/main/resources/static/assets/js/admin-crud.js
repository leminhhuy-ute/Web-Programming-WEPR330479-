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
    const message = document.querySelector("#message");
    if (message) {
        message.textContent = error.message || "Không thể tải dữ liệu. Vui lòng thử lại.";
        message.hidden = false;
    }
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
        const load = async () => {
            try {
                clearError();
                table.innerHTML = '<tr><td colspan="10" class="loading-cell"><div class="loading-state"><span class="loading-spinner"></span><span>Đang tải dữ liệu...</span></div></td></tr>';
                const query = new URLSearchParams(
                    new FormData(document.querySelector("#search")),
                );
                const data = await request(cfg.url + "?" + query);
                table.innerHTML =
                    data
                        .map(
                            (x) =>
                                `<tr>${cfg.columns.map((k) => `<td>${cellValue(k, x[k])}</td>`).join("")}<td class="action-cell">${actionButtons(x)}</td></tr>`,
                        )
                        .join("") || '<tr><td colspan="10" class="empty-cell"><div class="empty-state"><span>Chưa có dữ liệu.</span></div></td></tr>';

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
                table.innerHTML = '<tr><td colspan="10" class="empty-cell"><div class="empty-state"><span>Không thể tải dữ liệu.</span></div></td></tr>';
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
            if (input && data[f] != null)
                input.value = f.endsWith("At") ? String(data[f]).slice(0, 16) : data[f];
        }

        if (page === "users") {
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
