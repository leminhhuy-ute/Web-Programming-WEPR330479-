const LecturerCrud = {
    async initDashboard() {
        try {
            const res = await App.fetchApi(App.getContextPath() + '/api/lecturer/topics?myOnly=true');
            const topics = res.data;
            document.getElementById('stat-my-topics').textContent = topics.length;
            document.getElementById('stat-pending-topics').textContent = topics.filter(t => t.status === 'PENDING').length;
            document.getElementById('stat-approved-topics').textContent = topics.filter(t => t.status === 'APPROVED').length;

            const groupsRes = await App.fetchApi(App.getContextPath() + '/api/lecturer/student-groups');
            document.getElementById('stat-groups').textContent = groupsRes.data.length;

            this.renderRecentTopics(topics.slice(0, 5));
        } catch (e) {
            console.error(e);
        }
    },

    renderRecentTopics(topics) {
        const tbody = document.getElementById('recent-topics-tbody');
        if (!tbody) return;
        if (topics.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">Chưa có đề tài nào.</td></tr>';
            return;
        }

        tbody.innerHTML = topics.map(t => `
            <tr>
                <td><strong>${t.topicCode}</strong></td>
                <td><a href="detail.html?id=${t.id}">${t.title}</a></td>
                <td><span class="badge ${t.topicType.toLowerCase()}">${t.topicType}</span></td>
                <td><span class="badge ${t.status.toLowerCase()}">${t.status}</span></td>
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
                    depts.data.map(d => `<option value="${d.id}">${d.name} (${d.code})</option>`).join('');
            }

            const periods = await App.fetchApi(App.getContextPath() + '/api/common/registration-periods');
            const periodSelect = document.getElementById('filter-period');
            if (periodSelect) {
                periodSelect.innerHTML = '<option value="">-- Tất cả Đợt đăng ký --</option>' +
                    periods.data.map(p => `<option value="${p.id}">${p.name}</option>`).join('');
            }
        } catch (e) {
            console.error(e);
        }
    },

    async loadTopicList() {
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
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Không tìm thấy đề tài phù hợp.</td></tr>';
            return;
        }

        tbody.innerHTML = topics.map(t => `
            <tr>
                <td><strong>${t.topicCode}</strong></td>
                <td><a href="detail.html?id=${t.id}"><strong>${t.title}</strong></a></td>
                <td>${t.createdBy ? t.createdBy.fullName : '—'}</td>
                <td><span class="badge ${t.topicType.toLowerCase()}">${t.topicType}</span></td>
                <td>${t.department ? t.department.code : '—'}</td>
                <td><span class="badge ${t.status.toLowerCase()}">${t.status}</span></td>
                <td>
                    <a href="detail.html?id=${t.id}" class="button outline">Chi tiết</a>
                    <a href="form.html?id=${t.id}" class="button primary">Sửa</a>
                    ${t.status === 'PENDING' ? `<button onclick="LecturerCrud.deleteTopic(${t.id})" class="button danger">Xóa</button>` : ''}
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
            deptSelect.innerHTML = depts.data.map(d => `<option value="${d.id}">${d.name} (${d.code})</option>`).join('');
        }

        const periods = await App.fetchApi(App.getContextPath() + '/api/common/registration-periods');
        const periodSelect = document.getElementById('form-period');
        if (periodSelect) {
            periodSelect.innerHTML = periods.data.map(p => `<option value="${p.id}">${p.name}</option>`).join('');
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
            document.getElementById('detail-type').textContent = t.topicType;
            document.getElementById('detail-type').className = `badge ${t.topicType.toLowerCase()}`;
            document.getElementById('detail-status').textContent = t.status;
            document.getElementById('detail-status').className = `badge ${t.status.toLowerCase()}`;
            document.getElementById('detail-creator').textContent = t.createdBy ? t.createdBy.fullName : '—';
            document.getElementById('detail-dept').textContent = t.department ? t.department.name : '—';
            document.getElementById('detail-period').textContent = t.period ? t.period.name : '—';
            document.getElementById('detail-max').textContent = t.maxStudents + ' sinh viên';
            document.getElementById('detail-desc').textContent = t.description || 'Chưa có mô tả.';
            document.getElementById('detail-req').textContent = t.requirements || 'Chưa có yêu cầu đặc biệt.';
            document.getElementById('detail-adv1').textContent = t.advisor1 ? t.advisor1.fullName : 'Chưa gán';
            document.getElementById('detail-adv2').textContent = t.advisor2 ? t.advisor2.fullName : 'Chưa gán';

            if (t.rejectionReason) {
                document.getElementById('detail-reject-box').style.display = 'block';
                document.getElementById('detail-reject-reason').textContent = t.rejectionReason;
            }

            document.getElementById('btn-edit').href = `form.html?id=${t.id}`;
        } catch (e) {
            alert(e.message);
        }
    },

    async loadApprovalPage() {
        const user = await App.getCurrentUser();
        const deptId = user.department ? user.department.id : '';

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

    async renderApprovalTable(topics) {
        const tbody = document.getElementById('approval-tbody');
        if (!tbody) return;
        if (topics.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Không có đề tài cần quản lý.</td></tr>';
            return;
        }

        // Fetch lecturers for assignment dropdown
        const lecturersRes = await App.fetchApi(App.getContextPath() + '/api/common/lecturers');
        const lecturers = lecturersRes.data;

        tbody.innerHTML = topics.map(t => {
            const adv1Options = lecturers.map(l => `<option value="${l.id}" ${t.advisor1 && t.advisor1.id === l.id ? 'selected' : ''}>${l.fullName}</option>`).join('');
            const adv2Options = `<option value="">-- Không gán --</option>` + lecturers.map(l => `<option value="${l.id}" ${t.advisor2 && t.advisor2.id === l.id ? 'selected' : ''}>${l.fullName}</option>`).join('');

            return `
            <tr>
                <td><strong>${t.topicCode}</strong></td>
                <td><a href="detail.html?id=${t.id}"><strong>${t.title}</strong></a></td>
                <td>${t.createdBy ? t.createdBy.fullName : '—'}</td>
                <td><span class="badge ${t.topicType.toLowerCase()}">${t.topicType}</span></td>
                <td><span class="badge ${t.status.toLowerCase()}">${t.status}</span></td>
                <td>
                    <div style="display:flex; flex-direction:column; gap:4px;">
                        <select id="adv1-${t.id}" style="padding:4px; font-size:12px;">${adv1Options}</select>
                        <select id="adv2-${t.id}" style="padding:4px; font-size:12px;">${adv2Options}</select>
                        <button onclick="LecturerCrud.assignAdvisors(${t.id})" class="button outline" style="padding:2px 6px; font-size:11px;">Gán GVHD</button>
                    </div>
                </td>
                <td>
                    ${t.status === 'PENDING' ? `
                        <button onclick="LecturerCrud.approveTopic(${t.id})" class="button success" style="padding:4px 8px;">Duyệt</button>
                        <button onclick="LecturerCrud.rejectTopic(${t.id})" class="button danger" style="padding:4px 8px;">Từ chối</button>
                    ` : `
                        <button onclick="LecturerCrud.approveTopic(${t.id})" class="button outline" style="padding:4px 8px;">Đổi Duyệt</button>
                        <button onclick="LecturerCrud.rejectTopic(${t.id})" class="button outline" style="padding:4px 8px;">Đổi Từ chối</button>
                    `}
                </td>
            </tr>
            `;
        }).join('');
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

    async assignAdvisors(id) {
        const adv1Id = document.getElementById(`adv1-${id}`).value;
        const adv2Id = document.getElementById(`adv2-${id}`).value;

        if (!adv1Id) {
            alert('Vui lòng chọn Giảng viên hướng dẫn 1.');
            return;
        }

        try {
            await App.fetchApi(`${App.getContextPath()}/api/lecturer/department-topics/${id}/assign-advisors`, {
                method: 'POST',
                body: JSON.stringify({
                    advisor1Id: parseInt(adv1Id),
                    advisor2Id: adv2Id ? parseInt(adv2Id) : null
                })
            });
            alert('Đã gán Giảng viên hướng dẫn thành công!');
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
                tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Chưa có nhóm sinh viên đăng ký đề tài.</td></tr>';
                return;
            }

            tbody.innerHTML = groups.map(g => `
                <tr>
                    <td><strong>${g.groupCode}</strong></td>
                    <td>${g.groupName}</td>
                    <td><a href="detail.html?id=${g.topic.id}">${g.topic.title}</a> (${g.topic.topicCode})</td>
                    <td><strong>${g.leader ? g.leader.fullName : '—'}</strong> (${g.leader ? g.leader.userCode : ''})</td>
                    <td>${g.memberCount} sinh viên</td>
                    <td><span class="badge ${g.status.toLowerCase()}">${g.status}</span></td>
                    <td>
                        ${g.status === 'PENDING' ? `
                            <button onclick="LecturerCrud.approveGroup(${g.id})" class="button success" style="padding:4px 8px;">Nhận nhóm</button>
                            <button onclick="LecturerCrud.rejectGroup(${g.id})" class="button danger" style="padding:4px 8px;">Từ chối</button>
                        ` : `
                            <span style="font-size:12px; color:#666;">${g.notes || 'Đã xử lý'}</span>
                        `}
                    </td>
                </tr>
            `).join('');
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
