package com.drf.member.model.response;

import com.drf.member.entitiy.DeliveryAddress;


public record DeliveryAddressResponse(
        Long id,
        String name,
        String phone,
        String address,
        String addressDetail,
        String zipCode,
        boolean isDefault
) {
    public static DeliveryAddressResponse from(DeliveryAddress deliveryAddress) {
        return new DeliveryAddressResponse(
                deliveryAddress.getId(),
                deliveryAddress.getName(),
                deliveryAddress.getPhone(),
                deliveryAddress.getAddress(),
                deliveryAddress.getAddressDetail(),
                deliveryAddress.getZipCode(),
                deliveryAddress.isDefault()
        );
    }
}
