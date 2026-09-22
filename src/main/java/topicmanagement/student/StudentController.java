package topicmanagement.student;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.nio.charset.StandardCharsets;
import java.util.*;


@RestController @RequestMapping("/api/student")
class StudentController {
    private final StudentService service;
    StudentController(StudentService service) {this.service=service;}
    record Name(@NotBlank @Size(max=80) String name) {}
    record Member(@NotBlank @Size(max=30) String studentId) {}
    record TopicChoice(@NotBlank @Size(max=30) String topicId) {}
    record Decision(@NotNull Boolean accept) {}
    @GetMapping("/me") Object me(Principal p) {return service.state(p.getName());}
    @GetMapping("/topics") Object topics(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="") String department,@RequestParam(defaultValue="") String type) {return service.catalog(q,department,type);}
    @PostMapping("/groups") Object create(Principal p,@Valid @RequestBody Name input) {service.createGroup(p.getName(),input.name());return Map.of("message","Đã tạo nhóm. Bạn là nhóm trưởng.");}
    @PostMapping("/groups/invitations") Object invite(Principal p,@Valid @RequestBody Member input) {service.invite(p.getName(),input.studentId());return Map.of("message","Đã gửi lời mời. Thành viên cần xác nhận tham gia.");}
    @PostMapping("/invitations/{id}/response") Object respond(Principal p,@PathVariable Long id,@Valid @RequestBody Decision input) {service.respond(p.getName(),id,input.accept());return Map.of("message",input.accept()?"Đã tham gia nhóm.":"Đã từ chối lời mời.");}
    @PostMapping("/groups/leader") Object transfer(Principal p,@Valid @RequestBody Member input) {service.transfer(p.getName(),input.studentId());return Map.of("message","Đã chuyển quyền nhóm trưởng.");}
    @PostMapping("/registrations") Object register(Principal p,@Valid @RequestBody TopicChoice input) {service.register(p.getName(),input.topicId());return Map.of("message","Đã đăng ký đề tài. Vui lòng chờ giảng viên duyệt.");}
    @PostMapping("/reports") Object upload(Principal p,@RequestParam String stage,@RequestParam(defaultValue="") String note,@RequestParam MultipartFile file) throws java.io.IOException {service.upload(p.getName(),stage,note,file);return Map.of("message","Đã nộp báo cáo và lưu vào lịch sử.");}
    @GetMapping("/reports/{id}/download") ResponseEntity<byte[]> download(Principal p,@PathVariable Long id) {
        Report r=service.download(p.getName(),id);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(r.filename,StandardCharsets.UTF_8).build().toString())
            .header(HttpHeaders.CACHE_CONTROL,"no-store").contentType(MediaType.parseMediaType(r.contentType)).body(r.content);
    }
}
