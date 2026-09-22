'use strict';
const escapeCouncil = value => String(value ?? '').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const c$ = s => document.querySelector(s);
let councilState, councilBusy=false;
async function councilApi(path, body) {
  const options={credentials:'same-origin',headers:{Accept:'application/json'}};
  if(body!==undefined){
    const tokenResponse=await fetch('/api/auth/csrf');
    const token=await tokenResponse.json();
    options.method='POST';options.headers['Content-Type']='application/json';
    options.headers['X-XSRF-TOKEN']=token.data;options.body=JSON.stringify(body);
  }
  const response=await fetch(path,options);
  if(response.status===401){location.href='/login.html';throw new Error('Phiên đăng nhập đã hết hạn.');}
  const data=await response.json();
  if(!response.ok)throw new Error(data.message||'Không thể xử lý yêu cầu.');
  return data;
}
function councilMessage(message,error=false){const e=c$('#feedback');e.hidden=false;e.textContent=message;e.className=error?'error':'';}
const councilRole = role => ({CHAIRPERSON:'Chủ tịch',SECRETARY:'Thư ký',MEMBER:'Ủy viên'}[role]||role);
async function loadCouncils(){
  councilState=await councilApi('/api/councils');
  const dean=councilState.role==='DEAN';
  c$('#admin-actions').hidden=!dean;c$('#back-link').href=dean?'/admin/dashboard.html':'/lecturer/dashboard.html';
  c$('#council-count').textContent=councilState.councils.length+' hội đồng';
  c$('#council-list').innerHTML=councilState.councils.length?'<div class="grid cols">'+councilState.councils.map(c=>`
    <article class="council-card"><span class="topic-code">${escapeCouncil(c.code)}</span><h3>${escapeCouncil(c.name)}</h3>
    <p>${escapeCouncil(new Date(c.date).toLocaleString('vi-VN'))} · Phòng ${escapeCouncil(c.room)}</p>
    <ul>${c.members.map(m=>`<li>${escapeCouncil(m.name)} — ${councilRole(m.role)}</li>`).join('')}</ul></article>`).join('')+'</div>':'<p class="muted">Chưa có hội đồng thuộc phạm vi của bạn.</p>';
  c$('#defense-list').innerHTML=councilState.defenses.length?'<div class="stack">'+councilState.defenses.map(d=>`
    <article class="council-card"><div class="panel-head"><h3>${escapeCouncil(d.topic)}</h3><span class="badge ${d.published?'APPROVED':'PENDING'}">${d.published?'Đã công bố':d.finalized?'Đã tổng hợp':'Đang chấm'}</span></div>
    <p>${escapeCouncil(d.groupName)} · ${escapeCouncil(d.council)}</p><p>Phản biện: ${escapeCouncil(d.reviewer)}</p>
    <p>Điểm tổng hợp: <strong>${d.score??'Chưa tổng hợp'}</strong></p>
    <details><summary>Xem ${d.grades.length} phiếu đánh giá</summary>${d.grades.map(g=>`<p><b>${escapeCouncil(g.name)}: ${g.score}</b><br>${escapeCouncil(g.comment)}</p>`).join('')}</details>
    <div class="actions">${d.canGrade?`<button class="primary" data-grade="${d.id}">Nhập / sửa điểm của tôi</button>`:''}
    ${d.canFinalize?`<button data-finalize="${d.id}">Tổng hợp điểm</button>`:''}
    ${dean&&d.finalized&&!d.published?`<button class="primary" data-publish="${d.id}">Công bố kết quả</button>`:''}</div></article>`).join('')+'</div>':'<p class="muted">Chưa có nhóm được phân công cho hội đồng của bạn.</p>';
}
function showCouncilDialog(title,html){c$('#council-dialog').innerHTML=`<div class="dialog-head"><h2 id="dialog-title">${title}</h2><button type="button" data-close aria-label="Đóng">×</button></div>${html}<p id="dialog-error" role="alert" class="danger"></p>`;c$('#council-dialog').showModal();}
const lecturerOptions=()=>councilState.lecturers.map(u=>`<option value="${u.id}">${escapeCouncil(u.name)}</option>`).join('');
document.addEventListener('click',async e=>{
  const b=e.target.closest('button');if(!b||councilBusy)return;
  if(b.hasAttribute('data-close'))return c$('#council-dialog').close();
  if(b.id==='create-council'){
    showCouncilDialog('Thành lập hội đồng',`<form id="council-form"><label>Mã hội đồng<input name="code" maxlength="30" required></label><label>Tên hội đồng<input name="name" maxlength="150" required></label><label>Ngày báo cáo<input name="defenseDate" type="datetime-local" required></label><label>Phòng<input name="room" maxlength="80" required></label><p>Chọn 3–5 giảng viên khác nhau.</p>
      ${[0,1,2,3,4].map(i=>`<div class="member-input"><label>Giảng viên ${i+1}<select name="member${i}" ${i<3?'required':''}><option value="">${i<3?'Chọn giảng viên':'Không thêm'}</option>${lecturerOptions()}</select></label><label>Vai trò ${i+1}<select name="role${i}"><option value="${i===0?'CHAIRPERSON':i===1?'SECRETARY':'MEMBER'}">${i===0?'Chủ tịch':i===1?'Thư ký':'Ủy viên'}</option></select></label></div>`).join('')}<button class="primary">Lưu hội đồng</button></form>`);
  }
  if(b.id==='assign-group'){
    if(!councilState.groups.length||!councilState.councils.length)return councilMessage('Cần có nhóm đã được duyệt và một hội đồng.',true);
    showCouncilDialog('Phân công nhóm',`<form id="assignment-form"><label>Nhóm & đề tài<select name="groupId">${councilState.groups.map(g=>`<option value="${g.id}">${escapeCouncil(g.name)}</option>`).join('')}</select></label><label>Hội đồng<select name="councilId">${councilState.councils.map(c=>`<option value="${c.id}">${escapeCouncil(c.name)}</option>`).join('')}</select></label><label>Giảng viên phản biện<select name="reviewerId">${lecturerOptions()}</select></label><p class="muted">Giảng viên phản biện phải thuộc hội đồng. Hội đồng không bao gồm GVHD của đề tài.</p><button class="primary">Phân công</button></form>`);
  }
  if(b.dataset.grade)showCouncilDialog('Phiếu đánh giá của tôi',`<form id="grade-form" data-id="${b.dataset.grade}"><label>Điểm (0–10)<input name="score" type="number" min="0" max="10" step="0.01" required></label><label>Nhận xét<textarea name="comment" maxlength="2000" required></textarea></label><button class="primary">Lưu đánh giá</button></form>`);
  if(b.dataset.finalize)showCouncilDialog('Tổng hợp điểm',`<p>Hệ thống tính trung bình cộng tất cả phiếu điểm và khóa chỉnh sửa.</p><form id="finalize-form" data-id="${b.dataset.finalize}"><button class="primary">Xác nhận tổng hợp</button></form>`);
  if(b.dataset.publish)showCouncilDialog('Công bố kết quả',`<p>Kết quả sẽ hiển thị cho các thành viên của nhóm.</p><form id="publish-form" data-id="${b.dataset.publish}"><button class="primary">Xác nhận công bố</button></form>`);
  if(b.id==='logout'){try{await councilApi('/api/auth/logout',{});location.href='/login.html';}catch(err){councilMessage(err.message,true);}}
});
document.addEventListener('submit',async e=>{
  const form=e.target;if(!['council-form','assignment-form','grade-form','finalize-form','publish-form'].includes(form.id))return;
  e.preventDefault();if(councilBusy)return;councilBusy=true;
  const values=Object.fromEntries(new FormData(form));let path='/api/councils',body=values;
  if(form.id==='council-form'){body={code:values.code,name:values.name,defenseDate:values.defenseDate,room:values.room,members:[0,1,2,3,4].filter(i=>values['member'+i]).map(i=>({userId:Number(values['member'+i]),role:values['role'+i]}))};}
  if(form.id==='assignment-form'){path+='/assignments';body=Object.fromEntries(Object.entries(values).map(([k,v])=>[k,Number(v)]));}
  if(form.id==='grade-form'){path+='/defenses/'+form.dataset.id+'/grade';body={score:Number(values.score),comment:values.comment};}
  if(form.id==='finalize-form'||form.id==='publish-form'){path+='/defenses/'+form.dataset.id+'/'+(form.id==='finalize-form'?'finalize':'publish');body={};}
  const submit=form.querySelector('button[type="submit"],button.primary');if(submit)submit.disabled=true;
  try{await councilApi(path,body);c$('#council-dialog').close();await loadCouncils();councilMessage('Đã lưu thay đổi.');}
  catch(err){c$('#dialog-error').textContent=err.message;}
  finally{councilBusy=false;if(submit)submit.disabled=false;}
});
loadCouncils().catch(err=>councilMessage(err.message,true));
