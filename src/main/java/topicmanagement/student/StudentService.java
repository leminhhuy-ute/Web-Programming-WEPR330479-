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
import topicmanagement.service.TopicRegistrationService;
import topicmanagement.dto.response.StudentStateResponse;
import topicmanagement.dto.response.StudentStateResponse.*;
import topicmanagement.dto.response.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final ReportService reportService;
    private final TopicRegistrationService registrationService;
    private final EntityManager em;
    private final RegistrationPeriodRepository periods;

    public StudentService(UserRepository users, StudentGroupRepository groups, GroupMemberRepository members,
            InvitationRepository invitations, TopicRepository topics, ReportService reportService,
            TopicRegistrationService registrationService, EntityManager em) {
        this(users, groups, members, invitations, topics, reportService, registrationService, em, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public StudentService(UserRepository users, StudentGroupRepository groups, GroupMemberRepository members,
            InvitationRepository invitations, TopicRepository topics, ReportService reportService,
            TopicRegistrationService registrationService, EntityManager em,
            @org.springframework.beans.factory.annotation.Autowired(required = false) RegistrationPeriodRepository periods) {
        this.users=users;this.groups=groups;this.members=members;this.invitations=invitations;
        this.topics=topics;this.reportService=reportService;this.registrationService=registrationService;this.em=em;
        this.periods=periods;
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
        var list = members.findByStudentId(user.getId());
        if (list.isEmpty()) return null;
        return list.stream()
            .max(Comparator.comparing(GroupMember::getId))
            .map(GroupMember::getGroup)
            .orElse(null);
    }
    private RegistrationPeriod currentOrLatestPeriod() {
        if (periods == null) return null;
        var active = periods.findActiveStudentPeriods(LocalDateTime.now());
        if (!active.isEmpty()) return active.getFirst();
        return periods.findFirstByOrderByIdDesc().orElse(null);
    }
    private StudentGroup lockGroup(Long id) {
        StudentGroup g=em.find(StudentGroup.class,id,LockModeType.PESSIMISTIC_WRITE);
        em.refresh(g);return g;
    }
    private StudentGroup leader(String username) {
        User me=student(username);StudentGroup found=group(me);
        if(found==null) throw bad("Bạn cần tạo hoặc tham gia một nhóm.");
        StudentGroup g=lockGroup(found.getId());
        if(!members.existsByGroupIdAndStudentId(g.getId(),g.getLeader().getId()))
            throw new IllegalStateException("Dữ liệu nhóm không hợp lệ: nhóm trưởng chưa thuộc danh sách thành viên.");
        if(!g.getLeader().getId().equals(me.getId()))throw denied();
        return g;
    }
    private void editable(StudentGroup g) {
        if(g.getStatus()!=GroupStatus.DRAFT && g.getStatus()!=GroupStatus.REJECTED)
            throw bad("Nhóm đã đăng ký đề tài. Danh sách thành viên được khóa.");
    }
    private StudentPerson person(User u) {
        return new StudentPerson(u.getUserCode(),u.getFullName(),u.getDepartment()==null?"":u.getDepartment().getName());
    }
    private StudentInvitation invitation(Invitation i) {
        return new StudentInvitation(i.id,i.group.getId(),i.group.getGroupName(),i.student.getUserCode(),
            i.student.getFullName(),i.status,i.createdAt);
    }
    private String type(Topic t) {return t.getTopicType()==RegistrationPeriodType.COURSE?"Môn học":t.getTopicType().name();}
    private StudentTopic topic(Topic t) {
        return new StudentTopic(t.getTopicCode(),t.getTitle(),Objects.toString(t.getDescription(),""),
            Objects.toString(t.getRequirements(),""),
            t.getAdvisor1()==null?"Chưa phân công":t.getAdvisor1().getFullName()+(t.getAdvisor2()==null?"":" · "+t.getAdvisor2().getFullName()),
            t.getDepartment().getName(),type(t),t.getMaxStudents(),t.getPeriod().getStudentStartAt(),
            t.getPeriod().getStudentEndAt(),t.getPeriod().getReviewDeadline(),t.getPeriod().getId(),t.getPeriod().getName());
    }
    public StudentStateResponse state(String username) {
        User me=student(username);StudentGroup g=group(me);
        StudentGroupView groupView=null;StudentRegistrationView registration=null;
        List<topicmanagement.dto.response.TopicRegistrationResponse> registrationHistory=List.of();
        List<topicmanagement.dto.response.ReportSummaryResponse> reportList=List.of();
        var invitationList=invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(me.getId(),"PENDING").stream().map(this::invitation).toList();
        if(g!=null) {
            groupView=new StudentGroupView(g.getId(),g.getGroupName(),g.getLeader().getUserCode(),3,
                members.findByGroupId(g.getId()).stream().map(m->person(m.getStudent())).toList(),
                invitations.findByGroupIdOrderByCreatedAtDesc(g.getId()).stream().map(this::invitation).toList(),
                g.getPeriod() != null ? g.getPeriod().getId() : g.getTopic() != null ? g.getTopic().getPeriod().getId() : null,
                g.getPeriod() != null ? g.getPeriod().getName() : g.getTopic() != null ? g.getTopic().getPeriod().getName() : "Chưa gắn đợt");
            var latest=registrationService.latest(g.getId());
            if(latest.isPresent()) {
                var r=latest.get();registration=new StudentRegistrationView(r.getId(),r.getStatus().name(),r.getCreatedAt(),r.getReason(),topic(r.getTopic()));
                registrationHistory=registrationService.listForGroup(g.getId());
            } else if(g.getTopic()!=null) {
                registration=new StudentRegistrationView(g.getId(),g.getStatus().name(),g.getRegisteredAt(),g.getNotes(),topic(g.getTopic()));
            }
            reportList=reportService.listForStudent(me,g);
        }
        return new StudentStateResponse(person(me),groupView,registration,registrationHistory,reportList,invitationList);
    }
    public List<CatalogItem> catalog(String q,String department,String type) {
        return catalogPage(q,department,type,0,100).content();
    }

    public PageResponse<CatalogItem> catalogPage(String q,String department,String typeValue,int page,int size) {
        RegistrationPeriodType periodType=parseCatalogType(typeValue);
        var pageable=PageRequest.of(Math.max(0,page),Math.min(Math.max(1,size),100),
            Sort.by(Sort.Direction.DESC,"createdAt"));
        var source=topics.searchPublishedCatalog(q==null?"":q.strip(),department==null?"":department.strip(),periodType,pageable);
        var counts=activeGroupCounts(source.getContent().stream().map(Topic::getId).toList());
        var mapped=source.map(t->{
            var now=LocalDateTime.now();
            boolean open=!now.isBefore(t.getPeriod().getStudentStartAt())&&!now.isAfter(t.getPeriod().getStudentEndAt());
            return new CatalogItem(topic(t),counts.getOrDefault(t.getId(),0L),open);
        });
        return PageResponse.from(mapped);
    }

    private Map<Long,Long> activeGroupCounts(List<Long> topicIds) {
        if(topicIds.isEmpty())return Map.of();
        Map<Long,Long> result=new HashMap<>();
        groups.countActiveByTopicIds(topicIds).forEach(row->result.put((Long)row[0],(Long)row[1]));
        return result;
    }

    private RegistrationPeriodType parseCatalogType(String value) {
        if(value==null||value.isBlank())return null;
        if(value.equals("Môn học"))return RegistrationPeriodType.COURSE;
        try{return RegistrationPeriodType.valueOf(value);}catch(IllegalArgumentException ignored){return null;}
    }
    @Transactional public void createGroup(String username,String name) {
        createGroup(username, name, null);
    }
    @Transactional public void createGroup(String username,String name,Long periodId) {
        User me=student(username);em.lock(me,LockModeType.PESSIMISTIC_WRITE);
        RegistrationPeriod period = periodId == null ? currentOrLatestPeriod()
            : periods.findById(periodId).orElseThrow(() -> bad("Đợt đăng ký không tồn tại."));
        if (periodId != null) TopicPolicy.registration(period, period.getType(), true);
        if (period != null) {
            if (members.existsByRegistrationPeriodIdAndStudentId(period.getId(), me.getId()))
                throw bad("Bạn đã thuộc một nhóm trong đợt này.");
        } else if (group(me) != null) {
            throw bad("Bạn đã thuộc một nhóm.");
        }
        if(name==null||name.isBlank()||name.strip().length()>80)throw bad("Tên nhóm cần từ 1 đến 80 ký tự.");
        StudentGroup g=new StudentGroup();g.setGroupCode("G-"+UUID.randomUUID());g.setGroupName(name.strip());g.setLeader(me);
        g.setPeriod(period);
        groups.save(g);members.save(new GroupMember(g,me,"LEADER", period));
    }

    public List<PeriodChoice> periodChoices(String username) {
        User me = student(username);
        var memberships = members.findByStudentId(me.getId());
        var now = LocalDateTime.now();
        return periods.search("", null).stream().map(p -> new PeriodChoice(p.getId(), p.getName(), p.getType().name(),
            !now.isBefore(p.getStudentStartAt()) && !now.isAfter(p.getStudentEndAt()),
            memberships.stream().anyMatch(m ->
                (m.getRegistrationPeriod() != null && p.getId().equals(m.getRegistrationPeriod().getId()))
                || (m.getGroup().getPeriod() != null && p.getId().equals(m.getGroup().getPeriod().getId()))
                || (m.getGroup().getTopic() != null && p.getId().equals(m.getGroup().getTopic().getPeriod().getId())))))
            .toList();
    }
    @Transactional public void invite(String username,String code) {
        StudentGroup g=leader(username);editable(g);User u=target(code);
        RegistrationPeriod period = g.getPeriod() != null ? g.getPeriod() : currentOrLatestPeriod();
        if (period != null && members.existsByRegistrationPeriodIdAndStudentId(period.getId(), u.getId()))
            throw bad("Sinh viên này đã thuộc một nhóm trong đợt này.");
        else if (period == null && group(u) != null)
            throw bad("Sinh viên này đã thuộc một nhóm.");
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
            RegistrationPeriod period = g.getPeriod() != null ? g.getPeriod() : currentOrLatestPeriod();
            if (period != null && members.existsByRegistrationPeriodIdAndStudentId(period.getId(), me.getId()))
                throw bad("Bạn đã thuộc một nhóm trong đợt này.");
            else if (period == null && group(me) != null)
                throw bad("Bạn đã thuộc một nhóm.");
            editable(g);
            int count=members.findByGroupId(g.getId()).size();
            if(count>=3)throw bad("Nhóm đã đủ 3 thành viên.");
            members.save(new GroupMember(g,me,"MEMBER", period));g.setMemberCount(count+1);i.status="ACCEPTED";
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
    @Transactional public void leaveGroup(String username) {
        User me = student(username);
        StudentGroup g = group(me);
        if (g == null) throw bad("Bạn không thuộc nhóm nào.");
        editable(g);
        if (g.getLeader().getId().equals(me.getId())) {
            throw bad("Nhóm trưởng không thể rời nhóm. Hãy chuyển quyền nhóm trưởng hoặc giải tán nhóm.");
        }
        var mList = members.findByGroupId(g.getId());
        var myMembership = mList.stream()
            .filter(m -> m.getStudent().getId().equals(me.getId()))
            .findFirst()
            .orElseThrow(() -> bad("Không tìm thấy thông tin thành viên."));
        g.getMembers().removeIf(m -> m.getStudent().getId().equals(me.getId()));
        members.delete(myMembership);
        g.setMemberCount(Math.max(1, mList.size() - 1));
        groups.save(g);
    }
    @Transactional public void removeMember(String username, String studentCode) {
        StudentGroup g = leader(username);
        editable(g);
        User targetUser = target(studentCode);
        if (g.getLeader().getId().equals(targetUser.getId())) {
            throw bad("Nhóm trưởng không thể tự xóa khỏi nhóm. Hãy giải tán nhóm hoặc chuyển nhóm trưởng.");
        }
        var mList = members.findByGroupId(g.getId());
        var targetMembership = mList.stream()
            .filter(m -> m.getStudent().getId().equals(targetUser.getId()))
            .findFirst()
            .orElseThrow(() -> bad("Thành viên không thuộc nhóm này."));
        g.getMembers().removeIf(m -> m.getStudent().getId().equals(targetUser.getId()));
        members.delete(targetMembership);
        g.setMemberCount(Math.max(1, mList.size() - 1));
        groups.save(g);
    }
    @Transactional public void disbandGroup(String username) {
        StudentGroup g = leader(username);
        editable(g);
        g.getMembers().clear();
        invitations.deleteByGroupId(g.getId());
        members.deleteByGroupId(g.getId());
        groups.delete(g);
    }
    @Transactional public void register(String username,String code) {
        StudentGroup g=leader(username);editable(g);
        Topic t=topics.findByTopicCode(code).orElseThrow(()->bad("Không tìm thấy đề tài."));
        em.lock(t,LockModeType.PESSIMISTIC_WRITE);em.refresh(t);
        if(t.getStatus()!=TopicStatus.APPROVED)throw bad("Đề tài chưa được công bố.");
        TopicPolicy.registration(t.getPeriod(),t.getTopicType(),true);
        if(members.findByGroupId(g.getId()).size()>t.getMaxStudents())throw bad("Nhóm vượt số thành viên cho phép của đề tài.");
        registrationService.submit(g,t,student(username));
    }
    @Transactional public void upload(String username,String stage,String note,MultipartFile file)throws IOException {
        User actor=student(username);StudentGroup g=leader(username);
        reportService.upload(actor,g,stage,note,file);
    }

    public Report download(String username,Long id) {
        return reportService.downloadForStudent(student(username),id);
    }

    @Transactional public void cancelRegistration(String username,String note) {
        User actor=student(username);StudentGroup g=leader(username);
        registrationService.cancelByStudent(g,actor,note);
    }
}
