const lecturerLabel = value => ({COURSE:'Môn học',NCKH:'Nghiên cứu khoa học',TLCN:'Tiểu luận chuyên ngành',KLTN:'Khóa luận tốt nghiệp',PENDING:'Chờ duyệt',APPROVED:'Đã duyệt',REJECTED:'Từ chối'}[value] || value);

const cleanLecturerName = (name) => {
    if (!name) return '—';
    let c = String(name).replace(/\s*\((demo|test|sample)\)/gi, '').replace(/\b(demo|test|sample)\b/gi, '').trim();
    if (!c || c.toLowerCase() === 'admin' || c.toLowerCase() === 'admin demo' || c.includes('Tru?ng khoa')) return 'Nguyễn Văn Minh';
    return c;
};

const LecturerCrud = {
    async initDashboard() {
        try {
            const user = await App.getCurrentUser();
            let topicsUrl = `${App.getContextPath()}/api/lecturer/topics`;
            let myTopicsLabel = 'Đề tài tôi đề xuất';
            let recentHeading = 'Đề tài đề xuất gần đây';

            if (user && user.role === 'DEAN') {
                topicsUrl = `${App.getContextPath()}/api/lecturer/topics`;
                myTopicsLabel = 'Tổng đề tài toàn khoa';
                recentHeading = 'Đề tài toàn khoa gần đây';
            } else if (user && user.role === 'HEAD_OF_DEPT') {
                const deptId = user.department ? user.department.id : (user.departmentId || '');
                topicsUrl = `${App.getContextPath()}/api/lecturer/topics?departmentId=${deptId}`;
                const deptCode = (user.department && user.department.code) || user.departmentCode || '';
                myTopicsLabel = `Đề tài thuộc bộ môn ${deptCode ? '(' + deptCode + ')' : ''}`.trim();
                recentHeading = `Đề tài bộ môn gần đây ${deptCode ? '(' + deptCode + ')' : ''}`.trim();
            } else {
                topicsUrl = `${App.getContextPath()}/api/lecturer/topics?myOnly=true`;
                myTopicsLabel = 'Đề tài tôi đề xuất';
                recentHeading = 'Đề tài đề xuất gần đây';
            }

            const labelEl = document.querySelector('.stat-card:first-child .label');
            if (labelEl) labelEl.textContent = myTopicsLabel;
            const headingEl = document.querySelector('.content-body .card h2') || document.querySelector('.main-content .card h2');
            if (headingEl) headingEl.textContent = recentHeading;

            const res = await App.fetchApi(topicsUrl);
            const topics = res.data || [];
            document.getElementById('stat-my-topics').textContent = topics.length;
            document.getElementById('stat-pending-topics').textContent = topics.filter(t => t.status === 'PENDING').length;
            document.getElementById('stat-approved-topics').textContent = topics.filter(t => t.status === 'APPROVED').length;

            const groupsRes = await App.fetchApi(App.getContextPath() + '/api/lecturer/student-groups');
            document.getElementById('stat-groups').textContent = (groupsRes.data || []).length;

            this.renderRecentTopics(topics.slice(0, 5));
        } catch (e) {
            console.error(e);
        }
    },

    renderRecentTopics(topics) {
        const tbody = document.getElementById('recent-topics-tbody');
        if (!tbody) return;
        if (!topics || topics.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding: 24px; color: var(--text-muted);">Chưa có đề tài nào.</td></tr>';
            return;
        }

        tbody.innerHTML = topics.map(t => `
            <tr>
                <td><strong>${escapeHtml(t.topicCode)}</strong></td>
                <td><a href="/lecturer/topics/detail.html?id=${t.id}"><strong>${escapeHtml(t.title)}</strong></a></td>
                <td>${escapeHtml(cleanLecturerName(t.createdBy ? t.createdBy.fullName : '—'))}</td>
                <td><span class="badge ${t.topicType.toLowerCase()}">${escapeHtml(lecturerLabel(t.topicType))}</span></td>
                <td><span class="badge ${t.status.toLowerCase()}">${escapeHtml(lecturerLabel(t.status))}</span></td>
                <td>${App.formatDate(t.createdAt)}</td>
            </tr>
        `).join('');
    },

    async loadFilterOptions() {
        try {
            const depts = await App.fetchApi(App.getContextPath() + '/api/common/departments');
            const deptSelect = document.getElementById('filter-dept');
            if (deptSelect) {
                deptSelect.innerHTML = '<option value="">-- Tất cả Bộ môn --</option>' +
                    depts.data.map(d => `<option value="${d.id}">${escapeHtml(d.name)} (${escapeHtml(d.code)})</option>`).join('');
            }

            const periods = await App.fetchApi(App.getContextPath() + '/api/common/registration-periods');
            const periodSelect = document.getElementById('filter-period');
            if (periodSelect) {
                periodSelect.innerHTML = '<option value="">-- Tất cả Đợt đăng ký --</option>' +
                    periods.data.map(p => `<option value="${p.id}">${escapeHtml(p.name)}</option>`).join('');
            }
        } catch (e) {
            console.error(e);
        }
    },

    async loadTopicList() {
        this.currentUser = await App.getCurrentUser();
        await this.loadFilterOptions();
        const fetchAndRender = async () => {
            const deptId = document.getElementById('filter-dept')?.value || '';
            const periodId = document.getElementById('filter-period')?.value || '';
            const type = document.getElementById('filter-type')?.value || '';
            const status = document.getElementById('filter-status')?.value || '';
            const search = document.getElementById('filter-search')?.value || '';
            const myOnly = document.getElementById('filter-my-only')?.checked ? 'true' : '';

            let url = `${App.getContextPath()}/api/lecturer/topics?departmentId=${deptId}&periodId=${periodId}&type=${type}&status=${status}&search=${encodeURIComponent(search)}&myOnly=${myOnly}`;
            try {
                const res = await App.fetchApi(url);
                this.renderTopicTable(res.data);
            } catch (e) {
                alert(e.message);
            }
        };

        ['filter-dept', 'filter-period', 'filter-type', 'filter-status', 'filter-my-only'].forEach(id => {
            document.getElementById(id)?.addEventListener('change', fetchAndRender);
        });
        document.getElementById('filter-search')?.addEventListener('input', fetchAndRender);

        fetchAndRender();
    },

    renderTopicTable(topics) {
        const tbody = document.getElementById('topics-tbody');
        if (!tbody) return;
        if (topics.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding: 24px; color: var(--text-muted);">Không tìm thấy đề tài phù hợp.</td></tr>';
            return;
        }

        tbody.innerHTML = topics.map(t => `
            <tr>
                <td><strong>${escapeHtml(t.topicCode)}</strong></td>
                <td><a href="detail.html?id=${t.id}"><strong>${escapeHtml(t.title)}</strong></a></td>
                <td>${escapeHtml(cleanLecturerName(t.createdBy ? t.createdBy.fullName : '—'))}</td>
                <td><span class="badge ${t.topicType.toLowerCase()}">${escapeHtml(lecturerLabel(t.topicType))}</span></td>
                <td>${escapeHtml(t.department ? t.department.code : '—')}</td>
                <td><span class="badge ${t.status.toLowerCase()}">${escapeHtml(lecturerLabel(t.status))}</span></td>
                <td class="action-cell">
                    <div class="action-group">
                        <a href="detail.html?id=${t.id}" class="action-btn action-btn--view">Chi tiết</a>
                        ${t.createdBy?.id === this.currentUser?.id && t.status !== 'APPROVED' ? `<a href="form.html?id=${t.id}" class="action-btn action-btn--edit">Sửa</a>` : ''}
                        ${t.createdBy?.id === this.currentUser?.id && t.status !== 'APPROVED' ? `<button onclick="LecturerCrud.deleteTopic(${t.id})" class="action-btn action-btn--delete">Xóa</button>` : ''}
                    </div>
                </td>
            </tr>
        `).join('');
    },

    async deleteTopic(id) {
        if (!confirm('Bạn có chắc chắn muốn xóa đề tài đề xuất này không?')) return;
        try {
            await App.fetchApi(`${App.getContextPath()}/api/lecturer/topics/${id}`, { method: 'DELETE' });
            alert('Xóa đề tài thành công.');
            location.reload();
        } catch (e) {
            alert(e.message);
        }
    },

    async loadForm() {
        const depts = await App.fetchApi(App.getContextPath() + '/api/common/departments');
        const deptSelect = document.getElementById('form-department');
        if (deptSelect) {
            deptSelect.innerHTML = depts.data.map(d => `<option value="${d.id}">${escapeHtml(d.name)} (${escapeHtml(d.code)})</option>`).join('');
        }

        const periods = await App.fetchApi(App.getContextPath() + '/api/common/registration-periods');
        const periodSelect = document.getElementById('form-period');
        if (periodSelect) {
            periodSelect.innerHTML = periods.data.map(p => `<option value="${p.id}">${escapeHtml(p.name)}</option>`).join('');
        }

        const urlParams = new URLSearchParams(window.location.search);
        const id = urlParams.get('id');
        if (id) {
            document.getElementById('form-page-title').textContent = 'Chỉnh sửa thông tin đề tài';
            try {
                const res = await App.fetchApi(`${App.getContextPath()}/api/lecturer/topics/${id}`);
                const t = res.data;
                document.getElementById('form-topic-id').value = t.id;
                document.getElementById('form-code').value = t.topicCode;
                document.getElementById('form-title').value = t.title;
                document.getElementById('form-description').value = t.description || '';
                document.getElementById('form-requirements').value = t.requirements || '';
                document.getElementById('form-max-students').value = t.maxStudents;
                document.getElementById('form-type').value = t.topicType;
                if (t.department) document.getElementById('form-department').value = t.department.id;
                if (t.period) document.getElementById('form-period').value = t.period.id;
            } catch (e) {
                alert(e.message);
            }
        }

        const form = document.getElementById('topic-form');
        form?.addEventListener('submit', async (e) => {
            e.preventDefault();
            const topicId = document.getElementById('form-topic-id').value;
            const payload = {
                topicCode: document.getElementById('form-code').value,
                title: document.getElementById('form-title').value,
                description: document.getElementById('form-description').value,
                requirements: document.getElementById('form-requirements').value,
                maxStudents: parseInt(document.getElementById('form-max-students').value),
                topicType: document.getElementById('form-type').value,
                departmentId: parseInt(document.getElementById('form-department').value),
                periodId: parseInt(document.getElementById('form-period').value)
            };

            const method = topicId ? 'PUT' : 'POST';
            const url = topicId ? `${App.getContextPath()}/api/lecturer/topics/${topicId}` : `${App.getContextPath()}/api/lecturer/topics`;

            try {
                await App.fetchApi(url, {
                    method: method,
                    body: JSON.stringify(payload)
                });
                alert(topicId ? 'Cập nhật đề tài thành công!' : 'Đề xuất đề tài mới thành công!');
                window.location.href = 'list.html';
            } catch (err) {
                alert(err.message);
            }
        });
    },

    async loadDetail() {
        const urlParams = new URLSearchParams(window.location.search);
        const id = urlParams.get('id');
        if (!id) {
            window.location.href = 'list.html';
            return;
        }

        try {
            const res = await App.fetchApi(`${App.getContextPath()}/api/lecturer/topics/${id}`);
            const t = res.data;
            document.getElementById('detail-code').textContent = t.topicCode;
            document.getElementById('detail-title').textContent = t.title;
            document.getElementById('detail-type').textContent = lecturerLabel(t.topicType);
            document.getElementById('detail-type').className = `badge ${t.topicType.toLowerCase()}`;
            document.getElementById('detail-status').textContent = lecturerLabel(t.status);
            document.getElementById('detail-status').className = `badge ${t.status.toLowerCase()}`;
            document.getElementById('detail-creator').textContent = cleanLecturerName(t.createdBy ? t.createdBy.fullName : '—');
            document.getElementById('detail-dept').textContent = t.department ? t.department.name : '—';
            document.getElementById('detail-period').textContent = t.period ? t.period.name : '—';
            document.getElementById('detail-max').textContent = t.maxStudents + ' sinh viên';
            document.getElementById('detail-desc').textContent = t.description || 'Chưa có mô tả.';
            document.getElementById('detail-req').textContent = t.requirements || 'Chưa có yêu cầu đặc biệt.';
            document.getElementById('detail-adv1').textContent = cleanLecturerName(t.advisor1 ? t.advisor1.fullName : 'Chưa gán');
            document.getElementById('detail-adv2').textContent = cleanLecturerName(t.advisor2 ? t.advisor2.fullName : 'Chưa gán');

            if (t.rejectionReason) {
                document.getElementById('detail-reject-box').style.display = 'block';
                document.getElementById('detail-reject-reason').textContent = t.rejectionReason;
            }

            document.getElementById('btn-edit').href = `form.html?id=${t.id}`;
            const user = await App.getCurrentUser();
            document.getElementById('btn-edit').hidden = t.status === 'APPROVED' || t.createdBy?.id !== user.id;
        } catch (e) {
            alert(e.message);
        }
    },

    async loadApprovalPage() {
        const user = await App.getCurrentUser();
        let deptId = '';
        if (user && user.role === 'HEAD_OF_DEPT') {
            deptId = user.department ? user.department.id : (user.departmentId || '');
        }

        const fetchAndRender = async () => {
            const status = document.getElementById('approval-status-filter')?.value || '';
            let url = `${App.getContextPath()}/api/lecturer/department-topics?departmentId=${deptId}&status=${status}`;
            try {
                const res = await App.fetchApi(url);
                this.renderApprovalTable(res.data);
            } catch (e) {
                alert(e.message);
            }
        };

        document.getElementById('approval-status-filter')?.addEventListener('change', fetchAndRender);
        fetchAndRender();
    },

    _approvalTopics: [],
    _lecturers: [],

    async renderApprovalTable(topics) {
        this._approvalTopics = topics || [];
        const tbody = document.getElementById('approval-tbody');
        if (!tbody) return;
        if (topics.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding: 24px; color: var(--text-muted);">Không có đề tài cần quản lý.</td></tr>';
            return;
        }

        // Fetch lecturers for assignment dropdown
        if (!this._lecturers.length) {
            try {
                const lecturersRes = await App.fetchApi(App.getContextPath() + '/api/common/lecturers');
                this._lecturers = lecturersRes.data || [];
            } catch (err) {
                console.error('Error fetching lecturers', err);
            }
        }

        tbody.innerHTML = topics.map(t => {
            const adv1Name = t.advisor1 ? cleanLecturerName(t.advisor1.fullName) : 'Chưa gán';
            const adv2Name = t.advisor2 ? cleanLecturerName(t.advisor2.fullName) : null;
            const hasAdvisor = !!t.advisor1;

            return `
            <tr>
                <td><strong>${escapeHtml(t.topicCode)}</strong></td>
                <td><a href="detail.html?id=${t.id}"><strong>${escapeHtml(t.title)}</strong></a></td>
                <td>${escapeHtml(cleanLecturerName(t.createdBy ? t.createdBy.fullName : '—'))}</td>
                <td><span class="badge ${t.topicType.toLowerCase()}">${escapeHtml(lecturerLabel(t.topicType))}</span></td>
                <td><span class="badge ${t.status.toLowerCase()}">${escapeHtml(lecturerLabel(t.status))}</span></td>
                <td>
                    <div style="font-size:12.5px; line-height:1.45; margin-bottom:6px;">
                        <div><strong>GVHD 1:</strong> <span style="color:var(--navy); font-weight:600;">${escapeHtml(adv1Name)}</span></div>
                        ${adv2Name ? `<div><strong>GVHD 2:</strong> <span style="color:var(--text-secondary);">${escapeHtml(adv2Name)}</span></div>` : ''}
                        <div style="margin-top:4px;">
                            <span class="badge ${hasAdvisor ? 'approved' : 'pending'}" style="font-size:11px; padding:2px 8px;">${hasAdvisor ? 'Đã gán GVHD' : 'Chưa gán'}</span>
                        </div>
                    </div>
                    <button type="button" ${t.status === 'REJECTED' ? 'disabled' : ''} onclick="LecturerCrud.openAssignModal(${t.id})" class="action-btn action-btn--edit" style="width:100%; justify-content:center; font-size:12px; height:32px;">Gán giảng viên hướng dẫn</button>
                </td>
                <td class="action-cell">
                    <div class="action-group">
                        ${t.status === 'PENDING' ? `
                            <button onclick="LecturerCrud.approveTopic(${t.id})" class="action-btn action-btn--unlock">Duyệt</button>
                            <button onclick="LecturerCrud.rejectTopic(${t.id})" class="action-btn action-btn--delete">Từ chối</button>
                        ` : '<span class="text-muted" style="font-size:12.5px;">Đã xử lý</span>'}
                    </div>
                </td>
            </tr>
            `;
        }).join('');

        // Attach modal submit listener once
        const assignForm = document.getElementById('assign-advisor-form');
        if (assignForm && !assignForm.dataset.initialized) {
            assignForm.dataset.initialized = 'true';
            assignForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const topicId = document.getElementById('assign-topic-id').value;
                const adv1Id = document.getElementById('assign-adv1').value;
                const adv2Id = document.getElementById('assign-adv2').value;
                const errorEl = document.getElementById('assign-dialog-error');
                const submitBtn = document.getElementById('btn-submit-assign');

                if (errorEl) errorEl.style.display = 'none';

                if (!adv1Id) {
                    if (errorEl) {
                        errorEl.textContent = 'Vui lòng chọn Giảng viên hướng dẫn 1.';
                        errorEl.style.display = 'block';
                    }
                    return;
                }

                if (adv2Id && adv1Id === adv2Id) {
                    if (errorEl) {
                        errorEl.textContent = 'GVHD 1 và GVHD 2 không được trùng nhau.';
                        errorEl.style.display = 'block';
                    }
                    return;
                }

                submitBtn.disabled = true;
                try {
                    await App.fetchApi(`${App.getContextPath()}/api/lecturer/department-topics/${topicId}/assign-advisors`, {
                        method: 'POST',
                        body: JSON.stringify({
                            advisor1Id: parseInt(adv1Id),
                            advisor2Id: adv2Id ? parseInt(adv2Id) : null
                        })
                    });
                    document.getElementById('assign-advisor-dialog').close();
                    alert('Đã phân công Giảng viên hướng dẫn thành công!');
                    // Refresh topics
                    const status = document.getElementById('approval-status-filter')?.value || '';
                    const user = await App.getCurrentUser();
                    const deptId = (user && user.role === 'HEAD_OF_DEPT') ? ((user.department && user.department.id) || user.departmentId || '') : '';
                    const res = await App.fetchApi(`${App.getContextPath()}/api/lecturer/department-topics?departmentId=${deptId}&status=${status}`);
                    LecturerCrud.renderApprovalTable(res.data);
                } catch (err) {
                    if (errorEl) {
                        errorEl.textContent = err.message || 'Không thể lưu phân công GVHD.';
                        errorEl.style.display = 'block';
                    }
                } finally {
                    submitBtn.disabled = false;
                }
            });
        }
    },

    openAssignModal(id) {
        const t = this._approvalTopics.find(x => x.id === id);
        if (!t) return;

        const dialog = document.getElementById('assign-advisor-dialog');
        if (!dialog) return;

        document.getElementById('assign-topic-id').value = t.id;
        document.getElementById('assign-topic-title').textContent = `${t.topicCode} — ${t.title}`;

        const adv1Select = document.getElementById('assign-adv1');
        const adv2Select = document.getElementById('assign-adv2');

        const curAdv1Id = t.advisor1 ? t.advisor1.id : (t.createdBy ? t.createdBy.id : null);
        const curAdv2Id = t.advisor2 ? t.advisor2.id : null;

        adv1Select.innerHTML = '<option value="">-- Chọn Giảng viên hướng dẫn 1 --</option>' +
            this._lecturers.map(l => `<option value="${l.id}" ${curAdv1Id === l.id ? 'selected' : ''}>${escapeHtml(cleanLecturerName(l.fullName))}</option>`).join('');

        adv2Select.innerHTML = '<option value="">-- Không gán --</option>' +
            this._lecturers.map(l => `<option value="${l.id}" ${curAdv2Id === l.id ? 'selected' : ''}>${escapeHtml(cleanLecturerName(l.fullName))}</option>`).join('');

        const errorEl = document.getElementById('assign-dialog-error');
        if (errorEl) errorEl.style.display = 'none';

        dialog.showModal();
    },

    async approveTopic(id) {
        if (!confirm('Bạn có chắc chắn muốn DUYỆT đề tài này không?')) return;
        try {
            await App.fetchApi(`${App.getContextPath()}/api/lecturer/department-topics/${id}/approval`, {
                method: 'POST',
                body: JSON.stringify({ status: 'APPROVED' })
            });
            alert('Đã duyệt đề tài.');
            location.reload();
        } catch (e) {
            alert(e.message);
        }
    },

    async rejectTopic(id) {
        const reason = prompt('Nhập lý do từ chối đề tài:');
        if (reason === null) return;
        try {
            await App.fetchApi(`${App.getContextPath()}/api/lecturer/department-topics/${id}/approval`, {
                method: 'POST',
                body: JSON.stringify({ status: 'REJECTED', rejectionReason: reason })
            });
            alert('Đã từ chối đề tài.');
            location.reload();
        } catch (e) {
            alert(e.message);
        }
    },

    async loadStudentGroups() {
        try {
            const res = await App.fetchApi(App.getContextPath() + '/api/lecturer/student-groups');
            const groups = res.data;
            const tbody = document.getElementById('groups-tbody');
            if (!tbody) return;

            if (groups.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding: 24px; color: var(--text-muted);">Chưa có nhóm sinh viên đăng ký đề tài.</td></tr>';
                return;
            }

            tbody.innerHTML = groups.map(g => `
                <tr>
                    <td><strong>${escapeHtml(g.groupCode)}</strong></td>
                    <td>${escapeHtml(g.groupName)}</td>
                    <td><a href="detail.html?id=${g.topic.id}"><strong>${escapeHtml(g.topic.title)}</strong></a></td>
                    <td><strong>${escapeHtml(cleanLecturerName(g.leader ? g.leader.fullName : '—'))}</strong></td>
                    <td>${g.memberCount} sinh viên</td>
                    <td><span class="badge ${g.status.toLowerCase()}">${escapeHtml(lecturerLabel(g.status))}</span></td>
                    <td class="action-cell">
                        <div class="action-group">
                            <button onclick="LecturerCrud.viewReports(${g.id})" class="action-btn action-btn--view">Báo cáo</button>
                            ${g.status === 'PENDING' ? `
                                <button onclick="LecturerCrud.approveGroup(${g.id})" class="action-btn action-btn--unlock">Nhận nhóm</button>
                                <button onclick="LecturerCrud.rejectGroup(${g.id})" class="action-btn action-btn--delete">Từ chối</button>
                            ` : `
                                <span class="text-muted" style="font-size:12px;">${escapeHtml(g.notes || 'Đã xử lý')}</span>
                            `}
                        </div>
                    </td>
                </tr>
            `).join('');
        } catch (e) {
            alert(e.message);
        }
    },

    async viewReports(groupId) {
        try {
            const res = await App.fetchApi(`${App.getContextPath()}/api/lecturer/groups/${groupId}/reports`);
            const reports = res.data;
            let dialog = document.getElementById('lecturer-report-dialog');
            if (!dialog) {
                dialog = document.createElement('dialog');
                dialog.id = 'lecturer-report-dialog';
                dialog.style.maxWidth = '760px';
                dialog.style.width = 'calc(100% - 32px)';
                dialog.style.border = '0';
                dialog.style.borderRadius = '12px';
                dialog.style.padding = '24px';
                document.body.appendChild(dialog);
            }
            const groupName = reports[0]?.groupName || `Nhóm #${groupId}`;
            dialog.innerHTML = `<div style="display:flex;justify-content:space-between;gap:16px;align-items:center"><div><h2>Báo cáo — ${escapeHtml(groupName)}</h2><p style="color:#64748b">Chỉ GVHD của đề tài và Trưởng khoa được truy cập.</p></div><button class="button outline" onclick="document.getElementById('lecturer-report-dialog').close()">Đóng</button></div>` +
                (reports.length ? `<table class="data-table"><thead><tr><th>Tệp</th><th>Cột mốc</th><th>Thời gian nộp</th><th></th></tr></thead><tbody>${reports.map(r => `<tr><td><strong>${escapeHtml(r.filename)}</strong><br><small>${(Number(r.size || 0) / 1024).toFixed(1)} KB</small></td><td>${escapeHtml(r.stage)}</td><td>${new Date(r.submittedAt).toLocaleString('vi-VN')}</td><td><a class="button primary" href="${App.getContextPath()}/api/lecturer/reports/${r.id}/download">Tải về</a></td></tr>`).join('')}</tbody></table>` : '<p>Nhóm chưa nộp báo cáo.</p>');
            dialog.showModal();
        } catch (e) {
            alert(e.message);
        }
    },

    async approveGroup(id) {
        if (!confirm('Bạn có chắc đồng ý nhận nhóm sinh viên này?')) return;
        try {
            await App.fetchApi(`${App.getContextPath()}/api/lecturer/student-groups/${id}/approval`, {
                method: 'POST',
                body: JSON.stringify({ status: 'APPROVED', notes: 'Giảng viên đã chấp nhận nhóm.' })
            });
            alert('Đã duyệt nhận nhóm sinh viên.');
            location.reload();
        } catch (e) {
            alert(e.message);
        }
    },

    async rejectGroup(id) {
        const notes = prompt('Nhập ghi chú/lý do không nhận nhóm:');
        if (notes === null) return;
        try {
            await App.fetchApi(`${App.getContextPath()}/api/lecturer/student-groups/${id}/approval`, {
                method: 'POST',
                body: JSON.stringify({ status: 'REJECTED', notes: notes })
            });
            alert('Đã từ chối nhóm sinh viên.');
            location.reload();
        } catch (e) {
            alert(e.message);
        }
    }
};
