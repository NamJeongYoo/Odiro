package com.interstellar.odiro.domain.place.service;

import com.interstellar.odiro.domain.keyword.repository.KeywordRepository;
import com.interstellar.odiro.domain.place.model.Place;
import com.interstellar.odiro.infrastructure.api.ExternalPlaceSearchAdapter;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
public class PlaceSearchService {
    private final ExternalPlaceSearchAdapter searchAdapter;
    private final PlaceMerger placeMerger;
    private final KeywordRepository keywordRepository;

    @Transactional
    public List<Place> search(String query) {
        // 1. 인기 키워드 카운트 업데이트 (비즈니스 로직)
        updateKeywordCount(query);

        // 2. 외부 API로부터 데이터 가져오기 (비동기)
        // 여기서는 Merger에 넘기기 위해 원본 리스트를 각각 받는 것이 좋습니다.
        var kakaoPlaces = searchAdapter.fetchFromKakao(query);
        var naverPlaces = searchAdapter.fetchFromNaver(query);

        // 3. 도메인 로직을 통한 결과 믹스 및 정렬
        return placeMerger.merge(kakaoPlaces, naverPlaces);
    }

    private void updateKeywordCount(String query) {
        // 동시성을 고려한 쿼리 호출 (KeywordRepository 구현 필요)
        keywordRepository.upsertKeyword(query);
    }
}
