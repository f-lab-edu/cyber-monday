package brucehan.order.infrastructure.product;

import brucehan.order.config.exception.BusinessException;
import brucehan.order.infrastructure.product.dto.ProductErrorApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import static brucehan.order.config.constant.ErrorCode.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductErrorTranslator {

    private final ObjectMapper objectMapper;

    public BusinessException translate(HttpClientErrorException e) {
        String responseBody = e.getResponseBodyAsString();
        if (responseBody.isBlank()) {
            log.warn("해석할 수 없는 product에러, body가 비었음 : status = {}", e.getStatusCode());
            return new BusinessException(SERVER_ERROR);
        }
        try {
            ProductErrorApiResponse errorApiResponse = objectMapper.readValue(responseBody, ProductErrorApiResponse.class);
            String businessCode = errorApiResponse.code();

            return switch (businessCode) {
                case "STOCK_NOT_ENOUGH" -> new BusinessException(STOCK_NOT_ENOUGH);
                case "PRODUCT_NOT_FOUND" -> new BusinessException(PRODUCT_NOT_FOUND);
                case null,
                default -> {
                    log.warn("해석할 수 없는 product에러 : status = {}, body code = {}", e.getStatusCode(), businessCode);
                    yield new BusinessException(SERVER_ERROR);
                }
            };
        } catch (JsonProcessingException ex) {
            log.warn("파싱 실패 ", ex);
            return new BusinessException(SERVER_ERROR);
        }
    }
}
