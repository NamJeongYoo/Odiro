package com.interstellar.odiro.infrastructure.client.naver;

import com.interstellar.odiro.domain.place.dto.PlaceDto;
import com.interstellar.odiro.infrastructure.client.PlaceSearchClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NaverPlaceClient implements PlaceSearchClient {

    @Value("${api.naver.id}")
    private String clientId;

    @Value("${api.naver.secret}")
    private String clientSecret;

    @Override
    public List<PlaceDto> search(String keyword, int size) {
        return null;
    }
}