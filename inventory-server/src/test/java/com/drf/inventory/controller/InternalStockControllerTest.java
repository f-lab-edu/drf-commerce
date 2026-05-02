package com.drf.inventory.controller;

import com.drf.common.exception.BusinessException;
import com.drf.inventory.common.exception.ErrorCode;
import com.drf.inventory.model.request.StockBatchReleaseRequest;
import com.drf.inventory.model.request.StockBatchReserveRequest;
import com.drf.inventory.service.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = InternalStockController.class)
class InternalStockControllerTest extends BaseControllerTest {

    @MockitoBean
    private StockService stockService;

    @Nested
    @DisplayName("재고 선점")
    class ReserveStock {

        @Test
        @DisplayName("선점 성공 - 200 반환")
        void reserveStock_success() throws Exception {
            // given
            StockBatchReserveRequest request = new StockBatchReserveRequest(
                    List.of(new StockBatchReserveRequest.StockBatchReserveItem(1L, 10L)));

            willDoNothing().given(stockService).batchReserveStock(any());

            // when & then
            mockMvc.perform(post("/internal/stocks/reserve")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER")
                            .header("Idempotency-Key", "550e8400-e29b-41d4-a716-446655440000")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 404 반환")
        void reserveStock_productNotFound() throws Exception {
            // given
            StockBatchReserveRequest request = new StockBatchReserveRequest(
                    List.of(new StockBatchReserveRequest.StockBatchReserveItem(999L, 10L)));

            willThrow(new BusinessException(ErrorCode.PRODUCT_NOT_FOUND))
                    .given(stockService).batchReserveStock(any());

            // when & then
            mockMvc.perform(post("/internal/stocks/reserve")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER")
                            .header("Idempotency-Key", "550e8400-e29b-41d4-a716-446655440000")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("재고가 부족하면 409 반환")
        void reserveStock_insufficientStock() throws Exception {
            // given
            StockBatchReserveRequest request = new StockBatchReserveRequest(
                    List.of(new StockBatchReserveRequest.StockBatchReserveItem(1L, 9999L)));

            willThrow(new BusinessException(ErrorCode.INSUFFICIENT_STOCK))
                    .given(stockService).batchReserveStock(any());

            // when & then
            mockMvc.perform(post("/internal/stocks/reserve")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER")
                            .header("Idempotency-Key", "550e8400-e29b-41d4-a716-446655440000")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(ErrorCode.INSUFFICIENT_STOCK.getMessage()));
        }
    }

    @Nested
    @DisplayName("재고 선점 해제")
    class ReleaseStock {

        @Test
        @DisplayName("해제 성공 - 200 반환")
        void releaseStock_success() throws Exception {
            // given
            StockBatchReleaseRequest request = new StockBatchReleaseRequest(
                    List.of(new StockBatchReleaseRequest.StockBatchReleaseItem(1L, 10L)));

            willDoNothing().given(stockService).batchReleaseStock(any());

            // when & then
            mockMvc.perform(post("/internal/stocks/release")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER")
                            .header("Idempotency-Key", "550e8400-e29b-41d4-a716-446655440000")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }
}
