package com.interstellar.odiro.domain.place.service;

import com.interstellar.odiro.domain.place.model.Place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlaceMerger {
    /**
     * 카카오와 네이버의 검색 결과를 혼합하여 정렬된 리스트를 반환합니다.
     * 1순위: 양쪽 모두 검색된 장소 (카카오 순서 우선)
     * 2순위: 카카오에만 있는 장소
     * 3순위: 네이버에만 있는 장소
     */
    public List<Place> merge(List<Place> kakaoPlaces, List<Place> naverPlaces) {
        if (kakaoPlaces == null) kakaoPlaces = Collections.emptyList();
        if (naverPlaces == null) naverPlaces = Collections.emptyList();

        // 1. 네이버 데이터를 비교하기 쉽게 Map으로 변환 (Key: 정규화된 이름)
        // 중복 장소가 있을 수 있으므로 List로 관리하거나 첫 번째 요소만 사용
        Map<String, Place> naverMap = naverPlaces.stream()
                .collect(Collectors.toMap(
                        Place::getNormalizedTitle,
                        p -> p,
                        (existing, replacement) -> existing // 중복 시 기존 것 유지
                ));

        List<Place> common = new ArrayList<>();
        List<Place> onlyKakao = new ArrayList<>();

        // 2. 카카오 리스트를 순회하며 분류
        for (Place kakao : kakaoPlaces) {
            String normalizedTitle = kakao.getNormalizedTitle();
            if (naverMap.containsKey(normalizedTitle)) {
                common.add(kakao);
                naverMap.remove(normalizedTitle); // 공통으로 분류되었으므로 네이버 맵에서 제거
            } else {
                onlyKakao.add(kakao);
            }
        }

        // 3. 네이버 맵에 남아있는 데이터는 네이버에만 존재하는 장소
        List<Place> onlyNaver = new ArrayList<>(naverMap.values());

        // 4. 최종 리스트 결합 (공통 -> 카카오 전용 -> 네이버 전용)
        List<Place> result = new ArrayList<>();
        result.addAll(common);
        result.addAll(onlyKakao);
        result.addAll(onlyNaver);

        // 5. 최대 10개까지만 반환
        return result.stream()
                .limit(10)
                .collect(Collectors.toList());
    }
}
