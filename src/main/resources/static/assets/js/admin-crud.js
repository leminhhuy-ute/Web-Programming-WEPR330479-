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

function initDateTimeFields(form) {
    const options = (maximum, placeholder) =>
        `<option value="">${placeholder}</option>` +
        Array.from({length: maximum + 1}, (_, value) => {
            const padded = String(value).padStart(2, "0");
            return `<option value="${padded}">${padded}</option>`;
        }).join("");

    form.querySelectorAll("[data-date-time-value]").forEach((hidden) => {
        const field = hidden.closest("[data-date-time-field]");
        const date = field.querySelector("[data-date-part]");
        const hour = field.querySelector("[data-hour-part]");
        const minute = field.querySelector("[data-minute-part]");
        const title = field.querySelector(".date-time-field__title")?.textContent.trim()
            || field.closest("[data-period-field]")?.querySelector(".date-time-field__title")?.textContent.trim()
            || "Thời gian";
        date.setAttribute("aria-label", `${title} — ngày`);
        hour.setAttribute("aria-label", `${title} — giờ (00–23)`);
        minute.setAttribute("aria-label", `${title} — phút (00–59)`);
        hour.innerHTML = options(23, "Giờ");
        minute.innerHTML = options(59, "Phút");

        const initial = /^(\d{4}-\d{2}-\d{2})T(\d{2}):(\d{2})/.exec(hidden.value || "");
        if (initial) {
            date.value = initial[1];
            hour.value = initial[2];
            minute.value = initial[3];
        }

        const required = hidden.dataset.required === "true";
        setDateTimeRequired(hidden, required);
        const sync = () => {
            hidden.value = date.value && hour.value !== "" && minute.value !== ""
                ? `${date.value}T${hour.value}:${minute.value}`
                : "";
        };
        [date, hour, minute].forEach((input) => input.addEventListener("change", sync));
        field.syncDateTimeValue = sync;
    });
}

function setDateTimeRequired(hidden, required) {
    const field = hidden.closest("[data-date-time-field]");
    field.querySelectorAll("[data-date-part], [data-hour-part], [data-minute-part]")
        .forEach((input) => { input.required = required; });
    hidden.dataset.required = String(required);
}

function clearDateTimeField(hidden) {
    const field = hidden.closest("[data-date-time-field]");
    field.querySelectorAll("[data-date-part], [data-hour-part], [data-minute-part]")
        .forEach((input) => { input.value = ""; });
    hidden.value = "";
}

const local = (v) => formatDateTime(v);

const labels = {
    DEAN: "Trưởng khoa", LECTURER: "Giảng viên", STUDENT: "Sinh viên",
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
        return esc(name || "—");
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
        let currentPage = 0;
        const pager = document.createElement("div");
        pager.className = "table-pagination";
        pager.setAttribute("aria-label", "Phân trang");
        const paginationAnchor = page === "departments"
            ? table.closest(".table-scroll")
            : table.closest("table");
        paginationAnchor.insertAdjacentElement("afterend", pager);
        const renderPager = (data) => {
            pager.innerHTML = `<button class="button secondary" type="button" data-page-prev ${data.first ? "disabled" : ""}>Trang trước</button>
                <span>Trang ${data.totalPages ? data.page + 1 : 0}/${data.totalPages} · ${data.totalElements} bản ghi</span>
                <button class="button secondary" type="button" data-page-next ${data.last || !data.totalPages ? "disabled" : ""}>Trang sau</button>`;
            pager.querySelector("[data-page-prev]").onclick = () => { currentPage--; void load(); };
            pager.querySelector("[data-page-next]").onclick = () => { currentPage++; void load(); };
        };
        const load = async () => {
            try {
                clearError();
                table.innerHTML = `<tr><td colspan="${page === 'users' ? 8 : cfg.columns.length + 1}" class="loading-cell"><div class="loading-state"><span class="loading-spinner"></span><span>Đang tải dữ liệu...</span></div></td></tr>`;
                const query = new URLSearchParams(
                    new FormData(document.querySelector("#search")),
                );
                query.set("page", String(currentPage));
                query.set("size", "20");
                const data = await request(cfg.url + "/page?" + query);
                table.innerHTML =
                    data.content
                        .map(
                            (x) =>
                                `<tr>${cfg.columns.map((k) => `<td>${cellValue(k, x[k])}</td>`).join("")}<td class="action-cell">${actionButtons(x)}</td></tr>`,
                        )
                        .join("") || `<tr><td colspan="${page === 'users' ? 8 : cfg.columns.length + 1}" class="empty-cell"><div class="empty-state"><span>Chưa có dữ liệu.</span></div></td></tr>`;
                renderPager(data);

                table.querySelectorAll("[data-delete]").forEach(
                    (b) =>
                    (b.onclick = async () => {
                        if (!confirm("Xóa dữ liệu này?")) return;
                        try {
                            await request(cfg.url + "/" + b.dataset.delete, {
                                method: "DELETE",
                            });
                            if (data.content.length === 1 && currentPage > 0) currentPage--;
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
                table.innerHTML = `<tr><td colspan="${page === 'users' ? 8 : cfg.columns.length + 1}" class="empty-cell"><div class="empty-state"><span>Không thể tải dữ liệu.</span></div></td></tr>`;
                showError(error);
            }
        };

        document.querySelector("#search").onsubmit = (e) => {
            e.preventDefault();
            currentPage = 0;
            void load();
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
            initDateTimeFields(document.querySelector("#entity-form"));
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
                const reviewDateTime = review.querySelector("[data-date-time-value]");
                setDateTimeRequired(reviewDateTime, needsReview);
                defense.querySelector("input").required = needsDefense;
                if (!needsReview) clearDateTimeField(reviewDateTime);
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
            document.querySelectorAll("[data-date-time-field]").forEach((field) => field.syncDateTimeValue?.());
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
