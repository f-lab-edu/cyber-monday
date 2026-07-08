package brucehan.product.presentation.response;

import lombok.Builder;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String brandName,
        String seller,
        Integer price
) {

}
