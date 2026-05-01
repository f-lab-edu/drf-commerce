package com.drf.product.controller;

import com.drf.common.exception.BusinessException;
import com.drf.common.exception.errorcode.CommonErrorCode;
import com.drf.product.common.exception.ErrorCode;
import com.drf.product.entity.ProductStatus;
import com.drf.product.model.response.ProductDetailResponse;
import com.drf.product.model.response.ProductListResponse;
import com.drf.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(value = ProductController.class)
public class ProductControllerTest extends BaseControllerTest {

    @MockitoBean
    private ProductService productService;

    @Nested
    @DisplayName("상품 상세 조회")
    class GetProduct {

        @Test
        @DisplayName("조회 성공")
        void getProduct_success() throws Exception {
            ProductDetailResponse response = new ProductDetailResponse(
                    1L,
                    "카테고리",
                    "상품명",
                    10000,
                    "상품 설명",
                    ProductStatus.READY,
                    10,
                    LocalDateTime.of(2026, 3, 1, 0, 0, 0),
                    LocalDateTime.of(2026, 4, 1, 0, 0, 0)
            );

            given(productService.getProduct(1L)).willReturn(response);

            mockMvc.perform(get("/products/1")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.categoryName").value("카테고리"))
                    .andExpect(jsonPath("$.data.name").value("상품명"))
                    .andExpect(jsonPath("$.data.price").value(10000))
                    .andExpect(jsonPath("$.data.discountRate").value(10));
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 시 404 반환")
        void getProduct_notFound() throws Exception {
            given(productService.getProduct(999L))
                    .willThrow(new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

            mockMvc.perform(get("/products/999")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()));
        }
    }

    @Nested
    @DisplayName("상품명 검색")
    class SearchProductsByName {

        @Test
        @DisplayName("검색 성공")
        void searchProductsByName_success() throws Exception {
            ProductListResponse item = new ProductListResponse(
                    1L, "카테고리", "상품명", 10000, ProductStatus.READY, 10,
                    LocalDateTime.of(2026, 1, 1, 0, 0, 0)
            );
            given(productService.searchProductsByName(anyString(), any()))
                    .willReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

            mockMvc.perform(get("/products")
                            .param("name", "상품")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].id").value(1L))
                    .andExpect(jsonPath("$.data.content[0].name").value("상품명"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("허용되지 않는 정렬 필드 사용 시 400 반환")
        void searchProductsByName_invalidSortField() throws Exception {
            mockMvc.perform(get("/products")
                            .param("name", "상품")
                            .param("sort", "name,asc")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(CommonErrorCode.INVALID_SORT_FIELD.getMessage()));
        }
    }

    @Nested
    @DisplayName("카테고리별 상품 조회")
    class GetProductsByCategory {

        @Test
        @DisplayName("조회 성공")
        void getProductsByCategory_success() throws Exception {
            ProductListResponse item = new ProductListResponse(
                    1L, "카테고리", "상품명", 10000, ProductStatus.READY, 10,
                    LocalDateTime.of(2026, 1, 1, 0, 0, 0)
            );
            given(productService.getProductsByCategory(anyLong(), any()))
                    .willReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

            mockMvc.perform(get("/products/categories/1")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].id").value(1L))
                    .andExpect(jsonPath("$.data.content[0].categoryName").value("카테고리"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 조회 시 404 반환")
        void getProductsByCategory_notFound() throws Exception {
            given(productService.getProductsByCategory(anyLong(), any()))
                    .willThrow(new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

            mockMvc.perform(get("/products/categories/999")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ErrorCode.CATEGORY_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("허용되지 않는 정렬 필드 사용 시 400 반환")
        void getProductsByCategory_invalidSortField() throws Exception {
            mockMvc.perform(get("/products/categories/1")
                            .param("sort", "name,asc")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(CommonErrorCode.INVALID_SORT_FIELD.getMessage()));
        }
    }
}
