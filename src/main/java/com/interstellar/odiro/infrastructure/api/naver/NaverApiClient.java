package com.interstellar.odiro.infrastructure.api.naver;

import com.interstellar.odiro.infrastructure.api.naver.dto.NaverSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "naver-api", url = "https://openapi.naver.com")
public interface NaverApiClient {
    @GetMapping("/v1/search/local.json")
    NaverSearchResponse search(
            @RequestHeader("X-Naver-Client-Id") String clientId,
            @RequestHeader("X-Naver-Client-Secret") String clientSecret,
            @RequestParam("query") String query,
            @RequestParam("display") int display
    );
}
