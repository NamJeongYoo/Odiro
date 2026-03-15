package com.interstellar.odiro.infrastructure.client;

import com.interstellar.odiro.domain.place.dto.PlaceDto;

import java.util.List;

public interface PlaceSearchClient {
    List<PlaceDto> search(String keyword, int size);
}
