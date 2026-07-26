package brucehan.product.presentation.response;

import java.util.List;

public record ProductOffsetResponseDto<T>(
        List<T> content,
        long size, // 페이지당 담을 수 있는 개수, 페이지 크기
        int currentPage, // 현재 페이지 인덱스
        int totalPages // 페이지 총 개수
) {
}
