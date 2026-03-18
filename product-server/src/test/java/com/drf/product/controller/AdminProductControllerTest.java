package com.drf.product.controller;

import com.drf.product.model.request.ProductCreateRequest;
import com.drf.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(value = AdminProductController.class)
public class AdminProductControllerTest extends BaseControllerTest {

    @MockitoBean
    private ProductService productService;

    @Nested
    @DisplayName("상품 등록")
    class CreateProduct {

        @Test
        @DisplayName("등록 성공")
        void createProduct_success() throws Exception {
            ProductCreateRequest request = ProductCreateRequest.builder()
                    .categoryId(1L)
                    .name("상품명")
                    .stock(100)
                    .price(10000)
                    .description("상품 설명")
                    .discountRate(10)
                    .saleStartAt(LocalDateTime.of(2026, 3, 1, 0, 0, 0))
                    .saleEndAt(LocalDateTime.of(2026, 4, 1, 0, 0, 0))
                    .build();

            given(productService.createProduct(any(ProductCreateRequest.class))).willReturn(1L);

            mockMvc.perform(post("/admin/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "ADMIN")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1L));
        }
    }
}
