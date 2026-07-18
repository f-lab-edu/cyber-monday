package brucehan.product.presentation.response;

import java.util.List;

public record ProductOffsetResponseDto<T>(
        List<T> content,
        int page,
        int size
) {
}
