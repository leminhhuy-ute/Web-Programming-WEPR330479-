package topicmanagement.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicmanagement.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  @ExceptionHandler(ResourceNotFoundException.class)
  ResponseEntity<ApiResponse<Void>> missing(ResourceNotFoundException e) {
    return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage(), null));
  }

  @ExceptionHandler(ConflictException.class)
  ResponseEntity<ApiResponse<Void>> conflict(ConflictException e) {
    return ResponseEntity.status(409).body(ApiResponse.fail(e.getMessage(), null));
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<ApiResponse<Void>> auth(AuthenticationException e) {
    return ResponseEntity.status(401).body(ApiResponse.fail(e.getMessage(), null));
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    MethodArgumentNotValidException.class,
    ConstraintViolationException.class
  })
  ResponseEntity<ApiResponse<Void>> bad(Exception e) {
    return ResponseEntity.badRequest()
        .body(
            ApiResponse.fail(
                e instanceof IllegalArgumentException ? e.getMessage() : "Dữ liệu không hợp lệ.",
                e instanceof IllegalArgumentException ? e.getMessage() : null));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ApiResponse<Void>> integrity(DataIntegrityViolationException e) {
    return ResponseEntity.status(409)
        .body(ApiResponse.fail("Dữ liệu vi phạm ràng buộc cơ sở dữ liệu.", null));
  }

  @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
  ResponseEntity<ApiResponse<Void>> concurrent(Exception e) {
    return ResponseEntity.status(409).body(ApiResponse.fail("Dữ liệu vừa được thay đổi. Vui lòng tải lại trang.", null));
  }

  @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
  ResponseEntity<ApiResponse<Void>> oversized(Exception e) {
    return ResponseEntity.status(413).body(ApiResponse.fail("Tệp vượt quá giới hạn 10 MB.", null));
  }

  @ExceptionHandler({
      org.springframework.http.converter.HttpMessageNotReadableException.class,
      org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
      org.springframework.web.bind.MissingServletRequestParameterException.class
  })
  ResponseEntity<ApiResponse<Void>> malformed(Exception e) {
    return ResponseEntity.badRequest().body(ApiResponse.fail("Dữ liệu gửi lên không đúng định dạng.", null));
  }

  @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
  ResponseEntity<ApiResponse<Void>> noResource(Exception e) {
    return ResponseEntity.status(404).body(ApiResponse.fail("Không tìm thấy trang hoặc tài nguyên.", null));
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiResponse<Void>> denied(AccessDeniedException e) {
    return ResponseEntity.status(403)
        .body(ApiResponse.fail("Bạn không có quyền truy cập tài nguyên này.", null));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiResponse<Void>> unexpected(Exception e) {
    log.error("Unhandled request failure", e);
    return ResponseEntity.status(500)
        .body(ApiResponse.fail("Không thể xử lý yêu cầu lúc này.", null));
  }
}
