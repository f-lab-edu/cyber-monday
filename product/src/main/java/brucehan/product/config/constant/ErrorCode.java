package brucehan.product.config.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum ErrorCode {
    SERVER_ERROR("INTERNAL_SERVER_ERROR", INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("INVALID_REQUEST", BAD_REQUEST),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", NOT_FOUND),
    STOCK_NOT_ENOUGH("STOCK_NOT_ENOUGH", TOO_MANY_REQUESTS);

    private final String code;
    private final HttpStatus status;

    ErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }
}
