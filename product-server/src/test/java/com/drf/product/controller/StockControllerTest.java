package com.drf.product.controller;

import com.drf.common.exception.BusinessException;
import com.drf.product.common.exception.ErrorCode;
import com.drf.product.model.request.StockReserveRequest;
import com.drf.product.model.response.StockReserveResponse;
import com.drf.product.service.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = StockController.class)
class StockControllerTest extends BaseControllerTest {

    @MockitoBean
    private StockService stockService;

    @Nested
    @DisplayName("재고 선점")
    class ReserveStock {

        @Test
        @DisplayName("선점 성공 - 200과 남은 재고를 반환한다")
        void reserveStock_success() throws Exception {
            // given
            StockReserveRequest request = new StockReserveRequest(10);
            StockReserveResponse response = new StockReserveResponse(1L, 90);

            given(stockService.reserveProductStock(eq(1L), any(StockReserveRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/stocks/1/reserve")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.productId").value(1L))
                    .andExpect(jsonPath("$.data.remainingStock").value(90));
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 404 반환")
        void reserveStock_productNotFound() throws Exception {
            // given
            StockReserveRequest request = new StockReserveRequest(10);

            given(stockService.reserveProductStock(eq(999L), any(StockReserveRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

            // when & then
            mockMvc.perform(post("/stocks/999/reserve")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("재고가 부족하면 409 반환")
        void reserveStock_insufficientStock() throws Exception {
            // given
            StockReserveRequest request = new StockReserveRequest(9999);

            given(stockService.reserveProductStock(eq(1L), any(StockReserveRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.INSUFFICIENT_STOCK));

            // when & then
            mockMvc.perform(post("/stocks/1/reserve")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(ErrorCode.INSUFFICIENT_STOCK.getMessage()));
        }

        @Test
        @DisplayName("quantity가 0이면 400 반환")
        void reserveStock_invalidQuantity_zero() throws Exception {
            // given
            StockReserveRequest request = new StockReserveRequest(0);

            // when & then
            mockMvc.perform(post("/stocks/1/reserve")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("quantity가 null이면 400 반환")
        void reserveStock_invalidQuantity_null() throws Exception {
            // given
            String body = "{}";

            // when & then
            mockMvc.perform(post("/stocks/1/reserve")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }
}
