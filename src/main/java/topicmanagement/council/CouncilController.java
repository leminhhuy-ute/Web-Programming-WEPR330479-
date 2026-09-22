package topicmanagement.council;
import java.math.BigDecimal;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
@RestController
public class CouncilController {
    private final CouncilService service;
    public CouncilController(CouncilService service) {this.service=service;}
    record Assignment(@NotNull Long groupId,@NotNull Long councilId,@NotNull Long reviewerId) {}
    record Score(@NotNull BigDecimal score,String comment) {}
    @GetMapping("/api/councils") Object state() {return service.state();}
    @PostMapping("/api/councils") Object create(@Valid @RequestBody CouncilService.CreateInput in) {service.create(in);return ok();}
    @PostMapping("/api/councils/assignments") Object assign(@Valid @RequestBody Assignment in) {service.assign(in.groupId(),in.councilId(),in.reviewerId());return ok();}
    @PostMapping("/api/councils/defenses/{id}/grade") Object grade(@PathVariable Long id,@Valid @RequestBody Score in) {service.grade(id,in.score(),in.comment());return ok();}
    @PostMapping("/api/councils/defenses/{id}/finalize") Object finish(@PathVariable Long id) {service.finalizeScore(id);return ok();}
    @PostMapping("/api/councils/defenses/{id}/publish") Object publish(@PathVariable Long id) {service.publish(id);return ok();}
    @GetMapping("/api/student/result") Object result() {return service.studentResult();}
    private Object ok(){return Map.of("message","Đã lưu thay đổi.");}
}
