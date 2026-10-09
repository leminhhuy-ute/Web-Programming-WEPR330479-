'use strict';
const $ = (s) => document.querySelector(s);
const esc = (v) => String(v ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const cleanName = (v) => String(v ?? '').replace(/\s*\((demo|test|example|sample)\)/gi, '').replace(/\b(demo|test|example|sample)\b/gi, '').trim();

const paths = {
  grid: '<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>',
  users: '<circle cx="9" cy="8" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3M16 5a3 3 0 0 1 0 6M21 21v-3a6 6 0 0 0-3-5"/>',
  search: '<circle cx="10" cy="10" r="6"/><path d="m15 15 6 6"/>',
  book: '<path d="M4 4h6a3 3 0 0 1 2 1 3 3 0 0 1 2-1h6v16h-6a3 3 0 0 0-2 1 3 3 0 0 0-2-1H4zM12 5v16"/>',
  upload: '<path d="M12 16V3m-5 5 5-5 5 5M4 15v5h16v-5"/>',
  logout: '<path d="M9 4H4v16h5M10 12h11m-4-4 4 4-4 4"/>',
  arrow: '<path d="M4 12h16m-6-6 6 6-6 6"/>',
  clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
  check: '<path d="m5 12 4 4L19 6"/>',
  info: '<circle cx="12" cy="12" r="9"/><path d="M12 11v6M12 7v1"/>',
  file: '<path d="M5 3h9l5 5v13H5zM14 3v6h5M8 13h8M8 17h6"/>'
};

const icon = name => `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${paths[name] || paths.file}</svg>`;
document.querySelectorAll('[data-icon]').forEach(e => e.innerHTML = icon(e.dataset.icon));

const date = v => v == null ? 'Chưa thiết lập' : new Intl.DateTimeFormat('vi-VN', {dateStyle:'medium', timeStyle:'short'}).format(new Date(v));
const statusText = {DRAFT:'Bản nháp', PENDING:'Chờ duyệt', APPROVED:'Đã được duyệt', REJECTED:'Bị từ chối', CANCELLED:'Đã hủy', ACCEPTED:'Đã tham gia', DECLINED:'Đã từ chối'};
const badge = (s, text) => `<span class="badge ${esc(s)}">${esc(text || statusText[s] || s)}</span>`;
const initials = name => (name || 'SV').split(' ').filter(Boolean).slice(-2).map(x => x[0]).join('').toUpperCase();

let result = {published: false}, announcements = [], periodChoices = [], resultPeriods = [], resultPeriodId = '';
let state, catalog = [], catalogMeta = {page: 0, totalElements: 0, totalPages: 0, first: true, last: true}, page = 'dashboard', toastTimer, busy = false;
let filters = {q: '', department: '', type: ''};

async function api(path, body, method) {
  const headers = {};
  const options = {headers, credentials: 'same-origin'};
  if (body !== undefined) {
    options.method = method || 'POST';
    headers[$('meta[name="_csrf_header"]').content] = $('meta[name="_csrf"]').content;
    if (body instanceof FormData) options.body = body;
    else {
      headers['Content-Type'] = 'application/json';
      options.body = JSON.stringify(body);
    }
  }
  const response = await fetch('/api/student' + path, options);
  if (response.status === 401) {
    location.href = '/login.html';
    throw new Error('Phiên đăng nhập đã hết hạn.');
  }
  const data = await response.json().catch(() => ({message: 'Không thể xử lý yêu cầu. Vui lòng thử lại.'}));
  if (!response.ok) throw new Error(data.message || 'Bạn không có quyền thực hiện thao tác này.');
  return data;
}

function toast(text, error = false) {
  const t = $('#toast');
  t.textContent = text;
  t.className = error ? 'error' : '';
  t.hidden = false;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => t.hidden = true, 6000);
}

function isLeader() { return state.group?.leaderId === state.student.id; }
function locked() { return state.registration && !['REJECTED', 'CANCELLED'].includes(state.registration.status); }

function heading(title, desc, action = '') {
  return `<header class="page-header"><div><h1>${title}</h1><p class="page-subtitle">${desc}</p></div>${action ? `<div class="header-actions">${action}</div>` : ''}</header>`;
}

function empty(title, text, action = '', ico = 'users') {
  return `<div class="empty"><div class="empty-icon">${icon(ico)}</div><h3>${title}</h3><p>${text}</p>${action}</div>`;
}

function linkButton(hash, label) {
  return `<button type="button" data-go="${hash}" class="button primary">${label} ${icon('arrow')}</button>`;
}

function groupMembers(compact = false) {
  const canManage = !compact && isLeader() && !locked();
  return state.group.members.map((m, i) => {
    const memName = cleanName(m.name);
    const isSelf = m.id === state.student.id;
    return `<div class="member"><span class="avatar ${i%3===1?'green':i%3===2?'purple':''}">${esc(initials(memName))}</span><div class="grow"><b>${esc(memName)}${isSelf?' <span class="muted">(Bạn)</span>':''}</b><small>${esc(m.id)} · ${esc(m.className)}</small></div>${m.id===state.group.leaderId?badge('APPROVED','Nhóm trưởng'):badge('MEMBER','Thành viên')}${canManage&&!isSelf?`<div class="member-actions"><button type="button" class="button small secondary" data-transfer="${esc(m.id)}">Chọn làm nhóm trưởng</button><button type="button" class="button small danger" data-remove-member="${esc(m.id)}">Xóa</button></div>`:''}</div>`;
  }).join('');
}

function stepList() {
  const n = !state.group ? 0 : !state.registration ? 1 : ['PENDING', 'REJECTED', 'CANCELLED'].includes(state.registration.status) ? 2 : state.reports.length ? 4 : 3;
  return `<ol class="steps">${['Tạo nhóm sinh viên', 'Đăng ký đề tài', 'Chờ giảng viên phê duyệt', 'Thực hiện & nộp báo cáo'].map((s, i) => `<li class="${i<n?'done':i===n?'current':''}"><span class="step-number">${i<n?'✓':i+1}</span><div><b>${s}</b><small>${i<n?'Đã hoàn thành':i===n?'Bước tiếp theo của bạn':'Chưa bắt đầu'}</small></div></li>`).join('')}</ol>`;
}

function dashboard() {
  const r = state.registration;
  const next = !state.group ? 'group' : !r ? 'topics' : r.status === 'APPROVED' ? 'reports' : 'project';
  const stName = cleanName(state.student.name);
  const firstName = stName.split(' ').slice(-2).join(' ') || 'bạn';
  return heading(`Chào bạn, ${esc(firstName)}!`, 'Không gian quản lý nhóm, đề tài và báo cáo học vụ.') +
    `<div class="grid metrics">
      <div class="panel metric">
        <span class="metric-icon">${icon('users')}</span>
        <div><small>NHÓM SINH VIÊN</small><strong>${state.group?`${state.group.members.length} / 3 thành viên`:'Chưa tham gia'}</strong></div>
      </div>
      <div class="panel metric">
        <span class="metric-icon">${icon('book')}</span>
        <div><small>ĐỀ TÀI CỦA NHÓM</small><strong>${r?statusText[r.status]:'Chưa đăng ký'}</strong></div>
      </div>
      <div class="panel metric">
        <span class="metric-icon">${icon('file')}</span>
        <div><small>BÁO CÁO ĐÃ NỘP</small><strong>${state.reports.length} báo cáo</strong></div>
      </div>
    </div>
    <section class="hero panel">
      <div>
        <span class="eyebrow">HÀNH TRÌNH ĐỀ TÀI CỦA BẠN</span>
        <h2>${!state.group?'Bắt đầu từ một nhóm phù hợp':!r?'Nhóm đã sẵn sàng, hãy chọn đề tài':r.status==='APPROVED'?'Sẵn sàng cho cột mốc tiếp theo':'Theo dõi kết quả đăng ký của nhóm'}</h2>
        <p>${!state.group?'Tạo nhóm, mời bạn cùng lớp và cùng nhau lựa chọn đề tài yêu thích.':r?esc(r.topic.title):'Khám phá các đề tài đã công bố, tìm hiểu yêu cầu và gửi đăng ký cho nhóm.'}</p>
      </div>
      ${linkButton(next,!state.group?'Tạo nhóm ngay':!r?'Khám phá đề tài':r.status==='APPROVED'?'Nộp báo cáo':'Xem trạng thái')}
    </section>
    <div class="grid cols">
      <section class="panel">
        <div class="panel-head"><h2>Nhóm của tôi</h2><a href="#group">Quản lý nhóm →</a></div>
        ${state.group?`<div class="panel-head"><h3>${esc(state.group.name)}</h3>${badge('MEMBER',`${state.group.members.length}/3 thành viên`)}</div>${groupMembers(true)}`:empty('Bạn chưa có nhóm','Tạo một nhóm mới hoặc chấp nhận lời mời từ bạn cùng lớp.',linkButton('group','Đến quản lý nhóm'))}
      </section>
      <section class="panel">
        <div class="panel-head"><h2>Tiến trình thực hiện</h2></div>
        ${stepList()}
      </section>
    </div>
    <section class="panel section-gap">
      <div class="panel-head"><h2>Thông tin cần lưu ý</h2>${badge('MEMBER','Quy chế đào tạo')}</div>
      <div class="grid cols">
        <div><h3>Mỗi sinh viên tham gia một nhóm</h3><p class="muted">Nhóm có tối đa 3 thành viên và một nhóm trưởng. Nhóm trưởng thay mặt nhóm đăng ký đề tài và nộp báo cáo.</p></div>
        <div><h3>Kiểm tra thời gian đăng ký</h3><p class="muted">Chỉ đăng ký trong thời gian mở của đề tài. Kết quả xét duyệt được hiển thị tại mục Đề tài đang thực hiện.</p></div>
      </div>
    </section>`;
}

function groupPage() {
  const leader = isLeader();
  const isLocked = locked();
  const headerAction = state.group && !isLocked
    ? (leader
        ? `<div style="display:flex;gap:8px;align-items:center;"><button type="button" class="button primary" data-action="invite">+ Mời thành viên</button><button type="button" class="button danger" data-action="disband-group">Giải tán nhóm</button></div>`
        : `<button type="button" class="button danger" data-action="leave-group">Rời nhóm</button>`)
    : '';

  return heading('Nhóm của tôi', 'Kết nối thành viên và chuẩn bị cho đề tài của nhóm.', headerAction) +
    `<section class="panel section-gap"><p><b>Đợt của nhóm:</b> ${esc(state.group?.periodName || 'Chưa tham gia nhóm')}</p>${state.group && periodChoices.some(p => p.open && !p.joined) ? '<button type="button" class="button secondary" data-action="create">Tạo nhóm trong đợt khác</button>' : ''}</section>` +
    `<div class="grid cols">
      <section class="panel">
        <div class="panel-head"><h2>${state.group?esc(state.group.name):'Tạo nhóm sinh viên'}</h2>${state.group?badge('MEMBER',`${state.group.members.length}/3 thành viên`):''}</div>
        ${state.group?`${groupMembers()}${isLocked?`<div class="hint">${icon('info')}Danh sách thành viên đã khóa vì nhóm đã đăng ký đề tài.</div>`:`<div class="hint">${icon('info')}Còn ${3-state.group.members.length} chỗ trống. Thành viên được thêm khi chấp nhận lời mời.</div>`}<div class="actions">${linkButton('topics','Tiếp tục chọn đề tài')}${!isLocked?(leader?'<button type="button" class="button danger" data-action="disband-group">Giải tán nhóm</button>':'<button type="button" class="button danger" data-action="leave-group">Rời nhóm</button>'):''}</div>`:empty('Cùng nhau bắt đầu','Người tạo nhóm sẽ là nhóm trưởng đầu tiên. Bạn có thể chuyển quyền cho một thành viên trong nhóm.','<button type="button" class="button primary" data-action="create">+ Tạo nhóm mới</button>')}
      </section>
      <section class="panel">
        <div class="panel-head"><h2>Lời mời dành cho bạn</h2>${badge('MEMBER',state.invitations.length+' lời mời')}</div>
        ${state.invitations.length?`<div class="stack">${state.invitations.map(i=>`<div class="invitation"><h3>${esc(i.groupName)}</h3><small class="muted">Được mời vào ${date(i.createdAt)}</small><div class="actions"><button type="button" class="button primary small" data-respond="${i.id}" data-accept="true">Tham gia nhóm</button><button type="button" class="button secondary small" data-respond="${i.id}" data-accept="false">Từ chối</button></div></div>`).join('')}</div>`:empty('Chưa có lời mời','Khi một nhóm mời bạn tham gia, lời mời sẽ xuất hiện tại đây.','','users')}
      </section>
    </div>
    ${state.group?`<section class="panel section-gap"><div class="panel-head"><h2>Lời mời của nhóm</h2></div>${state.group.invitations.length?`<div class="table-wrap"><table><thead><tr><th>SINH VIÊN</th><th>MSSV</th><th>THỜI GIAN</th><th>TRẠNG THÁI</th></tr></thead><tbody>${state.group.invitations.map(i=>`<tr><td>${esc(cleanName(i.studentName))}</td><td>${esc(i.studentId)}</td><td>${date(i.createdAt)}</td><td>${badge(i.status,i.status==='PENDING'?'Chờ xác nhận':null)}</td></tr>`).join('')}</tbody></table></div>`:'<p class="muted">Nhóm chưa gửi lời mời nào.</p>'}</section>`:''}`;
}

function topicsPage() {
  return heading('Kho đề tài', 'Tìm đề tài phù hợp với định hướng và thế mạnh của nhóm.') +
    `<section class="panel">
      <form id="filter-form" class="filters">
        <label>Tìm kiếm<input name="q" value="${esc(filters.q)}" placeholder="Tên đề tài, mã đề tài, giảng viên…"></label>
        <label>Bộ môn<select name="department">${['',...new Set(catalog.map(item=>item.topic.department)),...(filters.department?[filters.department]:[])].filter((v,i,a)=>a.indexOf(v)===i).map(x=>`<option value="${esc(x)}" ${x===filters.department?'selected':''}>${x||'Tất cả bộ môn'}</option>`).join('')}</select></label>
        <label>Loại đề tài<select name="type">${['','Môn học','NCKH','TLCN','KLTN'].map(x=>`<option value="${esc(x)}" ${x===filters.type?'selected':''}>${x||'Tất cả loại'}</option>`).join('')}</select></label>
        <button type="submit" class="button primary">${icon('search')}Tìm kiếm</button>
      </form>
      <div class="panel-head">
        <small class="muted">${catalogMeta.totalElements} đề tài đã công bố</small>
        <button type="button" class="text-button" data-action="reset-filter">Xóa bộ lọc</button>
      </div>
      ${catalog.length?`<div class="table-wrap"><table class="topics-table"><colgroup><col class="col-topic" style="width:44%;"><col class="col-dept" style="width:18%;"><col class="col-supervisor" style="width:18%;"><col class="col-reg" style="width:11%;"><col class="col-action" style="width:9%;"></colgroup><thead><tr><th style="width:44%;">ĐỀ TÀI</th><th style="width:18%;">BỘ MÔN / LOẠI</th><th style="width:18%;">GIẢNG VIÊN HƯỚNG DẪN</th><th style="width:11%;">ĐĂNG KÝ</th><th style="width:9%;text-align:center;">THAO TÁC</th></tr></thead><tbody>${catalog.map(({topic:t,registeredGroups,open})=>`<tr><td class="title-cell"><span class="topic-code">${esc(t.id)}</span><button type="button" class="text-button" data-topic="${esc(t.id)}"><b>${esc(t.title)}</b></button><small>${esc(t.technologies)}</small></td><td class="dept-cell"><b>${esc(t.department)}</b><small>${esc(t.type)}</small></td><td class="supervisor-cell">${esc(cleanName(t.supervisor))}</td><td class="reg-cell">${badge(open?'APPROVED':'MEMBER',open?'Đang mở':'Đã đóng')}<small>${registeredGroups||0} nhóm đã đăng ký</small></td><td class="action-cell"><button type="button" class="button secondary small" data-topic="${esc(t.id)}" aria-label="Xem chi tiết ${esc(t.title)}">Chi tiết</button></td></tr>`).join('')}</tbody></table></div>`:empty('Không tìm thấy đề tài','Thử một từ khóa khác hoặc xóa bộ lọc để xem toàn bộ danh sách.','','search')}
      <div class="actions">
        <button type="button" class="button secondary small" data-action="catalog-prev" ${catalogMeta.first?'disabled':''}>← Trang trước</button>
        <span class="muted">Trang ${catalogMeta.totalPages?catalogMeta.page+1:0} / ${catalogMeta.totalPages}</span>
        <button type="button" class="button secondary small" data-action="catalog-next" ${catalogMeta.last?'disabled':''}>Trang sau →</button>
      </div>
    </section>
    <div class="hint">${icon('info')}Chỉ nhóm trưởng được đăng ký. Nhiều nhóm có thể chọn cùng một đề tài trong thời gian đăng ký.</div>`;
}

function topicBody(t) {
  return `<span class="topic-code">${esc(t.id)}</span><h2>${esc(t.title)}</h2><div class="tags">${(t.technologies||'').split(' · ').map(x=>`<span>${esc(x)}</span>`).join('')}</div><p class="project-description">${esc(t.description)}</p><div class="project-meta"><div><small>Đợt đăng ký</small><strong>${esc(t.periodName)}</strong></div><div><small>Số sinh viên tối đa</small><strong>${t.capacity}</strong></div><div><small>Giảng viên hướng dẫn</small><strong>${esc(cleanName(t.supervisor))}</strong></div><div><small>Bộ môn / Loại</small><strong>${esc(t.department)} · ${esc(t.type)}</strong></div><div><small>Thời gian mở đăng ký</small><strong>${date(t.opensAt)}</strong></div><div><small>Hạn đăng ký</small><strong>${date(t.closesAt)}</strong></div></div>`;
}

function registrationTimeline() {
  const items = (state.registrationHistory || []).flatMap(r => r.history || []);
  return `<section class="panel section-gap">
    <div class="panel-head"><h2>Lịch sử đăng ký đề tài</h2>${badge('MEMBER', items.length + ' thay đổi')}</div>
    ${items.length ? `<div class="table-wrap"><table><thead><tr><th>ĐỀ TÀI</th><th>THAY ĐỔI</th><th>NGƯỜI THỰC HIỆN</th><th>THỜI GIAN</th><th>GHI CHÚ</th></tr></thead><tbody>${items.map(h => `<tr><td><b>${esc(h.topicCode)}</b><small>${esc(h.topicTitle)}</small></td><td>${h.oldStatus ? badge(h.oldStatus) : 'Khởi tạo'} → ${badge(h.newStatus)}</td><td>${esc(cleanName(h.changedBy))}</td><td>${date(h.changedAt)}</td><td>${esc(h.note || '—')}</td></tr>`).join('')}</tbody></table></div>` : empty('Chưa có lịch sử', 'Các lần đăng ký, duyệt, từ chối hoặc hủy sẽ được lưu tại đây.', '', 'clock')}
  </section>`;
}

function projectPage() {
  const r = state.registration;
  if (!r) return heading('Đề tài đang thực hiện', 'Thông tin đăng ký và kết quả phê duyệt của nhóm.') + `<section class="panel">${empty('Nhóm chưa đăng ký đề tài', 'Khám phá kho đề tài và gửi đăng ký để bắt đầu hành trình.', linkButton('topics', 'Đến kho đề tài'), 'book')}</section>` + registrationTimeline();
  const canCancel = isLeader() && ['PENDING', 'APPROVED'].includes(r.status) && new Date() <= new Date(r.topic.closesAt);
  const primary = r.status === 'APPROVED' ? linkButton('reports', 'Đến nộp báo cáo') : ['REJECTED', 'CANCELLED'].includes(r.status) ? linkButton('topics', 'Chọn lại đề tài') : '';
  return heading('Đề tài đang thực hiện', 'Thông tin đăng ký và kết quả phê duyệt của nhóm.') +
    `<div class="grid cols">
      <section class="panel">
        <div class="panel-head">${badge(r.status)}<small class="muted">Đăng ký: ${date(r.submittedAt)}</small></div>
        ${topicBody(r.topic)}
        ${r.feedback ? `<div class="notice"><b>Phản hồi của giảng viên</b><br>${esc(r.feedback)}</div>` : ''}
        <div class="actions">
          ${primary}
          ${canCancel ? '<button type="button" class="button danger small" data-action="cancel-registration">Hủy đăng ký</button>' : ''}
        </div>
      </section>
      <section class="panel">
        <h2>Tiến trình của nhóm</h2>
        ${stepList()}
        <div class="hint">${icon('info')}${r.status==='PENDING'?'Đăng ký đã được gửi. Kết quả sẽ được cập nhật sau khi giảng viên xét duyệt.':r.status==='CANCELLED'?'Đăng ký đã hủy nhưng toàn bộ lịch sử vẫn được lưu. Nhóm có thể chọn đề tài khác trong thời hạn.':'Tất cả thành viên được xem đề tài và lịch sử nộp báo cáo của nhóm.'}</div>
      </section>
    </div>
    ${registrationTimeline()}`;
}

function reportsPage() {
  const allowed = isLeader() && state.registration?.status === 'APPROVED';
  return heading('Nộp báo cáo', 'Lưu lại từng cột mốc trong quá trình thực hiện đề tài.') +
    `<div class="grid cols">
      <section class="panel">
        <h2>Báo cáo của nhóm</h2>
        ${state.registration ? `<p class="muted">${esc(state.registration.topic.title)}</p>` : ''}
        ${allowed ? `<form id="report-form">
          <label>Loại báo cáo
            <select name="stage">
              <option>Đề cương</option>
              <option>Giữa kỳ</option>
              <option>Cuối kỳ</option>
            </select>
          </label>
          <div class="upload-zone">
            ${icon('upload')}
            <h3>Chọn tệp báo cáo của nhóm</h3>
            <p>Định dạng PDF hoặc DOCX · Tối đa 10 MB</p>
            <label>Tệp báo cáo<input type="file" name="file" accept=".pdf,.docx" required></label>
          </div>
          <label>Ghi chú cho giảng viên<textarea name="note" maxlength="1000" placeholder="Tóm tắt nội dung cập nhật hoặc lưu ý…"></textarea></label>
          <button type="submit" class="button primary">${icon('upload')}Nộp báo cáo</button>
        </form>` : empty('Chưa thể nộp báo cáo', !state.group ? 'Bạn cần tham gia một nhóm.' : !isLeader() ? 'Chỉ nhóm trưởng được nộp báo cáo. Bạn vẫn có thể xem và tải các báo cáo của nhóm.' : 'Đề tài cần được phê duyệt trước khi nộp báo cáo.', '', 'upload')}
      </section>
      <section class="panel">
        <h2>Thông tin nộp bài</h2>
        <div class="project-meta" style="grid-template-columns:1fr">
          <div><small>Nhóm sinh viên</small><strong>${esc(state.group?.name || 'Chưa có nhóm')}</strong></div>
          <div><small>Quyền nộp bài của bạn</small><strong>${isLeader() ? 'Nhóm trưởng' : 'Thành viên'}</strong></div>
        </div>
        <div class="hint">${icon('info')}Mỗi lần nộp được lưu riêng trong lịch sử của nhóm. Chỉ nhóm trưởng được quyền gửi tệp mới.</div>
      </section>
    </div>
    <section class="panel section-gap">
      <div class="panel-head"><h2>Lịch sử nộp báo cáo</h2>${badge('MEMBER', state.reports.length + ' tệp')}</div>
      ${state.reports.length ? `<div class="table-wrap"><table><thead><tr><th>TÊN TỆP</th><th>LOẠI</th><th>THỜI GIAN NỘP</th><th>TRẠNG THÁI</th><th>THAO TÁC</th></tr></thead><tbody>${state.reports.map(r => `<tr><td><b>${esc(r.filename)}</b><small>${(r.size/1024).toFixed(1)} KB · ${esc(cleanName(r.submittedBy))}</small>${r.note ? `<small>${esc(r.note)}</small>` : ''}</td><td>${esc(r.stage)}</td><td>${date(r.submittedAt)}</td><td>${badge('APPROVED', 'Đã nộp')}</td><td><a href="/api/student/reports/${r.id}/download" class="button secondary small">Tải về</a></td></tr>`).join('')}</tbody></table></div>` : empty('Chưa có báo cáo', 'Các tệp do nhóm trưởng nộp sẽ hiển thị tại đây.', '', 'file')}
    </section>`;
}

function newsPanel() {
  return `<section class="panel section-gap announcement-panel">
    <div class="panel-head">
      <h2>Thông báo từ khoa</h2>
      ${announcements.length ? `<span class="badge" style="background:var(--primary-soft);color:var(--primary);font-size:12px;font-weight:600;padding:4px 10px;border-radius:20px;">${announcements.length} thông báo</span>` : ''}
    </div>
    ${announcements.length ? `<div class="announcement-list">${announcements.map(a => `<article class="announcement-card">
      <div class="announcement-header">
        <h3 class="announcement-title">${esc(a.title)}</h3>
        ${a.createdAt ? `<time class="announcement-date">${date(a.createdAt)}</time>` : ''}
      </div>
      <p class="announcement-content">${esc(a.content)}</p>
    </article>`).join('')}</div>` : '<p class="muted">Chưa có thông báo mới.</p>'}
  </section>`;
}

function resultsPage() {
  return heading('Kết quả đánh giá', 'Tra cứu các đợt bạn đã tham gia.') +
    `<section class="panel section-gap"><label>Đợt đăng ký<select id="result-period"><option value="">Đợt hiện tại hoặc gần nhất</option>${resultPeriods.map(p => `<option value="${p.id}" ${String(p.id) === resultPeriodId ? 'selected' : ''}>${esc(p.name)} · ${esc(p.type)}</option>`).join('')}</select></label></section>` + (result.published ? `<section class="panel"><span class="eyebrow">ĐIỂM TỔNG KẾT</span><h2>${esc(result.score)} / 10</h2><p>${esc(result.topic)}</p><div class="table-wrap"><table><thead><tr><th>GIẢNG VIÊN ĐÁNH GIÁ</th><th>ĐIỂM</th><th>NHẬN XÉT</th></tr></thead><tbody>${result.grades.map(g => `<tr><td>${esc(cleanName(g.name))}</td><td><b>${esc(g.score)}</b></td><td>${esc(g.comment)}</td></tr>`).join('')}</tbody></table></div></section>` : `<section class="panel">${empty('Chưa công bố kết quả', 'Điểm sẽ hiển thị sau khi hội đồng hoàn tất đánh giá và khoa công bố.', '', 'file')}</section>`);
}

function render() {
  page = location.hash.slice(1) || 'dashboard';
  if (!['dashboard', 'group', 'topics', 'project', 'reports', 'results'].includes(page)) page = 'dashboard';
  document.querySelectorAll('[data-nav]').forEach(a => {
    a.classList.toggle('active', a.dataset.nav === page);
    if (a.dataset.nav === page) a.setAttribute('aria-current', 'page');
    else a.removeAttribute('aria-current');
  });
  const displayName = cleanName(state.student.name) || 'Lê Minh Tuấn';
  $('#profile-name').textContent = displayName;
  $('#profile-avatar').textContent = initials(displayName);
  $('#invite-count').textContent = state.invitations.length || '';
  $('#main').innerHTML = ({
    dashboard: () => dashboard() + newsPanel(),
    group: groupPage,
    topics: topicsPage,
    project: projectPage,
    reports: reportsPage,
    results: resultsPage
  })[page]();
  document.title = ({
    dashboard: 'Tổng quan',
    group: 'Nhóm của tôi',
    topics: 'Kho đề tài',
    project: 'Đề tài đang thực hiện',
    reports: 'Nộp báo cáo',
    results: 'Kết quả đánh giá'
  })[page] + ' · HCM-UTE';
}

async function refresh() {
  const params = new URLSearchParams({...filters, page: String(catalogMeta.page || 0), size: '20'});
  const values = await Promise.all([api('/me'), api('/topics/page?' + params), api('/result' + (resultPeriodId ? '?periodId=' + encodeURIComponent(resultPeriodId) : '')), api('/announcements'), api('/periods'), api('/result-periods')]);
  state = values[0];
  catalogMeta = values[1];
  catalog = catalogMeta.content;
  result = values[2];
  announcements = values[3];
  periodChoices = values[4];
  resultPeriods = values[5];
  render();
}

function openDialog(title, body) {
  const d = $('#dialog');
  d.innerHTML = `<div class="dialog-head"><h2 id="dialog-title">${title}</h2><button class="close" data-action="close" aria-label="Đóng hộp thoại">×</button></div>${body}<div class="dialog-error danger notice" role="alert" hidden></div>`;
  d.showModal();
}

async function mutate(path, body) {
  if (busy) return;
  busy = true;
  const buttons = [...document.querySelectorAll('button')];
  buttons.forEach(b => b.disabled = true);
  try {
    const result = await api(path, body);
    $('#dialog').close();
    await refresh();
    toast(result.message);
  } catch (e) {
    if ($('#dialog').open) {
      const box = $('.dialog-error');
      box.hidden = false;
      box.textContent = e.message;
    } else toast(e.message, true);
  } finally {
    busy = false;
    buttons.forEach(b => b.disabled = false);
  }
}

document.addEventListener('click', e => {
  const b = e.target.closest('button');
  if (!b || busy) return;
  if (b.dataset.go) { location.hash = b.dataset.go; return; }
  if (b.dataset.action === 'close') { $('#dialog').close(); return; }
  if (b.dataset.action === 'create') {
    const available = periodChoices.filter(p => p.open && !p.joined);
    if (!available.length) { toast('Không có đợt đang mở mà bạn chưa tham gia nhóm.', true); return; }
    openDialog('Tạo nhóm mới', `<p class="muted">Nhóm tối đa 3 sinh viên. Chọn đợt trước khi tạo nhóm.</p><form id="create-form"><label>Đợt đăng ký<select name="periodId" required>${available.map(p => `<option value="${p.id}">${esc(p.name)} · ${esc(p.type)}</option>`).join('')}</select></label><label>Tên nhóm<input name="name" required maxlength="80" placeholder="Ví dụ: Nhóm Phát triển phần mềm"></label><button class="button primary" type="submit">Tạo nhóm</button></form>`);
  }
  if (b.dataset.action === 'invite') openDialog('Mời thành viên', '<p class="muted">Gửi lời mời bằng mã số sinh viên. Người nhận cần xác nhận trước khi được thêm vào nhóm.</p><form id="invite-form"><label>Mã số sinh viên<input name="studentId" required maxlength="30" placeholder="Ví dụ: 22110002"></label><button class="button primary" type="submit">Gửi lời mời</button></form>');
  if (b.dataset.action === 'cancel-registration') openDialog('Hủy đăng ký đề tài', '<p>Đăng ký sẽ chuyển sang trạng thái <b>Đã hủy</b> và lịch sử vẫn được giữ nguyên.</p><form id="cancel-form"><label>Lý do<textarea name="note" maxlength="2000" placeholder="Lý do hủy đăng ký"></textarea></label><button class="button danger" type="submit">Xác nhận hủy</button></form>');
  if (b.dataset.transfer) {
    const member = state.group.members.find(m => m.id === b.dataset.transfer);
    openDialog('Chuyển quyền nhóm trưởng', `<p>Chọn <b>${esc(cleanName(member.name))}</b> làm nhóm trưởng? Sau khi chuyển, bạn sẽ là thành viên và không thể đăng ký hoặc nộp báo cáo thay nhóm.</p><button type="button" class="button primary" data-confirm-transfer="${esc(member.id)}">Xác nhận chuyển quyền</button>`);
  }
  if (b.dataset.confirmTransfer) mutate('/groups/leader', {studentId: b.dataset.confirmTransfer});
  if (b.dataset.action === 'leave-group') {
    openDialog('Rời nhóm', `<p>Bạn có chắc muốn rời nhóm?</p><div class="actions" style="margin-top:16px;"><button type="button" class="button danger" data-confirm-leave="true">Xác nhận rời nhóm</button><button type="button" class="button secondary" data-action="close">Hủy</button></div>`);
  }
  if (b.dataset.confirmLeave) mutate('/groups/leave', {});
  if (b.dataset.action === 'disband-group') {
    openDialog('Giải tán nhóm', `<p>Bạn có chắc muốn giải tán nhóm?</p><p class="muted">Tất cả thành viên sẽ rời khỏi nhóm và dữ liệu nhóm sẽ bị xóa hoàn toàn.</p><div class="actions" style="margin-top:16px;"><button type="button" class="button danger" data-confirm-disband="true">Xác nhận giải tán nhóm</button><button type="button" class="button secondary" data-action="close">Hủy</button></div>`);
  }
  if (b.dataset.confirmDisband) mutate('/groups/disband', {});
  if (b.dataset.removeMember) {
    const member = state.group?.members?.find(m => m.id === b.dataset.removeMember);
    const mName = member ? cleanName(member.name) : b.dataset.removeMember;
    openDialog('Xóa thành viên', `<p>Bạn có chắc muốn xóa thành viên này?</p><p class="muted">Thành viên <b>${esc(mName)}</b> (${esc(b.dataset.removeMember)}) sẽ bị xóa khỏi nhóm.</p><div class="actions" style="margin-top:16px;"><button type="button" class="button danger" data-confirm-remove-member="${esc(b.dataset.removeMember)}">Xác nhận xóa</button><button type="button" class="button secondary" data-action="close">Hủy</button></div>`);
  }
  if (b.dataset.confirmRemoveMember) mutate('/groups/members/remove', {studentId: b.dataset.confirmRemoveMember});
  if (b.dataset.respond) mutate('/invitations/' + b.dataset.respond + '/response', {accept: b.dataset.accept === 'true'});
  if (b.dataset.topic) {
    const item = catalog.find(x => x.topic.id === b.dataset.topic);
    const samePeriod = state.group?.periodId === item.topic.periodId;
    const can = isLeader() && !locked() && item.open && samePeriod;
    openDialog('Chi tiết đề tài', `${topicBody(item.topic)}<div class="notice">${!state.group ? 'Bạn cần tạo hoặc tham gia nhóm.' : !isLeader() ? 'Chỉ nhóm trưởng được đăng ký đề tài.' : locked() ? 'Nhóm đã có một đăng ký đề tài.' : !item.open ? 'Đã ngoài thời gian đăng ký.' : !samePeriod ? 'Đề tài thuộc đợt khác với nhóm. Hãy chọn đề tài cùng đợt.' : `Đăng ký cho nhóm <b>${esc(state.group.name)}</b>. Kết quả sẽ ở trạng thái chờ duyệt.`}</div>${can ? `<button type="button" class="button primary" data-register="${esc(item.topic.id)}">Xác nhận đăng ký đề tài</button>` : ''}`);
  }
  if (b.dataset.register) mutate('/registrations', {topicId: b.dataset.register});
  if (b.dataset.action === 'reset-filter') {
    filters = {q: '', department: '', type: ''};
    catalogMeta.page = 0;
    refresh().catch(e => toast(e.message, true));
  }
  if (b.dataset.action === 'catalog-prev' && !catalogMeta.first) {
    catalogMeta.page--;
    refresh().catch(e => toast(e.message, true));
  }
  if (b.dataset.action === 'catalog-next' && !catalogMeta.last) {
    catalogMeta.page++;
    refresh().catch(e => toast(e.message, true));
  }
});

document.addEventListener('submit', async e => {
  const form = e.target;
  if (!['filter-form', 'create-form', 'invite-form', 'report-form', 'cancel-form'].includes(form.id)) return;
  e.preventDefault();
  if (busy) return;
  const data = new FormData(form);
  if (form.id === 'create-form') mutate('/groups', {name: String(data.get('name')).trim(), periodId: Number(data.get('periodId'))});
  if (form.id === 'invite-form') mutate('/groups/invitations', {studentId: String(data.get('studentId')).trim()});
  if (form.id === 'cancel-form') mutate('/registrations/cancel', {note: String(data.get('note') || '').trim()});
  if (form.id === 'report-form') {
    if (data.get('file').size > 10 * 1024 * 1024) {
      toast('Tệp vượt quá giới hạn 10 MB.', true);
      return;
    }
    mutate('/reports', data);
  }
  if (form.id === 'filter-form') {
    filters = Object.fromEntries(data);
    catalogMeta.page = 0;
    try {
      const response = await api('/topics/page?' + new URLSearchParams({...filters, page: '0', size: '20'}));
      catalogMeta = response;
      catalog = response.content;
      render();
    } catch (error) {
      toast(error.message, true);
    }
  }
});

window.addEventListener('hashchange', () => {
  if (state) {
    render();
    window.scrollTo({top: 0});
  }
});

refresh().catch(e => {
  $('#main').innerHTML = empty('Chưa tải được dữ liệu', esc(e.message), '<button type="button" class="button secondary" data-action="reload">Thử lại</button>', 'info');
});

document.addEventListener('click', e => {
  if (e.target.closest('[data-action="reload"]')) location.reload();
});

// Mobile Drawer Navigation Toggle
const menuToggle = document.querySelector('[data-menu-toggle]');
const sidebar = document.querySelector('.sidebar');
if (menuToggle && sidebar) {
  menuToggle.addEventListener('click', (e) => {
    e.stopPropagation();
    sidebar.classList.toggle('open');
  });
  document.addEventListener('click', (e) => {
    if (sidebar.classList.contains('open') && !sidebar.contains(e.target) && !menuToggle.contains(e.target)) {
      sidebar.classList.remove('open');
    }
  });
}

// Only periods of the authenticated student's own groups are offered by the server.
document.addEventListener('change', async event => {
  if (event.target.id !== 'result-period') return;
  resultPeriodId = event.target.value;
  try {
    result = await api('/result' + (resultPeriodId ? '?periodId=' + encodeURIComponent(resultPeriodId) : ''));
    render();
  } catch (error) { toast(error.message, true); }
});
