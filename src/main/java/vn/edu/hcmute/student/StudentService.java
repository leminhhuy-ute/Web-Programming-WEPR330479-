package vn.edu.hcmute.student;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import java.io.*;
import java.util.zip.ZipInputStream;

@Service @Transactional(readOnly=true)
public class StudentService {
    private final StudentRepository students;
    private final GroupRepository groups;
    private final InvitationRepository invitations;
    private final TopicRepository topics;
    private final RegistrationRepository registrations;
    private final ReportRepository reports;
    private final Clock clock;
    public StudentService(StudentRepository s,GroupRepository g,InvitationRepository i,TopicRepository t,RegistrationRepository r,ReportRepository f,Clock clock) {
        students=s;groups=g;invitations=i;topics=t;registrations=r;reports=f;this.clock=clock;
    }
    private ResponseStatusException bad(String text) {return new ResponseStatusException(HttpStatus.BAD_REQUEST,text);}
    private ResponseStatusException denied() {return new ResponseStatusException(HttpStatus.FORBIDDEN,"Bạn không có quyền thực hiện thao tác này.");}
    private Student student(String id) {return students.findById(id).orElseThrow(this::denied);}
    private Student lockedStudent(String id) {return students.lock(id).orElseThrow(this::denied);}
    private StudentGroup group(Student s,boolean lock) {
        if(s.groupId==null) throw bad("Bạn cần tạo hoặc tham gia một nhóm trước.");
        return (lock?groups.lock(s.groupId):groups.findById(s.groupId)).orElseThrow(this::denied);
    }
    private StudentGroup leader(String id) {
        StudentGroup g=group(student(id),true);
        if(!g.leaderId.equals(id)) throw denied();
        return g;
    }
    private void editable(Long groupId) {
        if(registrations.findByGroupId(groupId).filter(r->!r.status.equals("REJECTED")).isPresent())
            throw bad("Nhóm đã đăng ký đề tài. Danh sách thành viên được khóa.");
    }
    private Map<String,Object> studentView(Student s) {return Map.of("id",s.id,"name",s.name,"className",s.className);}
    private Map<String,Object> invitationView(Invitation i) {
        return Map.of("id",i.id,"groupId",i.groupId,"groupName",groups.findById(i.groupId).orElseThrow().name,
            "studentId",i.studentId,"studentName",student(i.studentId).name,"status",i.status,"createdAt",i.createdAt);
    }
    public Map<String,Object> state(String id) {
        Student me=student(id);
        Map<String,Object> state=new LinkedHashMap<>();
        state.put("student",studentView(me)); state.put("group",null);state.put("registration",null);state.put("reports",List.of());
        state.put("invitations",invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(id,"PENDING").stream().map(this::invitationView).toList());
        if(me.groupId!=null) {
            StudentGroup g=group(me,false);
            state.put("group",Map.of("id",g.id,"name",g.name,"leaderId",g.leaderId,"maxMembers",3,"members",students.findByGroupIdOrderById(g.id).stream().map(this::studentView).toList(),
                "invitations",invitations.findByGroupIdOrderByCreatedAtDesc(g.id).stream().map(this::invitationView).toList()));
            registrations.findByGroupId(g.id).ifPresent(r->{
                Map<String,Object> rv=new LinkedHashMap<>();rv.put("id",r.id);rv.put("status",r.status);rv.put("submittedAt",r.submittedAt);
                rv.put("feedback",r.feedback);rv.put("topic",topics.findById(r.topicId).orElseThrow());state.put("registration",rv);
            });
            state.put("reports",reports.findByGroupIdOrderBySubmittedAtDesc(g.id).stream().map(r->Map.of("id",r.id,"filename",r.filename,"stage",r.stage,"note",r.note,"size",r.content.length,"submittedAt",r.submittedAt,"late",r.late,"submittedBy",r.submittedBy)).toList());
        }
        return state;
    }
    public List<Map<String,Object>> catalog(String query,String department,String type) {
        String term=query.strip().toLowerCase(Locale.ROOT);
        return topics.findByPublishedTrueOrderById().stream()
            .filter(t->(t.id+" "+t.title+" "+t.supervisor+" "+t.technologies).toLowerCase(Locale.ROOT).contains(term))
            .filter(t->department.isBlank()||t.department.equals(department)).filter(t->type.isBlank()||t.type.equals(type))
            .map(t->{
                long used=registrations.countByTopicIdAndStatusIn(t.id,List.of("PENDING","APPROVED"));
                return Map.<String,Object>of("topic",t,"remaining",Math.max(0,t.capacity-used),"open",!clock.instant().isBefore(t.opensAt)&&clock.instant().isBefore(t.closesAt));
            }).toList();
    }
    @Transactional public void createGroup(String id,String name) {
        Student s=lockedStudent(id);
        if(s.groupId!=null) throw bad("Bạn đã thuộc một nhóm.");
        if(name==null||name.isBlank()||name.strip().length()>80) throw bad("Tên nhóm cần từ 1 đến 80 ký tự.");
        StudentGroup g=groups.save(new StudentGroup(name.strip(),id,clock.instant()));s.groupId=g.id;
    }
    @Transactional public void invite(String id,String targetId) {
        StudentGroup g=leader(id);editable(g.id);
        Student target=student(targetId);
        if(target.groupId!=null) throw bad("Sinh viên này đã thuộc một nhóm.");
        if(students.countByGroupId(g.id)>=3) throw bad("Nhóm đã đủ 3 thành viên.");
        Invitation i=invitations.findByGroupIdAndStudentId(g.id,targetId).orElseGet(()->new Invitation(g.id,targetId,clock.instant()));
        if(i.id!=null&&i.status.equals("PENDING")) throw bad("Bạn đã gửi lời mời cho sinh viên này.");
        i.status="PENDING";i.createdAt=clock.instant();invitations.save(i);
    }
    @Transactional public void respond(String id,Long invitationId,boolean accept) {
        Student me=lockedStudent(id);
        Invitation i=invitations.findById(invitationId).orElseThrow(this::denied);
        if(!i.studentId.equals(id)) throw denied();
        StudentGroup g=groups.lock(i.groupId).orElseThrow(this::denied);
        if(!i.status.equals("PENDING")) throw bad("Lời mời đã được xử lý.");
        if(accept) {
            if(me.groupId!=null) throw bad("Bạn đã thuộc một nhóm.");
            editable(g.id);
            if(students.countByGroupId(g.id)>=3) throw bad("Nhóm đã đủ 3 thành viên.");
            me.groupId=g.id;i.status="ACCEPTED";
            invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(id,"PENDING").stream().filter(x->!x.id.equals(i.id)).forEach(x->x.status="DECLINED");
        } else i.status="DECLINED";
    }
    @Transactional public void transfer(String id,String targetId) {
        StudentGroup g=leader(id);
        if(!Objects.equals(student(targetId).groupId,g.id)) throw bad("Nhóm trưởng mới phải là thành viên trong nhóm.");
        g.leaderId=targetId;
    }
    @Transactional public void register(String id,String topicId) {
        StudentGroup g=leader(id);
        Registration current=registrations.findByGroupId(g.id).orElseGet(Registration::new);
        if(current.id!=null&&!"REJECTED".equals(current.status)) throw bad("Nhóm chỉ được đăng ký một đề tài.");
        Topic t=topics.lock(topicId).orElseThrow(()->bad("Không tìm thấy đề tài."));
        Instant now=clock.instant();
        if(!t.published||now.isBefore(t.opensAt)||!now.isBefore(t.closesAt)) throw bad("Đề tài chưa mở hoặc đã hết hạn đăng ký.");
        if(registrations.countByTopicIdAndStatusIn(topicId,List.of("PENDING","APPROVED"))>=t.capacity) throw bad("Đề tài đã đủ số nhóm đăng ký.");
        current.groupId=g.id;current.topicId=topicId;current.status="PENDING";current.submittedAt=now;current.feedback=null;registrations.save(current);
    }
    @Transactional public void upload(String id,String stage,String note,MultipartFile file) throws IOException {
        StudentGroup g=leader(id);
        Registration r=registrations.findByGroupId(g.id).filter(x->x.status.equals("APPROVED")).orElseThrow(()->bad("Chỉ nộp báo cáo sau khi đề tài được duyệt."));
        if(!List.of("Đề cương","Giữa kỳ","Cuối kỳ").contains(stage)) throw bad("Loại báo cáo không hợp lệ.");
        if(note==null||note.length()>1000) throw bad("Ghi chú tối đa 1.000 ký tự.");
        if(file.isEmpty()||file.getSize()>10*1024*1024) throw bad("Chọn tệp PDF hoặc DOCX, tối đa 10 MB.");
        String filename=Objects.toString(file.getOriginalFilename(),"report").replace('\\','/');
        filename=filename.substring(filename.lastIndexOf('/')+1).replaceAll("[\\p{Cntrl}]","");
        if(filename.length()>180) throw bad("Tên tệp quá dài.");
        byte[] bytes=file.getBytes();String lower=filename.toLowerCase(Locale.ROOT);String contentType;
        if(lower.endsWith(".pdf")&&bytes.length>=5&&new String(bytes,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-")) contentType="application/pdf";
        else if(lower.endsWith(".docx")&&isDocx(bytes)) contentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        else throw bad("Nội dung tệp không phải PDF hoặc DOCX hợp lệ.");
        Report report=new Report();report.groupId=g.id;report.submittedBy=id;report.filename=filename;report.contentType=contentType;
        report.stage=stage;report.note=note.strip();report.submittedAt=clock.instant();report.content=bytes;
        report.late=report.submittedAt.isAfter(topics.findById(r.topicId).orElseThrow().reportDueAt);reports.save(report);
    }
    private boolean isDocx(byte[] bytes) {
        // Inspect only the central structure, with bounds against oversized archive entries.
        try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(bytes))) {
            boolean content=false,document=false;int entries=0;long expanded=0;byte[] buffer=new byte[8192];
            for(var e=zip.getNextEntry();e!=null;e=zip.getNextEntry()) {
                if(++entries>1000) return false;
                if(e.getName().equals("[Content_Types].xml")) content=true;
                if(e.getName().equals("word/document.xml")) document=true;
                for(int n;(n=zip.read(buffer))!=-1;) {expanded+=n;if(expanded>30*1024*1024) return false;}
            }
            return content&&document;
        } catch(IOException ex) {return false;}
    }
    public Report download(String id,Long reportId) {
        Student me=student(id);Report r=reports.findById(reportId).orElseThrow(this::denied);
        if(me.groupId==null||!me.groupId.equals(r.groupId)) throw denied();return r;
    }
}
