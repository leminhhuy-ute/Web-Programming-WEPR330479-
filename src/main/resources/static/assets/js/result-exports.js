'use strict';
async function downloadResults(format) {
    const button=document.querySelector(`#export-${format}`);button.disabled=true;
    try {
        const period=document.querySelector('#export-period').value;
        const response=await fetch(api(`/api/admin/exports/results.${format}${period?'?periodId='+encodeURIComponent(period):''}`),{credentials:'same-origin'});
        if(!response.ok){const error=await response.json();throw new Error(error.message||'Không thể tải kết quả.');}
        const url=URL.createObjectURL(await response.blob());
        const link=document.createElement('a');link.href=url;link.download=`ket-qua-${period||'tat-ca'}.${format}`;link.click();
        setTimeout(()=>URL.revokeObjectURL(url),1000);
    } catch(error){const message=document.querySelector('#message');message.hidden=false;message.textContent=error.message;}
    finally {button.disabled=false;}
}
document.querySelector('#export-xlsx').addEventListener('click',()=>downloadResults('xlsx'));
document.querySelector('#export-pdf').addEventListener('click',()=>downloadResults('pdf'));
requireDean().then(async()=>{
    const periods=await request('/api/admin/registration-periods');
    document.querySelector('#export-period').insertAdjacentHTML('beforeend',periods.map(p=>`<option value="${p.id}">${escapeHtml(p.name)}</option>`).join(''));
}).catch(error=>{const message=document.querySelector('#message');message.hidden=false;message.textContent=error.message;});
