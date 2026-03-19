package com.drf.product.controller;

import com.drf.common.model.CommonResponse;
import com.drf.product.model.response.ProductDetailResponse;
import com.drf.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/products/{id}")
    public ResponseEntity<CommonResponse<ProductDetailResponse>> getProduct(@PathVariable long id) {
        return ResponseEntity.ok(CommonResponse.success(productService.getProduct(id)));
    }
}
