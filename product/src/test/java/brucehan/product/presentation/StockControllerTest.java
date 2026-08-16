package brucehan.product.presentation;

import brucehan.product.domain.entity.Stock;
import brucehan.product.domain.repository.StockJpaRepository;
import brucehan.product.presentation.request.StockRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StockControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    StockJpaRepository stockJpaRepository;
    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        stockJpaRepository.deleteAll();
        stockJpaRepository.save(new Stock(100, 1L));
    }

    @Test
    void testGetStock() throws Exception {
        mockMvc.perform(get("/v1/stocks/{productId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    void testDecreaseStock() throws Exception {
        StockRequestDto requestDto = new StockRequestDto(1L, 101, 1L);
        mockMvc.perform(patch("/v1/stocks/decrease")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void testNotExistProduct() throws Exception {
        StockRequestDto requestDto = new StockRequestDto(2L, 1, 1L);
        mockMvc.perform(patch("/v1/stocks/decrease")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }


}