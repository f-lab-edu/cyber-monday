package brucehan.product.init;

import brucehan.product.infrastructure.ProductJpaRepository;
import brucehan.product.infrastructure.StockJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShuwdownEventListener implements ApplicationListener<ContextClosedEvent> {
    private final ProductJpaRepository productJpaRepository;
    private final StockJpaRepository stockJpaRepository;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        productJpaRepository.deleteAll();
        stockJpaRepository.deleteAll();
    }
}
