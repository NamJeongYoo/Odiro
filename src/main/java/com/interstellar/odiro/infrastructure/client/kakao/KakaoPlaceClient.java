package com.interstellar.odiro.infrastructure.client.kakao;

import com.interstellar.odiro.domain.place.dto.PlaceDto;
import com.interstellar.odiro.infrastructure.client.PlaceSearchClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KakaoPlaceClient implements PlaceSearchClient {
    private final RestTemplate restTemplate;

    @Value("${api.kakao.key}")
    private String apiKey;

    private static final String KAKAO_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";

    @Override
    public List<PlaceDto> search(String keyword, int size) {
        return null;
    }
}
