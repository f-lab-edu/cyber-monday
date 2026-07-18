package brucehan.product.application.mapper;

import brucehan.product.domain.entity.Product;
import brucehan.product.presentation.response.ProductResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ProductMapper {

    @Mapping(source = "stock.id", target = "stockId")
    ProductResponseDto toResponseDto(Product product);
}
