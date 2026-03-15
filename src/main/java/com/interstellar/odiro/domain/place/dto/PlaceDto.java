package com.interstellar.odiro.domain.place.dto;

public record PlaceDto(
        String title,
        String address
) {
    public static String normalizeTitle(String title) {
        if (title == null) return "";
        return title.replaceAll("<[^>]*>", "") // 네이버 <b> 태그 제거
                .replaceAll("\\s+", " ")    // 연속 공백 정규화
                .trim();
    }

    public String getCleanTitle() {
        return title.replaceAll("\\s+", "");
    }
}
