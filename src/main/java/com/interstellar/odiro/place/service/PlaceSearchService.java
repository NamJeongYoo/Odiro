package com.interstellar.odiro.place.service;

import com.interstellar.odiro.place.service.dto.MergedPlaceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate; // Redis를 이용한 동시성 제어 및 통계 저장

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

    /**
     * 장소 검색 통합 로직 (병렬 처리 및 Redis 통계 반영)
     */
    public List<MergedPlaceDTO> searchPlacesParallel(String keyword) {
        // 1. [동시성/대용량] Redis를 이용한 키워드별 검색 횟수 증가 (Atomic 연산)
        // Redis의 increment는 싱글 스레드 기반 원자적 연산으로 동시성 이슈를 완벽히 해결합니다.
        try {
            redisTemplate.opsForZSet().incrementScore("search:ranking", keyword, 1);
        } catch (Exception e) {
            log.error("Redis 통계 저장 실패 (서비스는 계속 진행): {}", e.getMessage());
        }

        // 2. [병렬 처리] 외부 API 호출을 위한 URI 생성
        URI kakaoUri = buildUri(kakaoUrl, keyword, "size");
        URI naverUri = buildUri(naverUrl, keyword, "display");

        // 3. [장애 대응] 각 API 호출을 비동기로 실행하며 개별 예외 처리 (Circuit Breaker 역할 대체)
        CompletableFuture<Map> kakaoTask = CompletableFuture.supplyAsync(() -> fetchApi(kakaoUri, createKakaoHeaders()));
        CompletableFuture<Map> naverTask = CompletableFuture.supplyAsync(() -> fetchApi(naverUri, createNaverHeaders()));

        // 4. [데이터 병합] 두 작업이 모두 완료되면 정렬 및 매칭 로직 수행
        return CompletableFuture.allOf(kakaoTask, naverTask)
                .thenApply(v -> mergeAndSort(kakaoTask.join(), naverTask.join()))
                .join();
    }

    /**
     * 카카오와 네이버 결과를 병합하고 정렬하는 로직
     */
    private List<MergedPlaceDTO> mergeAndSort(Map kakaoRes, Map naverRes) {
        Map<String, MergedPlaceDTO> mergedMap = new LinkedHashMap<>();

        // 1. 카카오 데이터 우선 처리 (기준 데이터)
        if (kakaoRes != null && kakaoRes.get("documents") != null) {
            List<Map<String, Object>> docs = (List<Map<String, Object>>) kakaoRes.get("documents");
            for (Map<String, Object> doc : docs) {
                String title = (String) doc.get("place_name");
                String cleanName = cleanTitle(title);
                mergedMap.put(cleanName, MergedPlaceDTO.builder()
                        .title(title)
                        .address((String) doc.get("address_name"))
                        .mapX((String) doc.get("x"))
                        .mapY((String) doc.get("y"))
                        .apiSource("KAKAO")
                        .build());
            }
        }

        // 2. 네이버 데이터 병합 및 일치 여부 확인
        if (naverRes != null && naverRes.get("items") != null) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) naverRes.get("items");
            for (Map<String, Object> item : items) {
                String rawTitle = (String) item.get("title");
                String title = rawTitle.replaceAll("<[^>]*>", ""); // HTML 태그 제거
                String cleanName = cleanTitle(title);

                if (mergedMap.containsKey(cleanName)) {
                    // 일치하는 항목이 있으면 BOTH로 변경 및 정보 보강
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

        // 3. [정렬] BOTH 소스를 가진 데이터를 상단으로 정렬
        return mergedMap.values().stream()
                .sorted((p1, p2) -> {
                    if (p1.getApiSource().equals("BOTH") && !p2.getApiSource().equals("BOTH")) return -1;
                    if (!p1.getApiSource().equals("BOTH") && p2.getApiSource().equals("BOTH")) return 1;
                    return 0;
                })
                .collect(Collectors.toList());
    }

    // --- Helper Methods ---

    private String cleanTitle(String title) {
        return title.replaceAll("\\s+", "").toLowerCase(); // 공백 제거 및 소문자화로 매칭률 향상
    }

    private URI buildUri(String baseUrl, String qVal, String sKey) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("query", qVal)
                .queryParam(sKey, 5)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();
    }

    private Map fetchApi(URI uri, HttpHeaders headers) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class).getBody();
        } catch (Exception e) {
            log.error("외부 API 호출 실패 ({}): {}", uri.getHost(), e.getMessage());
            return Collections.emptyMap(); // 한쪽 API가 죽어도 서비스는 유지 (장애 전이 방지)
        }
    }

    private HttpHeaders createKakaoHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private HttpHeaders createNaverHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverClientId);
        headers.set("X-Naver-Client-Secret", naverClientSecret);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }
}