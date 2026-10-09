'use strict';
let mailPage=0;
async function loadMail() {
    try {
        const status=document.querySelector('[name="status"]').value;
        const state=await request(`/api/admin/email-notifications?page=${mailPage}${status?'&status='+encodeURIComponent(status):''}`);
        const page=state.page;
        document.querySelector('#mail-state').textContent=state.enabled?'Gửi email đã bật. Hàng đợi được xử lý tự động.':'Gửi email đang tắt. Thông báo vẫn được lưu và sẽ gửi khi cấu hình SMTP được bật.';
        document.querySelector('#mail-test').disabled=!state.enabled;
        document.querySelector('#mail-test-recipient').textContent=state.enabled?`Email thử được gửi đến ${state.testRecipient}. Điểm trong thư chỉ là minh họa.`:'';
        document.querySelector('#mail-rows').innerHTML=page.content.length?page.content.map(n=>`<tr><td>${escapeHtml(n.student)}<br><small>${escapeHtml(n.group)}</small></td><td>${escapeHtml(n.recipient)}</td><td>${({PENDING:'Đang chờ',SENT:'Đã gửi',FAILED:'Thất bại'})[n.status]}${n.lastError?`<br><small>${escapeHtml(n.lastError)}</small>`:''}</td><td>${n.attempts}</td><td>${formatDateTime(n.sentAt||n.createdAt)}</td><td>${n.status==='FAILED'?`<button class="button secondary" data-retry="${n.id}">Gửi lại</button>`:'—'}</td></tr>`).join(''):'<tr><td colspan="6">Chưa có email trong trạng thái này.</td></tr>';
        document.querySelector('#mail-prev').disabled=page.first;
        document.querySelector('#mail-next').disabled=page.last;
        document.querySelector('#mail-count').textContent=`Trang ${page.page+1}/${Math.max(1,page.totalPages)} · ${page.totalElements} email`;
    } catch(error) { const el=document.querySelector('#message');el.hidden=false;el.textContent=error.message; }
}
document.querySelector('#mail-filter').addEventListener('submit',event=>{event.preventDefault();mailPage=0;void loadMail();});
document.querySelector('#mail-test').addEventListener('click',async event=>{
    const button=event.currentTarget;const message=document.querySelector('#mail-test-message');button.disabled=true;
    message.hidden=false;message.classList.remove('error');message.textContent='Đang gửi email thử…';
    try { await request('/api/admin/email-notifications/test',{method:'POST'});message.textContent='Máy chủ SMTP đã chấp nhận email thử. Hãy kiểm tra Hộp thư đến hoặc Spam.'; }
    catch(error) { message.classList.add('error');message.textContent=error.message; }
    finally { button.disabled=false; }
});
document.querySelector('#mail-prev').addEventListener('click',()=>{mailPage--;void loadMail();});
document.querySelector('#mail-next').addEventListener('click',()=>{mailPage++;void loadMail();});
document.querySelector('#mail-rows').addEventListener('click',async event=>{
    const button=event.target.closest('[data-retry]');if(!button)return;button.disabled=true;
    try { await request(`/api/admin/email-notifications/${button.dataset.retry}/retry`,{method:'POST'});await loadMail(); }
    catch(error) { const el=document.querySelector('#message');el.hidden=false;el.textContent=error.message;button.disabled=false; }
});
requireDean().then(loadMail).catch(()=>{});
