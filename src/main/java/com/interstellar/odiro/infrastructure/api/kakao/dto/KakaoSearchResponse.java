package com.interstellar.odiro.infrastructure.api.kakao.dto;

import java.util.List;

public record KakaoSearchResponse(
        List<Document> documents
) {
    public record Document(
            String place_name,
            String address_name,
            String road_address_name
    ) {}
}