package com.interstellar.odiro.place.controller.dto;

public class PlaceSearchResponse {
    private String title;       // 장소 이름
    private String address;     // 전체 주소
    private String roadAddress; // 도로명 주소
    private String phone;       // 전화번호
    private String category;    // 카테고리
    private String mapX;        // 경도 (Longitude)
    private String mapY;        // 위도 (Latitude)
    private String link;        // 상세 페이지 링크 (주로 네이버 제공)
    private String apiSource;   // 데이터 출처 (KAKAO, NAVER 또는 BOTH)
}
