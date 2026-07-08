package brucehan.product.application.mapper;

import brucehan.product.domain.entity.Product;
import brucehan.product.presentation.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);
}
