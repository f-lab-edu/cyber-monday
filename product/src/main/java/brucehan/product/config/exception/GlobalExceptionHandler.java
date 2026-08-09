package brucehan.product.config.exception;

import brucehan.product.config.constant.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, Exception e) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        log.error("서버 에러 : {}", request.getRequestURI(), e);
        return ResponseEntity.status(errorCode.getStatus()).body(new ErrorResponse(errorCode.getCode()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(HttpServletRequest request, BusinessException be) {
        ErrorCode errorCode = be.getErrorCode();
        if (errorCode == ErrorCode.PRODUCT_NOT_FOUND) {
            log.warn("상품을 찾지 못함 : {} - 코드 : {}", request.getRequestURI(), be.getErrorCode(), be);
        }
        return ResponseEntity.status(errorCode.getStatus()).body(new ErrorResponse(errorCode.getCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(HttpServletRequest request, MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        log.warn("유효하지 않은 파라미터 : {} 경로의 {} ", request.getRequestURI(), e.getBindingResult());
        return ResponseEntity.status(errorCode.getStatus()).body(new ErrorResponse(errorCode.getCode()));
    }
}
