package topicmanagement.council;
import java.math.BigDecimal;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.response.*;
@RestController
public class CouncilController {
    private final CouncilService service;
    public CouncilController(CouncilService service) {this.service=service;}
    record Assignment(@NotNull Long groupId,@NotNull Long councilId,@NotNull Long reviewerId) {}
    record Score(@NotNull BigDecimal score,String comment) {}
    @GetMapping("/api/councils") Object state() {return service.state();}
    @GetMapping("/api/councils/page") Object page(@RequestParam(defaultValue="0") int page,
        @RequestParam(defaultValue="20") int size) {return service.page(page,size);}
    @PostMapping("/api/councils") Object create(@Valid @RequestBody CouncilService.CreateInput in) {service.create(in);return ok();}
    @PutMapping("/api/councils/{id}") Object update(@PathVariable Long id, @Valid @RequestBody CouncilService.CreateInput in) {service.update(id, in);return ok();}
    @DeleteMapping("/api/councils/{id}") Object delete(@PathVariable Long id) {service.delete(id);return ok();}
    @PostMapping("/api/councils/assignments") Object assign(@Valid @RequestBody Assignment in) {service.assign(in.groupId(),in.councilId(),in.reviewerId());return ok();}
    @PostMapping("/api/councils/defenses/{id}/grade") Object grade(@PathVariable Long id,@Valid @RequestBody Score in) {service.grade(id,in.score(),in.comment());return ok();}
    @PostMapping("/api/councils/defenses/{id}/finalize") Object finish(@PathVariable Long id) {service.finalizeScore(id);return ok();}
    @PostMapping("/api/councils/defenses/{id}/publish") Object publish(@PathVariable Long id) {service.publish(id);return ok();}
    @GetMapping("/api/student/result") Object result(@RequestParam(required = false) Long periodId) {return service.studentResult(periodId);}
    private Object ok(){return new MessageResponse("Đã lưu thay đổi.");}
}
