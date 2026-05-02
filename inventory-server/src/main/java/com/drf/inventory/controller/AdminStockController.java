package com.drf.inventory.controller;

import com.drf.common.model.CommonResponse;
import com.drf.inventory.model.request.StockCreateRequest;
import com.drf.inventory.service.AdminStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/products/stocks")
public class AdminStockController {

    private final AdminStockService adminStockService;

    @PostMapping
    public ResponseEntity<CommonResponse<Void>> createStock(
            @Valid @RequestBody StockCreateRequest request
    ) {
        adminStockService.createStock(request);
        return ResponseEntity.ok(CommonResponse.success());
    }
}
