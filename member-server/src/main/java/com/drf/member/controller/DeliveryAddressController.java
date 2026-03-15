package com.drf.member.controller;

import com.drf.member.common.model.AuthInfo;
import com.drf.member.model.request.DeliveryAddressCreateRequest;
import com.drf.member.service.DeliveryAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members/me/delivery-addresses")
public class DeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;

    @PostMapping
    public ResponseEntity<Void> register(
            @RequestBody @Valid DeliveryAddressCreateRequest request, AuthInfo authInfo) {
        deliveryAddressService.register(request, authInfo);
        return ResponseEntity.noContent().build();
    }
}