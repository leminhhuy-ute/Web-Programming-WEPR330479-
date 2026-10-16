'use strict';
let mailPage=0;
async function loadMail() {
    try {
        const status=document.querySelector('[name="status"]').value;
        const state=await request(`/api/admin/email-notifications?page=${mailPage}${status?'&status='+encodeURIComponent(status):''}`);
        const page=state.page;
        document.querySelector('#mail-state').textContent=state.enabled?'Gửi email đã bật. Email điểm thật sẽ được tạo sau khi Trưởng khoa công bố kết quả đã tổng hợp. Hàng đợi được xử lý tự động.':'Gửi email đang tắt. Kết quả công bố vẫn được lưu vào hàng đợi và sẽ gửi khi SMTP được bật.';
        document.querySelector('#mail-test').disabled=!state.enabled;
        document.querySelector('#mail-test-recipient').textContent=state.enabled?`Thư kiểm tra sẽ gửi đến địa chỉ người gửi đã cấu hình: ${state.testRecipient}. Thư không chứa điểm.`:'';
        document.querySelector('#mail-rows').innerHTML=page.content.length?page.content.map(n=>`<tr><td>${escapeHtml(n.student)}<br><small>${escapeHtml(n.group)}</small></td><td>${escapeHtml(n.recipient)}</td><td>${({PENDING:'Đang chờ',SENT:'Đã gửi',FAILED:'Thất bại'})[n.status]}${n.lastError?`<br><small>${escapeHtml(n.lastError)}</small>`:''}</td><td>${n.attempts}</td><td>${formatDateTime(n.sentAt||n.createdAt)}</td><td>${n.status==='FAILED'?`<button class="button secondary" data-retry="${n.id}">Gửi lại</button>`:'—'}</td></tr>`).join(''):'<tr><td colspan="6">Chưa có email điểm thật. Thư chỉ xuất hiện sau khi Trưởng khoa công bố kết quả đã tổng hợp.</td></tr>';
        document.querySelector('#mail-prev').disabled=page.first;
        document.querySelector('#mail-next').disabled=page.last;
        document.querySelector('#mail-count').textContent=`Trang ${page.page+1}/${Math.max(1,page.totalPages)} · ${page.totalElements} email`;
    } catch(error) { const el=document.querySelector('#message');el.hidden=false;el.textContent=error.message; }
}
document.querySelector('#mail-filter').addEventListener('submit',event=>{event.preventDefault();mailPage=0;void loadMail();});
document.querySelector('#mail-test').addEventListener('click',async event=>{
    const button=event.currentTarget;const message=document.querySelector('#mail-test-message');button.disabled=true;
    message.hidden=false;message.classList.remove('error');message.textContent='Đang gửi email kiểm tra SMTP…';
    try { await request('/api/admin/email-notifications/test',{method:'POST'});message.textContent='Máy chủ SMTP đã chấp nhận thư kiểm tra. Đây chưa phải xác nhận thư vào Hộp thư đến; kiểm tra cả Spam nếu chưa thấy.'; }
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
