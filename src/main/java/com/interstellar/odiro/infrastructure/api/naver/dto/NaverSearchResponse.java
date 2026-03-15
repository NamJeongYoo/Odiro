package com.interstellar.odiro.infrastructure.api.naver.dto;

import java.util.List;

public record NaverSearchResponse(
        List<Item> items
) {
    public record Item(
            String title,
            String address,
            String roadAddress
    ) {}
}