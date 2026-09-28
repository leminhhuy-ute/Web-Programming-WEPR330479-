"""Audit-only reproduction. Run ONLY against disposable demo H2 server at :18080.
Creates fixtures; never targets persistent H2 data. Stop that server to discard changes.
"""
import datetime as dt
import http.cookiejar
import json
from pathlib import Path
import urllib.error
import urllib.request

BASE = 'http://127.0.0.1:18080'
evidence = []

class Session:
    def __init__(self, username):
        self.opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
        self.token = None
        self.token = self.call('GET', '/api/auth/csrf')['data']
        self.call('POST', '/api/auth/login', {'username':username, 'password':'Demo@12345'})
        self.token = self.call('GET', '/api/auth/csrf')['data']

    def call(self, method, path, body=None, content_type=None, expected=200):
        headers = {'X-XSRF-TOKEN': self.token} if self.token else {}
        if body is not None:
            if content_type:
                headers['Content-Type'] = content_type
            else:
                headers['Content-Type'] = 'application/json'
                body = json.dumps(body, ensure_ascii=False).encode()
        try:
            response = self.opener.open(urllib.request.Request(BASE+path, data=body, headers=headers, method=method))
        except urllib.error.HTTPError as error:
            response = error
        raw = response.read()
        try:
            result = json.loads(raw)
        except ValueError:
            result = {'bytes':len(raw)}
        if '/api/auth/' not in path:
            evidence.append({'method':method, 'path':path, 'status':response.code})
        if response.code != expected:
            raise RuntimeError(f'{method} {path}: {response.code} {result}')
        return result

def record(name, value):
    evidence.append({'assertion':name, 'observed':value})

def main():
    admin = Session('dean01')
    creator = Session('lecturer02')
    student = Session('student03')
    member = Session('student04')
    advisor = Session('lecturer01')
    staff = admin.call('GET', '/api/admin/users')['data']
    ids = {u['username']:u['id'] for u in staff}
    now = dt.datetime.now()
    stamp = lambda days: (now+dt.timedelta(days=days)).isoformat(timespec='seconds')
    period = {'name':'Audit report/results lifecycle','type':'COURSE','lecturerStartAt':stamp(-3),
              'lecturerEndAt':stamp(1),'studentStartAt':stamp(2),'studentEndAt':stamp(7)}
    period_id = admin.call('POST','/api/admin/registration-periods',period,expected=201)['data']['id']
    topic = {'topicCode':'AUDIT-STUDENT-B','title':'Audit replacement topic B','description':'Isolated lifecycle fixture',
             'maxStudents':3,'topicType':'COURSE','departmentId':1,'periodId':period_id}
    topic_id = creator.call('POST','/api/lecturer/topics',topic)['data']['id']
    admin.call('POST',f'/api/lecturer/department-topics/{topic_id}/assign-advisors',{'advisor1Id':ids['lecturer02']})
    admin.call('POST',f'/api/lecturer/department-topics/{topic_id}/approval',{'status':'APPROVED'})
    period.update(lecturerEndAt=stamp(-2),studentStartAt=stamp(-1))
    admin.call('PUT',f'/api/admin/registration-periods/{period_id}',period)
    student.call('POST','/api/student/groups',{'name':'Audit lifecycle group'})
    student.call('POST','/api/student/registrations',{'topicId':'DT-CNPM-202602'})
    state = student.call('GET','/api/student/me')
    group_id = state['group']['id']
    advisor.call('POST',f'/api/lecturer/student-groups/{group_id}/approval',{'status':'APPROVED'})
    boundary = 'audit-boundary-930df0'
    multipart = (
        f'--{boundary}\r\nContent-Disposition: form-data; name="stage"\r\n\r\nCuối kỳ\r\n'
        f'--{boundary}\r\nContent-Disposition: form-data; name="note"\r\n\r\nTopic A report\r\n'
        f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="a.pdf"\r\n'
        f'Content-Type: application/pdf\r\n\r\n%PDF-1.7\nnot-a-parseable-pdf\r\n--{boundary}--\r\n').encode()
    student.call('POST','/api/student/reports',multipart,'multipart/form-data; boundary='+boundary)
    report_id = student.call('GET','/api/student/me')['reports'][0]['id']
    record('Minimal invalid PDF accepted',report_id)
    admin.call('POST','/api/councils/assignments',{'groupId':group_id,'councilId':1,'reviewerId':ids['lecturer05']})
    defense = next(x for x in admin.call('GET','/api/councils')['defenses'] if x['groupName']=='Audit lifecycle group')
    for account in ['dean01','hod01','lecturer03','lecturer04','lecturer05']:
        s = admin if account == 'dean01' else Session(account)
        s.call('POST',f"/api/councils/defenses/{defense['id']}/grade",{'score':8,'comment':'Audit original topic A score'})
    admin.call('POST',f"/api/councils/defenses/{defense['id']}/finalize",{})
    admin.call('POST',f"/api/councils/defenses/{defense['id']}/publish",{})
    record('Published original result',student.call('GET','/api/student/result'))
    student.call('POST','/api/student/registrations/cancel',{'note':'Audit cancellation after publication'})
    student.call('POST','/api/student/groups/invitations',{'studentId':'22110004'})
    invitation = member.call('GET','/api/student/me')['invitations'][0]
    member.call('POST',f"/api/student/invitations/{invitation['id']}/response",{'accept':True})
    record('Late joining member sees published original result',member.call('GET','/api/student/result'))
    student.call('POST','/api/student/registrations',{'topicId':'AUDIT-STUDENT-B'})
    record('Published result after switch to pending replacement topic',student.call('GET','/api/student/result'))
    record('Old report relabeled to replacement topic',student.call('GET','/api/student/me')['reports'])
    record('Replacement supervisor can fetch old report',creator.call('GET',f'/api/lecturer/reports/{report_id}'))
    advisor.call('GET',f'/api/lecturer/reports/{report_id}',expected=403)
    record('Original supervisor loses old report access',403)

try:
    main()
finally:
    output = Path(__file__).with_name('student-runtime-results.json')
    output.write_text(json.dumps(evidence, ensure_ascii=False, indent=2), encoding='utf-8')
    print(output)
    print(json.dumps(evidence[-8:], ensure_ascii=True, indent=2))
