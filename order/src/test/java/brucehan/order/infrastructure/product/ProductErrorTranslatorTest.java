package brucehan.order.infrastructure.product;

import brucehan.order.config.constant.ErrorCode;
import brucehan.order.config.exception.BusinessException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static brucehan.order.config.constant.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest
@ActiveProfiles("test")
class ProductErrorTranslatorTest {

    @Autowired
    ProductErrorTranslator productErrorTranslator;

    @ParameterizedTest
    @MethodSource("productErrors")
    void translate(HttpStatus status, String body, ErrorCode expected) {
        HttpClientErrorException e = HttpClientErrorException.create(
                status, status.getReasonPhrase(), new HttpHeaders(),
                body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8
        );
        BusinessException result = productErrorTranslator.translate(e);
        assertThat(result.getErrorCode()).isEqualTo(expected);
    }

    static Stream<Arguments> productErrors() {
        return Stream.of(
                Arguments.of(TOO_MANY_REQUESTS, "{\"code\":\"STOCK_NOT_ENOUGH\"}", STOCK_NOT_ENOUGH),
                Arguments.of(BAD_REQUEST, "{}", SERVER_ERROR),
                Arguments.of(NOT_FOUND, "{\"code\":\"PRODUCT_NOT_FOUND\"}", PRODUCT_NOT_FOUND),
                Arguments.of(BAD_REQUEST, "{\"code\":\"INVALID_REQUEST\"}", SERVER_ERROR),
                Arguments.of(CONFLICT, "{\"code\":\"SOMETHING_NEW\"}", SERVER_ERROR),
                Arguments.of(BAD_REQUEST, "", SERVER_ERROR),
                Arguments.of(BAD_REQUEST, "<html>error</html>", SERVER_ERROR),
                Arguments.of(NOT_FOUND, "{\"status\":404,\"error\":\"Not Found\"}", SERVER_ERROR),
                Arguments.of(TOO_MANY_REQUESTS, "{\"code\":\"STOCK_NOT_ENOUGH\",\"message\":\"재고 부족\"}", STOCK_NOT_ENOUGH)
        );
    }

}