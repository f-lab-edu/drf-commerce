package com.drf.product.model.response;

import com.drf.product.entity.Category;

import java.util.ArrayList;
import java.util.List;

public record CategoryTreeResponse(
        Long id,
        String name,
        List<CategoryTreeResponse> children
) {
    public static CategoryTreeResponse from(Category category) {
        return new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                new ArrayList<>()
        );
    }

    public void addChild(CategoryTreeResponse child) {
        children.add(child);
    }
}


