package topicmanagement.student;

import jakarta.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;
import topicmanagement.entity.*;
import topicmanagement.enums.*;
import topicmanagement.repository.*;
import topicmanagement.service.TopicPolicy;
import java.time.*;
import java.util.*;
import java.io.*;
import java.util.zip.ZipInputStream;

@Service @Transactional(readOnly=true)
public class StudentService {
    private final UserRepository users;
    private final StudentGroupRepository groups;
    private final GroupMemberRepository members;
    private final InvitationRepository invitations;
    private final TopicRepository topics;
    private final ReportRepository reports;
    private final EntityManager em;
    public StudentService(UserRepository users, StudentGroupRepository groups, GroupMemberRepository members,
            InvitationRepository invitations, TopicRepository topics, ReportRepository reports, EntityManager em) {
        this.users=users;this.groups=groups;this.members=members;this.invitations=invitations;
        this.topics=topics;this.reports=reports;this.em=em;
    }
    private IllegalArgumentException bad(String message) {return new IllegalArgumentException(message);}
    private AccessDeniedException denied() {return new AccessDeniedException("Bạn không có quyền thao tác trên nhóm này.");}
    private User student(String username) {
        return users.findByUsername(username).filter(u->u.getRole()==Role.STUDENT && u.getStatus()==UserStatus.ACTIVE).orElseThrow(this::denied);
    }
    private User target(String code) {
        return em.createQuery("select u from User u where u.userCode=:code",User.class).setParameter("code",code)
            .getResultStream().filter(u->u.getRole()==Role.STUDENT && u.getStatus()==UserStatus.ACTIVE).findFirst()
            .orElseThrow(()->bad("Không tìm thấy sinh viên đang hoạt động với MSSV này."));
    }
    private StudentGroup group(User user) {
        return members.findByStudentId(user.getId()).stream().findFirst().map(GroupMember::getGroup).orElse(null);
    }
    private StudentGroup lockGroup(Long id) {
        StudentGroup g=em.find(StudentGroup.class,id,LockModeType.PESSIMISTIC_WRITE);
        em.refresh(g);return g;
    }
    private StudentGroup leader(String username) {
        User me=student(username);StudentGroup found=group(me);
        if(found==null) throw bad("Bạn cần tạo hoặc tham gia một nhóm.");
        StudentGroup g=lockGroup(found.getId());
        if(!g.getLeader().getId().equals(me.getId()))throw denied();
        return g;
    }
    private void editable(StudentGroup g) {
        if(g.getStatus()!=GroupStatus.DRAFT && g.getStatus()!=GroupStatus.REJECTED)
            throw bad("Nhóm đã đăng ký đề tài. Danh sách thành viên được khóa.");
    }
    private Map<String,Object> person(User u) {
        return Map.of("id",u.getUserCode(),"name",u.getFullName(),"className",u.getDepartment()==null?"":u.getDepartment().getName());
    }
    private Map<String,Object> invitation(Invitation i) {
        return Map.of("id",i.id,"groupId",i.group.getId(),"groupName",i.group.getGroupName(),
            "studentId",i.student.getUserCode(),"studentName",i.student.getFullName(),"status",i.status,"createdAt",i.createdAt);
    }
    private String type(Topic t) {return t.getTopicType()==RegistrationPeriodType.COURSE?"Môn học":t.getTopicType().name();}
    private Map<String,Object> topic(Topic t) {
        Map<String,Object> v=new LinkedHashMap<>();
        v.put("id",t.getTopicCode());v.put("title",t.getTitle());v.put("description",Objects.toString(t.getDescription(),""));
        v.put("technologies",Objects.toString(t.getRequirements(),""));
        v.put("supervisor",t.getAdvisor1()==null?"Chưa phân công":t.getAdvisor1().getFullName()+(t.getAdvisor2()==null?"":" · "+t.getAdvisor2().getFullName()));
        v.put("department",t.getDepartment().getName());v.put("type",type(t));
        v.put("capacity",1);v.put("opensAt",t.getPeriod().getStudentStartAt());v.put("closesAt",t.getPeriod().getStudentEndAt());
        // The period review deadline is for reviewers' grades, not student reports.
        v.put("reportDueAt",null);
        return v;
    }
    public Map<String,Object> state(String username) {
        User me=student(username);StudentGroup g=group(me);
        Map<String,Object> state=new LinkedHashMap<>();
        state.put("student",person(me));state.put("group",null);state.put("registration",null);state.put("reports",List.of());
        state.put("invitations",invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(me.getId(),"PENDING").stream().map(this::invitation).toList());
        if(g!=null) {
            state.put("group",Map.of("id",g.getId(),"name",g.getGroupName(),"leaderId",g.getLeader().getUserCode(),"maxMembers",3,
                "members",members.findByGroupId(g.getId()).stream().map(m->person(m.getStudent())).toList(),
                "invitations",invitations.findByGroupIdOrderByCreatedAtDesc(g.getId()).stream().map(this::invitation).toList()));
            if(g.getTopic()!=null) {
                Map<String,Object> reg=new LinkedHashMap<>();reg.put("id",g.getId());reg.put("status",g.getStatus().name());
                reg.put("submittedAt",g.getRegisteredAt());reg.put("feedback",g.getNotes());reg.put("topic",topic(g.getTopic()));state.put("registration",reg);
            }
            state.put("reports",reports.findByGroupIdOrderBySubmittedAtDesc(g.getId()).stream().map(f->Map.of(
                "id",f.id,"filename",f.filename,"stage",f.stage,"note",f.note,"size",f.content.length,"submittedAt",f.submittedAt,
                "late",f.late,"submittedBy",f.submittedBy.getUserCode())).toList());
        }
        return state;
    }
    public List<Map<String,Object>> catalog(String q,String department,String type) {
        String term=q.strip().toLowerCase(Locale.ROOT);
        return topics.findAll().stream().filter(t->t.getStatus()==TopicStatus.APPROVED)
            .filter(t->(t.getTopicCode()+" "+t.getTitle()+" "+(t.getAdvisor1()==null?"":t.getAdvisor1().getFullName())).toLowerCase(Locale.ROOT).contains(term))
            .filter(t->department.isBlank()||t.getDepartment().getName().equals(department))
            .filter(t->type.isBlank()||type(t).equals(type)).map(t->{
                long used=groups.findByTopicId(t.getId()).stream().filter(g->g.getStatus()!=GroupStatus.REJECTED).count();
                var now=LocalDateTime.now();
                return Map.<String,Object>of("topic",topic(t),"remaining",Math.max(0,1-used),"open",
                    !now.isBefore(t.getPeriod().getStudentStartAt())&&!now.isAfter(t.getPeriod().getStudentEndAt()));
            }).toList();
    }
    @Transactional public void createGroup(String username,String name) {
        User me=student(username);em.lock(me,LockModeType.PESSIMISTIC_WRITE);
        if(group(me)!=null)throw bad("Bạn đã thuộc một nhóm.");
        if(name==null||name.isBlank()||name.strip().length()>80)throw bad("Tên nhóm cần từ 1 đến 80 ký tự.");
        StudentGroup g=new StudentGroup();g.setGroupCode("G-"+UUID.randomUUID());g.setGroupName(name.strip());g.setLeader(me);
        groups.save(g);members.save(new GroupMember(g,me,"LEADER"));
    }
    @Transactional public void invite(String username,String code) {
        StudentGroup g=leader(username);editable(g);User u=target(code);
        if(group(u)!=null)throw bad("Sinh viên này đã thuộc một nhóm.");
        if(members.findByGroupId(g.getId()).size()>=3)throw bad("Nhóm đã đủ 3 thành viên.");
        Invitation i=invitations.findByGroupIdAndStudentId(g.getId(),u.getId()).orElseGet(Invitation::new);
        if(i.id!=null&&i.status.equals("PENDING"))throw bad("Lời mời đang chờ xác nhận.");
        i.group=g;i.student=u;i.status="PENDING";i.createdAt=LocalDateTime.now();invitations.save(i);
    }
    @Transactional public void respond(String username,Long id,boolean accept) {
        User me=student(username);em.lock(me,LockModeType.PESSIMISTIC_WRITE);
        Invitation i=invitations.findById(id).orElseThrow(this::denied);
        if(!i.student.getId().equals(me.getId()))throw denied();
        StudentGroup g=lockGroup(i.group.getId());em.refresh(i);
        if(!i.status.equals("PENDING"))throw bad("Lời mời đã được xử lý.");
        if(accept) {
            if(group(me)!=null)throw bad("Bạn đã thuộc một nhóm.");editable(g);
            int count=members.findByGroupId(g.getId()).size();
            if(count>=3)throw bad("Nhóm đã đủ 3 thành viên.");
            members.save(new GroupMember(g,me,"MEMBER"));g.setMemberCount(count+1);i.status="ACCEPTED";
            invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(me.getId(),"PENDING").stream()
                .filter(other->!other.id.equals(i.id)).forEach(other->other.status="DECLINED");
        }else i.status="DECLINED";
    }
    @Transactional public void transfer(String username,String code) {
        StudentGroup g=leader(username);User u=target(code);
        var list=members.findByGroupId(g.getId());
        if(list.stream().noneMatch(m->m.getStudent().getId().equals(u.getId())))throw bad("Nhóm trưởng mới phải thuộc nhóm.");
        g.setLeader(u);list.forEach(m->m.setMemberRole(m.getStudent().getId().equals(u.getId())?"LEADER":"MEMBER"));
    }
    @Transactional public void register(String username,String code) {
        StudentGroup g=leader(username);editable(g);
        Topic t=topics.findByTopicCode(code).orElseThrow(()->bad("Không tìm thấy đề tài."));
        em.lock(t,LockModeType.PESSIMISTIC_WRITE);em.refresh(t);
        if(t.getStatus()!=TopicStatus.APPROVED)throw bad("Đề tài chưa được công bố.");
        TopicPolicy.registration(t.getPeriod(),t.getTopicType(),true);
        if(members.findByGroupId(g.getId()).size()>t.getMaxStudents())throw bad("Nhóm vượt số thành viên cho phép của đề tài.");
        if(groups.findByTopicId(t.getId()).stream().anyMatch(x->x.getStatus()!=GroupStatus.REJECTED))throw bad("Đề tài đã có nhóm đăng ký.");
        g.setTopic(t);g.setStatus(GroupStatus.PENDING);g.setNotes(null);g.setRegisteredAt(LocalDateTime.now());
    }
    @Transactional public void upload(String username,String stage,String note,MultipartFile file)throws IOException {
        StudentGroup g=leader(username);
        if(g.getStatus()!=GroupStatus.APPROVED)throw bad("Chỉ nộp báo cáo sau khi nhóm được duyệt.");
        if(!List.of("Đề cương","Giữa kỳ","Cuối kỳ").contains(stage))throw bad("Loại báo cáo không hợp lệ.");
        if(note==null||note.length()>1000)throw bad("Ghi chú tối đa 1.000 ký tự.");
        if(file.isEmpty()||file.getSize()>10*1024*1024)throw bad("Chọn tệp PDF hoặc DOCX, tối đa 10 MB.");
        String filename=Objects.toString(file.getOriginalFilename(),"report").replace('\\','/');
        filename=filename.substring(filename.lastIndexOf('/')+1).replaceAll("[\\p{Cntrl}]","");
        if(filename.length()>180)throw bad("Tên tệp quá dài.");
        byte[] bytes=file.getBytes();String lower=filename.toLowerCase(Locale.ROOT),contentType;
        if(lower.endsWith(".pdf")&&bytes.length>=5&&new String(bytes,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"))contentType="application/pdf";
        else if(lower.endsWith(".docx")&&isDocx(bytes))contentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        else throw bad("Nội dung tệp không phải PDF hoặc DOCX hợp lệ.");
        Report f=new Report();f.group=g;f.submittedBy=student(username);f.filename=filename;f.contentType=contentType;
        f.stage=stage;f.note=note.strip();f.content=bytes;
        f.late=false;reports.save(f);
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

    public Report download(String username,Long id) {
        StudentGroup g=group(student(username));Report f=reports.findById(id).orElseThrow(this::denied);
        if(g==null||!g.getId().equals(f.group.getId()))throw denied();
        f.content.clone();return f;
    }
}
