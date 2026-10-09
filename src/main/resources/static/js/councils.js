'use strict';

const escapeCouncil = value => String(value ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const c$ = s => document.querySelector(s);
let councilState = null;
let councilBusy = false;

async function councilApi(path, body, method) {
  const options = { credentials: 'same-origin', headers: { Accept: 'application/json' } };
  if (body !== undefined || method) {
    const tokenResponse = await fetch('/api/auth/csrf');
    const token = await tokenResponse.json();
    options.method = method || 'POST';
    options.headers['Content-Type'] = 'application/json';
    options.headers['X-XSRF-TOKEN'] = token.data;
    if (body !== undefined) options.body = JSON.stringify(body);
  }
  const response = await fetch(path, options);
  if (response.status === 401) {
    location.href = '/login.html';
    throw new Error('Phiên đăng nhập đã hết hạn.');
  }
  const data = await response.json();
  if (!response.ok) throw new Error(data.message || 'Không thể xử lý yêu cầu.');
  return data;
}

function councilMessage(message, error = false) {
  const e = c$('#feedback');
  if (!e) return;
  e.hidden = false;
  e.textContent = message;
  e.className = error ? 'alert danger' : 'alert success';
  setTimeout(() => {
    if (e) e.hidden = true;
  }, 5000);
}

const councilRole = role => ({
  CHAIRPERSON: 'Chủ tịch',
  SECRETARY: 'Thư ký',
  REVIEWER: 'Phản biện',
  MEMBER: 'Ủy viên'
}[role] || role);

function formatCouncilDate(value) {
  if (typeof formatDateTime === 'function') return formatDateTime(value);
  if (!value) return '—';
  try {
    const d = new Date(value);
    if (isNaN(d.getTime())) return String(value);
    return d.toLocaleString('vi-VN');
  } catch {
    return String(value);
  }
}

const cleanCouncilName = (name) => {
  if (!name) return '—';
  let c = String(name).replace(/\s*\((demo|test|sample)\)/gi, '').replace(/\b(demo|test|sample)\b/gi, '').trim();
  if (!c || c.toLowerCase() === 'admin' || c.toLowerCase() === 'admin demo' || c.includes('Tru?ng khoa')) return 'Nguyễn Văn Minh';
  return c;
};

async function loadCouncils() {
  councilState = await councilApi('/api/councils');
  const dean = councilState.role === 'DEAN';
  const adminActions = c$('#admin-actions');
  if (adminActions) adminActions.hidden = !dean;

  const countBadge = c$('#council-count');
  if (countBadge) countBadge.textContent = `${councilState.councils.length} hội đồng`;

  const councilList = c$('#council-list');
  if (councilList) {
    if (!councilState.councils.length) {
      councilList.innerHTML = '<div class="empty-state"><p class="text-muted" style="margin:0;">Chưa có hội đồng nào thuộc phạm vi quản lý của bạn.</p></div>';
    } else {
      councilList.innerHTML = '<div class="council-grid">' + councilState.councils.map(c => `
        <article class="council-card">
          <div class="council-card-header">
            <span class="badge course">${escapeCouncil(c.code)}</span>
            <span class="council-room">Phòng ${escapeCouncil(c.room)}</span>
          </div>
          <h3 class="council-name">${escapeCouncil(c.name)}</h3>
          <p class="council-time">Thời gian: ${formatCouncilDate(c.date)}</p>
          <div class="council-members-box">
            <div class="members-title">Thành viên hội đồng (${c.members.length}):</div>
            <ul class="council-members-list">
              ${c.members.map(m => `
                <li>
                  <span class="member-name">${escapeCouncil(cleanCouncilName(m.name))}</span>
                  <span class="badge ${m.role === 'CHAIRPERSON' ? 'approved' : m.role === 'REVIEWER' ? 'nckh' : 'outline'}">${escapeCouncil(councilRole(m.role))}</span>
                </li>
              `).join('')}
            </ul>
          </div>
          ${dean ? `<div class="actions"><button class="button secondary" data-edit-council="${c.id}">Sửa hội đồng</button><button class="button danger" data-delete-council="${c.id}">Xóa hội đồng</button></div>` : ''}
        </article>
      `).join('') + '</div>';
    }
  }

  const defenseList = c$('#defense-list');
  if (defenseList) {
    if (!councilState.defenses.length) {
      defenseList.innerHTML = '<div class="empty-state"><p class="text-muted" style="margin:0;">Chưa có nhóm nào được phân công cho hội đồng của bạn.</p></div>';
    } else {
      defenseList.innerHTML = '<div class="defense-stack">' + councilState.defenses.map(d => `
        <article class="defense-card">
          <div class="defense-card-header">
            <div>
              <span class="badge ${d.published ? 'approved' : d.finalized ? 'course' : 'pending'}">
                ${d.published ? 'Đã công bố kết quả' : d.finalized ? 'Đã tổng hợp điểm' : 'Đang chấm điểm'}
              </span>
              <h3 class="defense-title">${escapeCouncil(d.topic)}</h3>
            </div>
            <div class="defense-score-box">
              <span class="score-label">Điểm tổng hợp</span>
              <span class="score-val ${d.score != null ? 'has-score' : ''}">${d.score != null ? Number(d.score).toFixed(2) : '—'}</span>
            </div>
          </div>
          <div class="defense-meta-grid">
            <div class="meta-item"><span class="meta-label">Nhóm SV:</span> <strong>${escapeCouncil(d.groupName)}</strong></div>
            <div class="meta-item"><span class="meta-label">Hội đồng:</span> <strong>${escapeCouncil(d.council)}</strong></div>
            <div class="meta-item"><span class="meta-label">Phản biện:</span> <strong>${escapeCouncil(cleanCouncilName(d.reviewer))}</strong></div>
          </div>
          <details class="defense-details" ${d.reports.length ? 'open' : ''}>
            <summary>Báo cáo của nhóm (${d.reports.length})</summary>
            <div class="grades-list">
              ${d.reports.length ? d.reports.map(report => `
                <div class="grade-item">
                  <div class="grade-item-head">
                    <span class="evaluator-name">${escapeCouncil(report.filename)}</span>
                    <a class="button secondary" href="/api/council/reports/${report.id}/download" style="min-height:30px; padding:3px 10px; font-size:12px;">Tải báo cáo</a>
                  </div>
                  <p class="evaluator-comment">${escapeCouncil(report.stage)} · ${(Number(report.size || 0) / 1024).toFixed(1)} KB · ${formatCouncilDate(report.submittedAt)}</p>
                </div>
              `).join('') : '<p class="text-muted" style="margin:8px 0;">Nhóm chưa nộp báo cáo.</p>'}
            </div>
          </details>
          <details class="defense-details">
            <summary>Xem chi tiết ${d.grades.length} phiếu đánh giá</summary>
            <div class="grades-list">
              ${d.grades.length ? d.grades.map(g => `
                <div class="grade-item">
                  <div class="grade-item-head">
                    <span class="evaluator-name">${escapeCouncil(cleanCouncilName(g.name))}</span>
                    <span class="badge approved">${g.score} điểm</span>
                  </div>
                  <p class="evaluator-comment">${escapeCouncil(g.comment || 'Không có nhận xét.')}</p>
                </div>
              `).join('') : '<p class="text-muted" style="margin:8px 0;">Chưa có phiếu đánh giá nào được nộp.</p>'}
            </div>
          </details>
          <div class="defense-actions">
            ${d.canGrade ? `<button class="button primary" data-grade="${d.id}">Nhập / sửa điểm của tôi</button>` : ''}
            ${d.canFinalize ? `<button class="button secondary" data-finalize="${d.id}">Tổng hợp điểm hội đồng</button>` : ''}
            ${dean && d.finalized && !d.published ? `<button class="button primary" data-publish="${d.id}">Công bố kết quả</button>` : ''}
          </div>
        </article>
      `).join('') + '</div>';
    }
  }
}

function showCouncilDialog(title, html) {
  const dialog = c$('#council-dialog');
  dialog.innerHTML = `
    <div class="dialog-head">
      <h2 id="dialog-title">${title}</h2>
      <button type="button" data-close aria-label="Đóng">✕</button>
    </div>
    ${html}
    <p id="dialog-error" role="alert" class="alert danger" style="display:none; margin-top:16px;"></p>
  `;
  dialog.showModal();
}

const lecturerOptions = (selectedId) => (councilState?.lecturers || []).map(u => `<option value="${u.id}" ${String(u.id) === String(selectedId) ? 'selected' : ''}>${escapeCouncil(u.name)}</option>`).join('');

const reviewerOptions = councilId => {
  const council = (councilState?.councils || []).find(c => String(c.id) === String(councilId));
  return (council?.members || []).filter(m => m.role === 'REVIEWER')
    .map(m => `<option value="${m.id}">${escapeCouncil(m.name)}</option>`).join('');
};

document.addEventListener('click', async e => {
  const b = e.target.closest('button');
  if (!b || councilBusy) return;

  if (b.hasAttribute('data-close')) {
    return c$('#council-dialog').close();
  }

  if (b.id === 'create-council' || b.dataset.editCouncil) {
    const editing = b.dataset.editCouncil ? councilState.councils.find(c => String(c.id) === b.dataset.editCouncil) : null;
    showCouncilDialog(editing ? 'Sửa hội đồng bảo vệ' : 'Thành lập hội đồng bảo vệ', `
      <form id="council-form" data-id="${editing?.id || ''}">
        <div class="form-row">
          <label>Mã hội đồng
            <input name="code" value="${escapeCouncil(editing?.code || '')}" placeholder="VD: HD-CNPM-01" maxlength="30" required>
          </label>
          <label>Phòng báo cáo
            <input name="room" value="${escapeCouncil(editing?.room || '')}" placeholder="VD: A1-302" maxlength="80" required>
          </label>
        </div>
        <label>Tên hội đồng
          <input name="name" value="${escapeCouncil(editing?.name || '')}" placeholder="VD: Hội đồng bảo vệ Khóa luận tốt nghiệp CNPM" maxlength="150" required>
        </label>
        <label>Thời gian báo cáo
          <input name="defenseDate" type="datetime-local" value="${escapeCouncil(editing?.date?.slice(0,16) || '')}" required>
        </label>
        <div>
          <p style="font-weight:600; margin:10px 0 8px; font-size:13px; color:var(--navy);">Thành viên hội đồng (chọn 3–5 giảng viên):</p>
          ${[0, 1, 2, 3, 4].map(i => `
            <div class="member-input">
              <label>Giảng viên ${i + 1}
                <select name="member${i}" ${i < 3 ? 'required' : ''}>
                  <option value="">${i < 3 ? '-- Chọn giảng viên --' : '-- Không thêm --'}</option>
                  ${lecturerOptions(editing?.members[i]?.id)}
                </select>
              </label>
              <label>Vai trò
                <select name="role${i}">
                  <option value="CHAIRPERSON" ${(editing?.members[i]?.role || ['CHAIRPERSON','SECRETARY','REVIEWER','MEMBER','MEMBER'][i]) === 'CHAIRPERSON' ? 'selected' : ''}>Chủ tịch</option>
                  <option value="SECRETARY" ${(editing?.members[i]?.role || ['CHAIRPERSON','SECRETARY','REVIEWER','MEMBER','MEMBER'][i]) === 'SECRETARY' ? 'selected' : ''}>Thư ký</option>
                  <option value="REVIEWER" ${(editing?.members[i]?.role || ['CHAIRPERSON','SECRETARY','REVIEWER','MEMBER','MEMBER'][i]) === 'REVIEWER' ? 'selected' : ''}>Phản biện</option>
                  <option value="MEMBER" ${(editing?.members[i]?.role || ['CHAIRPERSON','SECRETARY','REVIEWER','MEMBER','MEMBER'][i]) === 'MEMBER' ? 'selected' : ''}>Ủy viên</option>
                </select>
              </label>
            </div>
          `).join('')}
        </div>
        <div style="display:flex; justify-content:flex-end; gap:10px; margin-top:16px;">
          <button type="button" class="button outline" data-close>Hủy</button>
          <button type="submit" class="button primary">Lưu hội đồng</button>
        </div>
      </form>
    `);

    const councilForm = c$('#council-form');
    councilForm?.addEventListener('change', () => {
      const errorEl = c$('#dialog-error');
      const fData = new FormData(councilForm);
      const members = [0, 1, 2, 3, 4]
        .filter(idx => fData.get('member' + idx))
        .map(idx => ({ userId: fData.get('member' + idx), role: fData.get('role' + idx) }));

      const userIds = members.map(m => m.userId);
      if (new Set(userIds).size !== userIds.length) {
        if (errorEl) {
          errorEl.textContent = 'Một giảng viên không thể xuất hiện hai lần trong cùng hội đồng.';
          errorEl.style.display = 'block';
        }
        return;
      }

      const chairs = members.filter(m => m.role === 'CHAIRPERSON').length;
      if (chairs > 1) {
        if (errorEl) {
          errorEl.textContent = 'Chỉ được phép có một Chủ tịch hội đồng.';
          errorEl.style.display = 'block';
        }
        return;
      }

      const secs = members.filter(m => m.role === 'SECRETARY').length;
      if (secs > 1) {
        if (errorEl) {
          errorEl.textContent = 'Chỉ được phép có một Thư ký hội đồng.';
          errorEl.style.display = 'block';
        }
        return;
      }

      if (errorEl && (errorEl.textContent.includes('Chủ tịch') || errorEl.textContent.includes('Thư ký') || errorEl.textContent.includes('hai lần'))) {
        errorEl.style.display = 'none';
      }
    });
  }

  if (b.dataset.deleteCouncil) {
    const council = councilState.councils.find(c => String(c.id) === b.dataset.deleteCouncil);
    showCouncilDialog('Xóa hội đồng', `<form id="delete-council-form" data-id="${council.id}"><p>Xóa hội đồng <b>${escapeCouncil(council.name)}</b>? Hội đồng đã phân công nhóm sẽ không thể xóa.</p><div class="actions"><button type="button" class="button secondary" data-close>Hủy</button><button type="submit" class="button danger">Xác nhận xóa</button></div></form>`);
  }

  if (b.id === 'assign-group') {
    if (!councilState.groups.length || !councilState.councils.length) {
      return councilMessage('Cần có nhóm sinh viên đã được duyệt và ít nhất một hội đồng.', true);
    }
    const firstCouncil = councilState.councils[0];
    showCouncilDialog('Phân công nhóm & Giảng viên phản biện', `
      <form id="assignment-form">
        <label>Nhóm sinh viên & Đề tài
          <select name="groupId" required>
            ${councilState.groups.map(g => `<option value="${g.id}">${escapeCouncil(g.name)}</option>`).join('')}
          </select>
        </label>
        <label>Hội đồng phản biện
          <select name="councilId" id="assignment-council" required>
            ${councilState.councils.map(c => `<option value="${c.id}">${escapeCouncil(c.name)} (${escapeCouncil(c.code)})</option>`).join('')}
          </select>
        </label>
        <label>Giảng viên phản biện (GVPB)
          <select name="reviewerId" id="assignment-reviewer" required>
            ${reviewerOptions(firstCouncil.id)}
          </select>
        </label>
        <p class="text-muted" style="font-size:12px; margin:4px 0 12px; line-height:1.5;">
          Danh sách chỉ hiển thị các giảng viên có vai trò Phản biện trong hội đồng đã chọn. Thành viên hội đồng không được trùng với Giảng viên hướng dẫn của đề tài.
        </p>
        <div style="display:flex; justify-content:flex-end; gap:10px; margin-top:16px;">
          <button type="button" class="button outline" data-close>Hủy</button>
          <button type="submit" class="button primary">Xác nhận phân công</button>
        </div>
      </form>
    `);
    c$('#assignment-council')?.addEventListener('change', event => {
      const reviewerSelect = c$('#assignment-reviewer');
      if (reviewerSelect) reviewerSelect.innerHTML = reviewerOptions(event.target.value);
    });
  }

  if (b.dataset.grade) {
    showCouncilDialog('Phiếu đánh giá bảo vệ của tôi', `
      <form id="grade-form" data-id="${b.dataset.grade}">
        <label>Điểm đánh giá (thang điểm 0 – 10)
          <input name="score" type="number" min="0" max="10" step="0.01" placeholder="VD: 8.50" required>
        </label>
        <label>Nhận xét / Đánh giá chi tiết
          <textarea name="comment" maxlength="2000" placeholder="Nhập nhận xét về nội dung báo cáo, tiến độ và chất lượng đề tài..." rows="4" required></textarea>
        </label>
        <div style="display:flex; justify-content:flex-end; gap:10px; margin-top:16px;">
          <button type="button" class="button outline" data-close>Hủy</button>
          <button type="submit" class="button primary">Lưu đánh giá</button>
        </div>
      </form>
    `);
  }

  if (b.dataset.finalize) {
    showCouncilDialog('Tổng hợp điểm hội đồng', `
      <form id="finalize-form" data-id="${b.dataset.finalize}">
        <p style="font-size:14px; line-height:1.6; color:var(--text); margin-bottom:18px;">
          Hệ thống sẽ tính điểm trung bình cộng từ tất cả phiếu đánh giá của các thành viên trong hội đồng và khóa chỉnh sửa điểm.
        </p>
        <div style="display:flex; justify-content:flex-end; gap:10px;">
          <button type="button" class="button outline" data-close>Hủy</button>
          <button type="submit" class="button primary">Xác nhận tổng hợp điểm</button>
        </div>
      </form>
    `);
  }

  if (b.dataset.publish) {
    showCouncilDialog('Công bố kết quả bảo vệ', `
      <form id="publish-form" data-id="${b.dataset.publish}">
        <p style="font-size:14px; line-height:1.6; color:var(--text); margin-bottom:18px;">
          Kết quả điểm tổng hợp sẽ được công bố chính thức để các thành viên trong nhóm sinh viên có thể tra cứu trực tuyến.
        </p>
        <div style="display:flex; justify-content:flex-end; gap:10px;">
          <button type="button" class="button outline" data-close>Hủy</button>
          <button type="submit" class="button success">Xác nhận công bố kết quả</button>
        </div>
      </form>
    `);
  }
});

document.addEventListener('submit', async e => {
  const form = e.target;
  if (!['council-form', 'assignment-form', 'grade-form', 'finalize-form', 'publish-form', 'delete-council-form'].includes(form.id)) return;
  e.preventDefault();
  if (councilBusy) return;
  councilBusy = true;

  const values = Object.fromEntries(new FormData(form));
  let path = '/api/councils';
  let body = values;
  let method;

  if (form.id === 'council-form') {
    if (form.dataset.id) { path += '/' + form.dataset.id; method = 'PUT'; }
    const errorEl = c$('#dialog-error');
    if (errorEl) errorEl.style.display = 'none';

    const selectedMembers = [0, 1, 2, 3, 4]
      .filter(i => values['member' + i])
      .map(i => ({ userId: Number(values['member' + i]), role: values['role' + i] }));

    if (selectedMembers.length < 3) {
      if (errorEl) {
        errorEl.textContent = 'Hội đồng phải có từ 3 đến 5 thành viên.';
        errorEl.style.display = 'block';
      }
      councilBusy = false;
      return;
    }

    const userIds = selectedMembers.map(m => m.userId);
    if (new Set(userIds).size !== userIds.length) {
      if (errorEl) {
        errorEl.textContent = 'Một giảng viên không thể xuất hiện hai lần trong cùng hội đồng.';
        errorEl.style.display = 'block';
      }
      councilBusy = false;
      return;
    }

    const chairCount = selectedMembers.filter(m => m.role === 'CHAIRPERSON').length;
    if (chairCount > 1) {
      if (errorEl) {
        errorEl.textContent = 'Chỉ được phép có một Chủ tịch hội đồng.';
        errorEl.style.display = 'block';
      }
      councilBusy = false;
      return;
    }
    if (chairCount === 0) {
      if (errorEl) {
        errorEl.textContent = 'Hội đồng phải có đúng một Chủ tịch.';
        errorEl.style.display = 'block';
      }
      councilBusy = false;
      return;
    }

    const secCount = selectedMembers.filter(m => m.role === 'SECRETARY').length;
    if (secCount > 1) {
      if (errorEl) {
        errorEl.textContent = 'Chỉ được phép có một Thư ký hội đồng.';
        errorEl.style.display = 'block';
      }
      councilBusy = false;
      return;
    }
    if (secCount === 0) {
      if (errorEl) {
        errorEl.textContent = 'Hội đồng phải có đúng một Thư ký.';
        errorEl.style.display = 'block';
      }
      councilBusy = false;
      return;
    }

    const reviewerCount = selectedMembers.filter(m => m.role === 'REVIEWER').length;
    if (reviewerCount < 1) {
      if (errorEl) {
        errorEl.textContent = 'Hội đồng cần có ít nhất một Giảng viên phản biện.';
        errorEl.style.display = 'block';
      }
      councilBusy = false;
      return;
    }

    body = {
      code: values.code,
      name: values.name,
      defenseDate: values.defenseDate,
      room: values.room,
      members: selectedMembers
    };
  }

  if (form.id === 'delete-council-form') {
    path += '/' + form.dataset.id; method = 'DELETE'; body = undefined;
  }

  if (form.id === 'assignment-form') {
    path += '/assignments';
    body = Object.fromEntries(Object.entries(values).map(([k, v]) => [k, Number(v)]));
  }

  if (form.id === 'grade-form') {
    path += '/defenses/' + form.dataset.id + '/grade';
    body = { score: Number(values.score), comment: values.comment };
  }

  if (form.id === 'finalize-form' || form.id === 'publish-form') {
    path += '/defenses/' + form.dataset.id + '/' + (form.id === 'finalize-form' ? 'finalize' : 'publish');
    body = {};
  }

  const submit = form.querySelector('button[type="submit"]');
  if (submit) submit.disabled = true;

  try {
    await councilApi(path, body, method);
    c$('#council-dialog').close();
    await loadCouncils();
    councilMessage('Đã lưu thay đổi thành công.');
  } catch (err) {
    const errorEl = c$('#dialog-error');
    if (errorEl) {
      errorEl.textContent = err.message;
      errorEl.style.display = 'block';
    }
  } finally {
    councilBusy = false;
    if (submit) submit.disabled = false;
  }
});

loadCouncils().catch(err => councilMessage(err.message, true));
