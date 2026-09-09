package vn.edu.hcmute.topicmanagement.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import vn.edu.hcmute.topicmanagement.domain.*;
import vn.edu.hcmute.topicmanagement.repository.*;
import java.time.LocalDateTime;

@Configuration
public class DemoDataConfig {
    @Bean
    @Profile("!mysql")
    CommandLineRunner demoData(LecturerRepository lecturers, CouncilRepository councils,
                               CouncilMemberRepository members, TopicRepository topics) {
        return args -> {
            Lecturer an = lecturers.save(new Lecturer("GV001", "TS. Nguyễn Hoàng An", "an.nguyen@hcmute.edu.vn", Department.SOFTWARE_ENGINEERING));
            Lecturer binh = lecturers.save(new Lecturer("GV002", "ThS. Trần Gia Bình", "binh.tran@hcmute.edu.vn", Department.INFORMATION_SYSTEMS));
            Lecturer chi = lecturers.save(new Lecturer("GV003", "TS. Lê Minh Chi", "chi.le@hcmute.edu.vn", Department.COMPUTER_SCIENCE));
            Lecturer dung = lecturers.save(new Lecturer("GV004", "ThS. Phạm Anh Dũng", "dung.pham@hcmute.edu.vn", Department.COMPUTER_NETWORKS));
            Lecturer ha = lecturers.save(new Lecturer("GV005", "PGS.TS. Võ Thu Hà", "ha.vo@hcmute.edu.vn", Department.SOFTWARE_ENGINEERING));
            Council council = councils.save(new Council("HD-KLTN-01", "Hội đồng bảo vệ KLTN 01", LocalDateTime.now().plusDays(14).withHour(8).withMinute(0), "A4-401"));
            members.save(new CouncilMember(council, ha, CouncilRole.CHAIRPERSON));
            members.save(new CouncilMember(council, chi, CouncilRole.SECRETARY));
            members.save(new CouncilMember(council, binh, CouncilRole.REVIEWER));
            council.setStatus(CouncilStatus.READY); councils.save(council);
            Topic t1 = new Topic("KLTN-001", "Hệ thống quản lý đề tài sinh viên", "Nhóm 04", an);
            t1.setCouncil(council); t1.setReviewer(binh); t1.setStatus(TopicStatus.ASSIGNED); topics.save(t1);
            topics.save(new Topic("KLTN-002", "Nền tảng phân tích dữ liệu học tập", "Nhóm 07", dung));
        };
    }
}
