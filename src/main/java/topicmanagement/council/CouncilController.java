package topicmanagement.council;
import java.math.BigDecimal;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.response.*;
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('DEAN','HEAD_OF_DEPT','LECTURER')")
@RestController
public class CouncilController {
    private final CouncilService service;
    public CouncilController(CouncilService service) {this.service=service;}
    record Assignment(@NotNull Long groupId,@NotNull Long councilId,@NotNull Long reviewerId) {}
    record Score(@NotNull BigDecimal score,String comment) {}
    @GetMapping("/api/councils") public Object state() {return service.state();}
    @GetMapping("/api/councils/page") public Object page(@RequestParam(defaultValue="0") int page,
        @RequestParam(defaultValue="20") int size) {return service.page(page,size);}
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DEAN')")
    @PostMapping("/api/councils") public Object create(@Valid @RequestBody CouncilService.CreateInput in) {service.create(in);return ok();}
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DEAN')")
    @PutMapping("/api/councils/{id}") public Object update(@PathVariable Long id, @Valid @RequestBody CouncilService.CreateInput in) {service.update(id, in);return ok();}
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DEAN')")
    @DeleteMapping("/api/councils/{id}") public Object delete(@PathVariable Long id) {service.delete(id);return ok();}
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DEAN')")
    @PostMapping("/api/councils/assignments") public Object assign(@Valid @RequestBody Assignment in) {service.assign(in.groupId(),in.councilId(),in.reviewerId());return ok();}
    @PostMapping("/api/councils/defenses/{id}/grade") public Object grade(@PathVariable Long id,@Valid @RequestBody Score in) {service.grade(id,in.score(),in.comment());return ok();}
    @PostMapping("/api/councils/defenses/{id}/finalize") public Object finish(@PathVariable Long id) {service.finalizeScore(id);return ok();}
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DEAN')")
    @PostMapping("/api/councils/defenses/{id}/publish") public Object publish(@PathVariable Long id) {service.publish(id);return ok();}
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/api/student/result") public Object result(@RequestParam(required = false) Long periodId) {return service.studentResult(periodId);}
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/api/student/result-periods") public Object resultPeriods() {return service.studentResultPeriods();}
    private Object ok(){return new MessageResponse("Đã lưu thay đổi.");}
}
