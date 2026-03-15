package com.interstellar.odiro.infrastructure.api.kakao;

import com.interstellar.odiro.infrastructure.api.kakao.dto.KakaoSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kakao-api", url = "https://dapi.kakao.com")
public interface KakaoApiClient {
    @GetMapping("/v2/local/search/keyword.json")
    KakaoSearchResponse search(
            @RequestHeader("Authorization") String apiKey,
            @RequestParam("query") String query,
            @RequestParam("size") int size
    );
}