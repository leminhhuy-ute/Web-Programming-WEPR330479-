package vn.edu.hcmute.student;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Component @Profile("demo")
class DemoData implements CommandLineRunner {
    private final StudentRepository students;private final TopicRepository topics;private final GroupRepository groups;private final RegistrationRepository registrations;private final PasswordEncoder encoder;private final Clock clock;
    DemoData(StudentRepository s,TopicRepository t,GroupRepository g,RegistrationRepository r,PasswordEncoder e,Clock c) {students=s;topics=t;groups=g;registrations=r;encoder=e;clock=c;}
    @Override @Transactional public void run(String...args) {
        if(students.count()>0) return;
        String[] names={"Nguyễn Minh Anh","Trần Ngọc Linh","Lê Hoàng Nam","Phạm Gia Huy","Võ Thảo Nguyên","Đặng Quang Minh"};
        for(int i=0;i<names.length;i++) students.save(new Student("22110"+String.format("%03d",i+1),names[i],"22110CLST"+(i%2+1),encoder.encode("Student@123")));
        String[][] data={
            {"SE26-001","Hệ thống quản lý đề tài sinh viên","Công nghệ phần mềm","Môn học","ThS. Nguyễn Quốc Bảo","Java · Spring Boot · MySQL","Xây dựng cổng đăng ký, quản lý nhóm và theo dõi tiến độ đề tài. Sản phẩm gồm website responsive, cơ sở dữ liệu và tài liệu kiểm thử."},
            {"IS26-002","Phân tích hành vi học tập trên nền tảng số","Hệ thống thông tin","NCKH","TS. Trần Mai Phương","Python · SQL · Data analysis","Phân tích dữ liệu học tập, trực quan hóa tiến độ và đề xuất hỗ trợ sinh viên dựa trên dữ liệu."},
            {"SE26-003","Ứng dụng quản lý thư viện thông minh","Công nghệ phần mềm","TLCN","ThS. Lê Thanh Sơn","Java · Spring Boot · REST API","Quản lý danh mục sách, mượn trả, tìm kiếm và thông báo; thiết kế API cùng giao diện dễ sử dụng."},
            {"CS26-004","Nhận diện bệnh trên lá cây bằng học sâu","Khoa học máy tính","KLTN","TS. Võ Thu Hà","Python · PyTorch · Computer Vision","Thu thập dữ liệu, huấn luyện mô hình và đánh giá độ chính xác; xây dựng ứng dụng minh họa."},
            {"SE26-005","Nền tảng kết nối hoạt động tình nguyện","Công nghệ phần mềm","Môn học","ThS. Nguyễn Quốc Bảo","Java · Spring Boot · Thymeleaf","Quản lý chiến dịch, đăng ký tham gia và ghi nhận hoạt động tình nguyện trong trường."},
            {"IS26-006","Kho dữ liệu và báo cáo học vụ","Hệ thống thông tin","TLCN","TS. Trần Mai Phương","SQL · ETL · Dashboard","Xây dựng mô hình dữ liệu phục vụ tổng hợp và theo dõi các chỉ số học vụ."}
        };
        Instant now=clock.instant();
        for(String[] d:data) {Topic t=new Topic();t.id=d[0];t.title=d[1];t.department=d[2];t.type=d[3];t.supervisor=d[4];t.technologies=d[5];t.description=d[6];t.published=true;t.capacity=2;t.opensAt=now.minus(Duration.ofDays(7));t.closesAt=now.plus(Duration.ofDays(21));t.reportDueAt=now.plus(Duration.ofDays(75));topics.save(t);}
        StudentGroup g=groups.save(new StudentGroup("Nhóm Thư viện số","22110004",now));
        students.findById("22110004").orElseThrow().groupId=g.id;students.findById("22110005").orElseThrow().groupId=g.id;
        Registration r=new Registration();r.groupId=g.id;r.topicId="SE26-003";r.status="APPROVED";r.submittedAt=now.minus(Duration.ofDays(2));r.feedback="Dữ liệu demo: đề tài đã được giảng viên chấp thuận.";registrations.save(r);
    }
}
