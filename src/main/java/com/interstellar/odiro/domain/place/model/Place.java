package com.interstellar.odiro.domain.place.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Place {
    private final String title;     // 장소명
    private final String address;   // 전체 주소
    private final String source;    // 출처 (KAKAO, NAVER)

    // 일치 여부 확인을 위한 정규화된 이름 반환
    public String getNormalizedTitle() {
        if (title == null) return "";
        return title.replaceAll("<[^>]*>", "") // HTML 태그 제거
                .replaceAll("\\s+", "")    // 공백 제거
                .toLowerCase();
    }
}