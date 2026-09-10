package vn.edu.hcmute.student;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:module-test;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","spring.profiles.active=test"})
@AutoConfigureMockMvc
class StudentModuleTest {
    @Autowired StudentService service;
    @Autowired StudentRepository students;
    @Autowired GroupRepository groups;
    @Autowired InvitationRepository invitations;
    @Autowired TopicRepository topics;
    @Autowired RegistrationRepository registrations;
    @Autowired ReportRepository reports;
    @Autowired PasswordEncoder encoder;
    @Autowired MockMvc mvc;
    @BeforeEach void setup() {
        reports.deleteAll();invitations.deleteAll();registrations.deleteAll();students.deleteAll();groups.deleteAll();topics.deleteAll();
        for(int i=1;i<=6;i++)students.save(new Student("sv"+i,"Sinh viên "+i,"CNTT",encoder.encode("Student@123")));
        Topic t=new Topic();t.id="TEST-01";t.title="Đề tài kiểm thử";t.description="Mô tả";t.department="CNPM";t.type="Môn học";t.supervisor="GV A";t.technologies="Java";t.published=true;t.capacity=1;t.opensAt=Instant.now().minusSeconds(100);t.closesAt=Instant.now().plusSeconds(600);t.reportDueAt=Instant.now().plusSeconds(600);topics.save(t);
    }
    long invite(String target) {service.invite("sv1",target);return invitations.findByStudentIdAndStatusOrderByCreatedAtDesc(target,"PENDING").getFirst().id;}
    void pair() {service.createGroup("sv1","Nhóm thử nghiệm");service.respond("sv2",invite("sv2"),true);}
    void approve() {service.register("sv1","TEST-01");Registration r=registrations.findAll().getFirst();r.status="APPROVED";registrations.save(r);}
    @Test void groupRulesAndInvitationOwnership() {
        service.createGroup("sv1","Nhóm 1");long second=invite("sv2");
        assertThatThrownBy(()->service.respond("sv3",second,true)).isInstanceOf(ResponseStatusException.class);
        service.respond("sv2",second,true);
        assertThatThrownBy(()->service.createGroup("sv2","Nhóm khác")).hasMessageContaining("đã thuộc một nhóm");
        long third=invite("sv3"),fourth=invite("sv4");
        service.respond("sv3",third,true);
        assertThatThrownBy(()->service.respond("sv4",fourth,true)).hasMessageContaining("đủ 3");
        assertThat(students.findById("sv4").orElseThrow().groupId).isNull();
    }
    @Test void rejectedInvitationAndReinvitation() {
        service.createGroup("sv1","Nhóm 1");long id=invite("sv2");service.respond("sv2",id,false);
        assertThat(students.findById("sv2").orElseThrow().groupId).isNull();
        long same=invite("sv2");assertThat(same).isEqualTo(id);service.respond("sv2",same,true);
        assertThat(students.findById("sv2").orElseThrow().groupId).isNotNull();
    }
    @Test void leaderTransferRevokesOldAuthority() {
        pair();service.transfer("sv1","sv2");
        assertThatThrownBy(()->service.invite("sv1","sv3")).isInstanceOf(ResponseStatusException.class);
        service.invite("sv2","sv3");
        assertThatThrownBy(()->service.transfer("sv2","sv4")).hasMessageContaining("thành viên trong nhóm");
    }
    @Test void onlyLeaderCanRegisterAndMembershipLocks() {
        pair();long pending=invite("sv3");
        assertThatThrownBy(()->service.register("sv2","TEST-01")).isInstanceOf(ResponseStatusException.class);
        service.register("sv1","TEST-01");
        assertThatThrownBy(()->service.register("sv1","TEST-01")).hasMessageContaining("một đề tài");
        assertThatThrownBy(()->service.respond("sv3",pending,true)).hasMessageContaining("khóa");
        assertThat(registrations.findAll()).hasSize(1);
    }
    @Test void publishedAndTimeWindowAreEnforced() {
        service.createGroup("sv1","Nhóm 1");Topic t=topics.findById("TEST-01").orElseThrow();t.published=false;topics.save(t);
        assertThat(service.catalog("","","")).isEmpty();
        assertThatThrownBy(()->service.register("sv1","TEST-01")).isInstanceOf(ResponseStatusException.class);
        t.published=true;t.closesAt=Instant.now().minusSeconds(1);topics.save(t);
        assertThatThrownBy(()->service.register("sv1","TEST-01")).hasMessageContaining("hết hạn");
        t.closesAt=Instant.now().plusSeconds(600);t.opensAt=Instant.now().plusSeconds(100);topics.save(t);
        assertThatThrownBy(()->service.register("sv1","TEST-01")).hasMessageContaining("chưa mở");
        assertThat(registrations.count()).isZero();
    }
    @Test void rejectedRegistrationCanBeReplaced() {
        pair();service.register("sv1","TEST-01");Registration r=registrations.findAll().getFirst();r.status="REJECTED";registrations.save(r);
        service.register("sv1","TEST-01");assertThat(registrations.count()).isEqualTo(1);assertThat(registrations.findAll().getFirst().status).isEqualTo("PENDING");
    }
    @Test void concurrentLastSeatCannotBeOverbooked() throws Exception {
        service.createGroup("sv1","Nhóm 1");service.createGroup("sv2","Nhóm 2");
        try(ExecutorService executor=Executors.newFixedThreadPool(2)) {
            CountDownLatch start=new CountDownLatch(1);
            var first=executor.submit(()->{start.await();try{service.register("sv1","TEST-01");return true;}catch(ResponseStatusException e){return false;}});
            var second=executor.submit(()->{start.await();try{service.register("sv2","TEST-01");return true;}catch(ResponseStatusException e){return false;}});
            start.countDown();assertThat(List.of(first.get(10,TimeUnit.SECONDS),second.get(10,TimeUnit.SECONDS))).containsExactlyInAnyOrder(true,false);
        }
        assertThat(registrations.count()).isEqualTo(1);
    }
    @Test void uploadRequiresApprovalAndLeaderAndProtectsDownloads() throws Exception {
        pair();MockMultipartFile file=new MockMultipartFile("file","bao-cao.pdf","application/pdf","%PDF-1.7\nstudent-report".getBytes());
        assertThatThrownBy(()->service.upload("sv1","Giữa kỳ","",file)).hasMessageContaining("được duyệt");approve();
        assertThatThrownBy(()->service.upload("sv2","Giữa kỳ","",file)).isInstanceOf(ResponseStatusException.class);
        service.upload("sv1","Giữa kỳ","Bản đầu tiên",file);Report r=reports.findAll().getFirst();
        assertThat(service.download("sv2",r.id).content).isEqualTo(file.getBytes());
        assertThatThrownBy(()->service.download("sv3",r.id)).isInstanceOf(ResponseStatusException.class);
        assertThat(r.late).isFalse();
    }
    @Test void badUploadAndLateReport() throws Exception {
        pair();approve();
        MockMultipartFile wrong=new MockMultipartFile("file","fake.pdf","application/pdf","<script>alert(1)</script>".getBytes());
        assertThatThrownBy(()->service.upload("sv1","Giữa kỳ","",wrong)).hasMessageContaining("không phải PDF");
        MockMultipartFile big=new MockMultipartFile("file","large.pdf","application/pdf",new byte[10*1024*1024+1]);
        assertThatThrownBy(()->service.upload("sv1","Giữa kỳ","",big)).hasMessageContaining("10 MB");
        assertThat(reports.count()).isZero();
        Topic t=topics.findById("TEST-01").orElseThrow();t.reportDueAt=Instant.now().minusSeconds(10);topics.save(t);
        service.upload("sv1","Cuối kỳ","",new MockMultipartFile("file","report.pdf","application/pdf","%PDF-1.7 test".getBytes()));
        assertThat(reports.findAll().getFirst().late).isTrue();
    }
    @Test void mvcSecurityCsrfAndLogin() throws Exception {
        mvc.perform(get("/api/student/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/student")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(post("/login").param("username","sv1").param("password","Student@123").with(csrf())).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/student"));
        mvc.perform(post("/api/student/groups").with(user("sv1").roles("STUDENT")).contentType("application/json").content("{\"name\":\"Nhóm A\"}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/student/groups").with(user("sv1").roles("STUDENT")).with(csrf()).contentType("application/json").content("{\"name\":\"Nhóm A\"}")).andExpect(status().isOk());
        mvc.perform(get("/api/student/me").with(user("sv1").roles("STUDENT"))).andExpect(status().isOk()).andExpect(jsonPath("$.student.passwordHash").doesNotExist()).andExpect(jsonPath("$.group.name").value("Nhóm A"));
        mvc.perform(post("/api/student/groups").with(user("sv2").roles("STUDENT")).with(csrf()).contentType("application/json").content("{\"name\":\"\"}")).andExpect(status().isBadRequest());
    }
}
