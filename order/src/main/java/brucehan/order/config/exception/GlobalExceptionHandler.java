package brucehan.order.config.exception;

import brucehan.order.config.constant.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static brucehan.order.config.constant.ErrorCode.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, Exception e) {
        ErrorCode errorCode = SERVER_ERROR;
        log.error("서버 에러 : {}", request.getRequestURI(), e);
        return ResponseEntity.status(errorCode.getStatus()).body(new ErrorResponse(errorCode.getCode()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(HttpServletRequest request, BusinessException be) {
        ErrorCode errorCode = be.getErrorCode();
        if (errorCode == ORDER_NOT_FOUND) {
            log.warn("주문을 찾지 못함 : {} - 코드 : {}", request.getRequestURI(), be.getErrorCode());
        }
        if (errorCode == STOCK_NOT_ENOUGH) {
            log.warn("재고가 부족함 : {} - 코드 : {}", request.getRequestURI(), be.getErrorCode());
        }
        if (errorCode == PRODUCT_NOT_FOUND) {
            log.warn("상품을 찾을 수 없음 : {} - 코드 : {}", request.getRequestURI(), be.getErrorCode());
        }
        if (errorCode.getStatus().is5xxServerError()) {
            log.error("서버 에러 : {} - 코드 : {}", request.getRequestURI(), be.getErrorCode());
        }
        return ResponseEntity.status(errorCode.getStatus()).body(new ErrorResponse(errorCode.getCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(HttpServletRequest request, MethodArgumentNotValidException e) {
        ErrorCode errorCode = INVALID_REQUEST;
        log.warn("유효하지 않은 파라미터 : {} 경로의 {} ", request.getRequestURI(), e.getBindingResult());
        return ResponseEntity.status(errorCode.getStatus()).body(new ErrorResponse(errorCode.getCode()));
    }
}
