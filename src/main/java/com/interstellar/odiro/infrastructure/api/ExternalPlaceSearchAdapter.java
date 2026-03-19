package com.interstellar.odiro.infrastructure.api;

import com.interstellar.odiro.domain.place.model.Place;
import com.interstellar.odiro.infrastructure.api.kakao.KakaoApiClient;
import com.interstellar.odiro.infrastructure.api.naver.NaverApiClient;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@AllArgsConstructor
public class ExternalPlaceSearchAdapter {

    private final KakaoApiClient kakaoClient;
    private final NaverApiClient naverClient;

    // application.properties에서 가져온 키값들 (생성자 주입 가정)
    private final String kakaoKey;
    private final String naverId;
    private final String naverSecret;

    public List<Place> fetchAll(String query) {
        // 1. 카카오 API 비동기 호출 시작
        CompletableFuture<List<Place>> kakaoFuture = CompletableFuture.supplyAsync(() ->
                fetchFromKakao(query)
        );

        // 2. 네이버 API 비동기 호출 시작
        CompletableFuture<List<Place>> naverFuture = CompletableFuture.supplyAsync(() ->
                fetchFromNaver(query)
        );

        // 3. 둘 다 끝날 때까지 기다린 후 결과 합치기
        return CompletableFuture.allOf(kakaoFuture, naverFuture)
                .thenApply(v -> {
                    List<Place> kakaoResults = kakaoFuture.join();
                    List<Place> naverResults = naverFuture.join();

                    // 여기서 정렬 로직(PlaceMerger)을 태우기 위해 일단 리스트로 반환
                    return Stream.concat(kakaoResults.stream(), naverResults.stream())
                            .collect(Collectors.toList());
                }).join();
    }

    // ... 기존 fetchFromKakao, fetchFromNaver 메서드
    public List<Place> fetchFromKakao(String query) {
        return null;
    }

    public List<Place> fetchFromNaver(String query) {
        return null;
    }

}