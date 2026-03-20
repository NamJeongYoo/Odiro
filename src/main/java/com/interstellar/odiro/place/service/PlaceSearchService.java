package com.interstellar.odiro.place.service;

import com.interstellar.odiro.place.service.dto.MergedPlaceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private final RestTemplate restTemplate;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Value("${naver.client.id}")
    private String naverClientId;

    @Value("${naver.client.secret}")
    private String naverClientSecret;

    @Value("${kakao.api.url}")
    private String kakaoUrl;

    @Value("${naver.api.url}")
    private String naverUrl;

    public List<MergedPlaceDTO> searchPlacesParallel(String keyword) {
        // 1. 카카오 URL 생성
        URI kakaoUri = UriComponentsBuilder.fromHttpUrl(kakaoUrl)
                .queryParam("query", keyword)
                .queryParam("size", 5)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        // 2. 네이버 URL 생성
        URI naverUri = UriComponentsBuilder.fromHttpUrl(naverUrl)
                .queryParam("query", keyword)
                .queryParam("display", 5)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        // 카카오 헤더 설정
        HttpHeaders kakaoHeaders = new HttpHeaders();
        kakaoHeaders.set("Authorization", "KakaoAK " + kakaoApiKey);
        kakaoHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> kakaoEntity = new HttpEntity<>(kakaoHeaders);

        // 네이버 헤더 설정
        HttpHeaders naverHeaders = new HttpHeaders();
        naverHeaders.set("X-Naver-Client-Id", naverClientId);
        naverHeaders.set("X-Naver-Client-Secret", naverClientSecret);
        naverHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> naverEntity = new HttpEntity<>(naverHeaders);

        // Async call
        CompletableFuture<Map> kakaoTask = CompletableFuture.supplyAsync(() -> {
            try {
                return restTemplate.exchange(kakaoUri, HttpMethod.GET, kakaoEntity, Map.class).getBody();
            } catch (Exception e) {
                log.error("카카오 API 호출 실패: {}", e.getMessage());
                return Collections.emptyMap();
            }
        });

        CompletableFuture<Map> naverTask = CompletableFuture.supplyAsync(() -> {
            try {
                return restTemplate.exchange(naverUri, HttpMethod.GET, naverEntity, Map.class).getBody();
            } catch (Exception e) {
                log.error("네이버 API 호출 실패: {}", e.getMessage());
                return Collections.emptyMap();
            }
        });

        // 결과 병합
        return CompletableFuture.allOf(kakaoTask, naverTask)
                .thenApply(v -> mergeResults(kakaoTask.join(), naverTask.join()))
                .join();
    }

    private List<MergedPlaceDTO> mergeResults(Map kakaoRes, Map naverRes) {
        Map<String, MergedPlaceDTO> mergedMap = new LinkedHashMap<>();

        // 카카오 처리
        if (kakaoRes != null && kakaoRes.containsKey("documents")) {
            List<Map<String, Object>> docs = (List<Map<String, Object>>) kakaoRes.get("documents");
            for (Map<String, Object> doc : docs) {
                String title = (String) doc.get("place_name");
                String cleanName = title.replaceAll("\\s+", "");
                mergedMap.put(cleanName, MergedPlaceDTO.builder()
                        .title(title)
                        .address((String) doc.get("address_name"))
                        .mapX((String) doc.get("x"))
                        .mapY((String) doc.get("y"))
                        .apiSource("KAKAO")
                        .build());
            }
        }

        // 네이버 처리
        if (naverRes != null && naverRes.containsKey("items")) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) naverRes.get("items");
            for (Map<String, Object> item : items) {
                String rawTitle = (String) item.get("title");
                String title = rawTitle.replaceAll("<[^>]*>", ""); // HTML 태그 제거
                String cleanName = title.replaceAll("\\s+", "");

                if (mergedMap.containsKey(cleanName)) {
                    mergedMap.computeIfPresent(cleanName, (k, existing) -> MergedPlaceDTO.builder()
                            .title(existing.getTitle())
                            .address(existing.getAddress())
                            .mapX(existing.getMapX())
                            .mapY(existing.getMapY())
                            .link((String) item.get("link"))
                            .apiSource("BOTH")
                            .build());
                } else {
                    mergedMap.put(cleanName, MergedPlaceDTO.builder()
                            .title(title)
                            .address((String) item.get("address"))
                            .link((String) item.get("link"))
                            .apiSource("NAVER")
                            .build());
                }
            }
        }
        return new ArrayList<>(mergedMap.values());
    }
}