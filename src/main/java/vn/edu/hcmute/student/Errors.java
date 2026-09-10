package vn.edu.hcmute.student;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Map;

@RestControllerAdvice(basePackageClasses=StudentController.class)
class Errors {
    @ExceptionHandler(ResponseStatusException.class) ResponseEntity<?> business(ResponseStatusException e) {return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",e.getReason()==null?"Thao tác không hợp lệ.":e.getReason()));}
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation() {return ResponseEntity.badRequest().body(Map.of("message","Vui lòng nhập đủ thông tin và kiểm tra độ dài dữ liệu."));}
    @ExceptionHandler(MaxUploadSizeExceededException.class) ResponseEntity<?> size() {return ResponseEntity.status(413).body(Map.of("message","Tệp vượt quá giới hạn 10 MB."));}
    @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> conflict() {return ResponseEntity.status(409).body(Map.of("message","Dữ liệu vừa thay đổi. Vui lòng tải lại và thử lại."));}
}
