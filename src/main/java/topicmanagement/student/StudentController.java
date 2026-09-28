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
import topicmanagement.dto.response.MessageResponse;


@RestController @RequestMapping("/api/student")
class StudentController {
    private final StudentService service;
    StudentController(StudentService service) {this.service=service;}
    record Name(@NotBlank @Size(max=80) String name) {}
    record Member(@NotBlank @Size(max=30) String studentId) {}
    record TopicChoice(@NotBlank @Size(max=30) String topicId) {}
    record Decision(@NotNull Boolean accept) {}
    record Cancellation(@Size(max=2000) String note) {}
    @GetMapping("/me") Object me(Principal p) {return service.state(p.getName());}
    @GetMapping("/topics") Object topics(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="") String department,@RequestParam(defaultValue="") String type) {return service.catalog(q,department,type);}
    @GetMapping("/topics/page") Object topicPage(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="") String department,
        @RequestParam(defaultValue="") String type,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return service.catalogPage(q,department,type,page,size);
    }
    @PostMapping("/groups") Object create(Principal p,@Valid @RequestBody Name input) {service.createGroup(p.getName(),input.name());return new MessageResponse("Đã tạo nhóm. Bạn là nhóm trưởng.");}
    @PostMapping("/groups/invitations") Object invite(Principal p,@Valid @RequestBody Member input) {service.invite(p.getName(),input.studentId());return new MessageResponse("Đã gửi lời mời. Thành viên cần xác nhận tham gia.");}
    @PostMapping("/invitations/{id}/response") Object respond(Principal p,@PathVariable Long id,@Valid @RequestBody Decision input) {service.respond(p.getName(),id,input.accept());return new MessageResponse(input.accept()?"Đã tham gia nhóm.":"Đã từ chối lời mời.");}
    @PostMapping("/groups/leader") Object transfer(Principal p,@Valid @RequestBody Member input) {service.transfer(p.getName(),input.studentId());return new MessageResponse("Đã chuyển quyền nhóm trưởng.");}
    @PostMapping("/groups/leave") Object leave(Principal p) {service.leaveGroup(p.getName());return new MessageResponse("Đã rời nhóm thành công.");}
    @PostMapping("/groups/members/remove") Object removeMember(Principal p,@Valid @RequestBody Member input) {service.removeMember(p.getName(),input.studentId());return new MessageResponse("Đã xóa thành viên khỏi nhóm.");}
    @PostMapping("/groups/disband") Object disband(Principal p) {service.disbandGroup(p.getName());return new MessageResponse("Đã giải tán nhóm thành công.");}
    @PostMapping("/registrations") Object register(Principal p,@Valid @RequestBody TopicChoice input) {service.register(p.getName(),input.topicId());return new MessageResponse("Đã đăng ký đề tài. Vui lòng chờ giảng viên duyệt.");}
    @PostMapping("/registrations/cancel") Object cancel(Principal p,@Valid @RequestBody Cancellation input) {service.cancelRegistration(p.getName(),input.note());return new MessageResponse("Đã hủy đăng ký và giữ lại lịch sử.");}
    @PostMapping("/reports") Object upload(Principal p,@RequestParam String stage,@RequestParam(defaultValue="") String note,@RequestParam MultipartFile file) throws java.io.IOException {service.upload(p.getName(),stage,note,file);return new MessageResponse("Đã nộp báo cáo và lưu vào lịch sử.");}
    @GetMapping("/reports/{id}/download") ResponseEntity<byte[]> download(Principal p,@PathVariable Long id) {
        Report r=service.download(p.getName(),id);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(r.filename,StandardCharsets.UTF_8).build().toString())
            .header(HttpHeaders.CACHE_CONTROL,"no-store").contentType(MediaType.parseMediaType(r.contentType)).body(r.content);
    }
}
