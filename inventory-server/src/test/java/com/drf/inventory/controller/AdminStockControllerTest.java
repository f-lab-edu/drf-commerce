package com.drf.inventory.controller;

import com.drf.inventory.model.request.StockCreateRequest;
import com.drf.inventory.service.AdminStockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AdminStockController.class)
class AdminStockControllerTest extends BaseControllerTest {

    @MockitoBean
    private AdminStockService adminStockService;

    @Nested
    @DisplayName("재고 등록")
    class CreateStock {
        @Test
        @DisplayName("재고 등록 성공 - 200 OK를 반환한다")
        void createStock_success() throws Exception {
            // given
            StockCreateRequest request = new StockCreateRequest(1L, 100L);
            willDoNothing().given(adminStockService).createStock(any());

            // when & then
            mockMvc.perform(post("/admin/products/stocks")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "ADMIN")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("필수 파라미터 누락 시 400 Bad Request를 반환한다")
        void createStock_fail_invalidRequest() throws Exception {
            // given
            StockCreateRequest request = new StockCreateRequest(null, -1L);

            // when & then
            mockMvc.perform(post("/admin/products/stocks")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "ADMIN")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
