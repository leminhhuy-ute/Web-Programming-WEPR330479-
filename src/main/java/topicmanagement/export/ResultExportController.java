package topicmanagement.export;
import java.io.IOException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController
public class ResultExportController {
    private final ResultExportService service;
    public ResultExportController(ResultExportService service){this.service=service;}
    @GetMapping("/api/admin/exports/results.xlsx")
    public ResponseEntity<byte[]> excel(@RequestParam(required=false) Long periodId)throws IOException {
        return download(service.excel(periodId),"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","results.xlsx");
    }
    @GetMapping("/api/admin/exports/results.pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam(required=false) Long periodId)throws IOException {
        return download(service.pdf(periodId),"application/pdf","results.pdf");
    }
    @GetMapping("/api/student/result.pdf")
    public ResponseEntity<byte[]> studentPdf(@RequestParam(required=false) Long periodId)throws IOException {
        return download(service.studentPdf(periodId),"application/pdf","student-result.pdf");
    }
    private ResponseEntity<byte[]> download(byte[] bytes,String type,String file) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(type))
            .header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(file).build().toString())
            .header(HttpHeaders.CACHE_CONTROL,"no-store").body(bytes);
    }
}
