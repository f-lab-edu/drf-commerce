package com.drf.product.controller;

import com.drf.common.model.CommonResponse;
import com.drf.product.model.request.ProductCreateRequest;
import com.drf.product.model.response.ProductCreateResponse;
import com.drf.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminProductController {
    private final ProductService productService;

    @PostMapping("/admin/products")
    public ResponseEntity<CommonResponse<ProductCreateResponse>> crateProduct(@Valid @RequestBody ProductCreateRequest request) {
        Long productId = productService.createProduct(request);
        ProductCreateResponse response = new ProductCreateResponse(productId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
