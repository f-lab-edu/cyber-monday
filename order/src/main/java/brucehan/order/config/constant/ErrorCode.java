package brucehan.order.config.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum ErrorCode {
    SERVER_ERROR("INTERNAL_SERVER_ERROR", INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("INVALID_REQUEST", BAD_REQUEST),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", NOT_FOUND),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", NOT_FOUND),
    INVALID_ORDER_STATE("INVALID_ORDER_STATE", CONFLICT),
    ORDER_IN_PROGRESS("ORDER_IN_PROGRESS", CONFLICT),
    STOCK_NOT_ENOUGH("STOCK_NOT_ENOUGH", TOO_MANY_REQUESTS),
    PRODUCT_SERVICE_UNAVAILABLE("PRODUCT_SERVICE_UNAVAILABLE", SERVICE_UNAVAILABLE);

    private final String code;
    private final HttpStatus status;

    ErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }
}