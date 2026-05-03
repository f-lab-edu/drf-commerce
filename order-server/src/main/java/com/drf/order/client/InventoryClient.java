package com.drf.order.client;

import com.drf.common.model.CommonResponse;
import com.drf.order.client.dto.request.StockBatchLookupRequest;
import com.drf.order.client.dto.request.StockBatchReleaseRequest;
import com.drf.order.client.dto.request.StockBatchReserveRequest;
import com.drf.order.client.dto.response.InternalStockResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "inventory-client", url = "${clients.inventory-server.url}")
public interface InventoryClient {

    @GetMapping("/internal/stocks/available")
    CommonResponse<List<InternalStockResponse>> getAvailableStocks(@SpringQueryMap StockBatchLookupRequest request);

    @PostMapping("/internal/stocks/reserve")
    CommonResponse<Void> reserveStock(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody StockBatchReserveRequest request
    );

    @PostMapping("/internal/stocks/release")
    CommonResponse<Void> releaseStock(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody StockBatchReleaseRequest request
    );
}
