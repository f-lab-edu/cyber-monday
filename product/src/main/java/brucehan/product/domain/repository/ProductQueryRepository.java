package brucehan.product.domain.repository;

import brucehan.product.application.dto.ProductPagedDto;
import brucehan.product.application.dto.QProductPagedDto;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static brucehan.product.domain.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Page<ProductPagedDto> findByOffset(Pageable pageable) {

        List<ProductPagedDto> content = queryFactory
                .select(new QProductPagedDto(product.id, product.name, product.price))
                .from(product)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
