package com.interstellar.odiro.domain.keyword.repository;

import com.interstellar.odiro.domain.keyword.entity.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KeywordRepository extends JpaRepository<SearchKeyword, String> {

    /**
     * 키워드 카운트를 1 증가시킵니다.
     * 데이터가 없으면 새로 생성(Insert), 있으면 기존 값에 +1(Update) 합니다.
     * DB 레벨에서 원자적 연산을 수행하여 동시성 이슈를 해결합니다.
     */
    @Modifying
    @Query(value = "MERGE INTO search_keyword (keyword, count) " +
            "KEY(keyword) " +
            "VALUES (:keyword, CASE WHEN (SELECT count FROM search_keyword WHERE keyword = :keyword) IS NULL THEN 1 " +
            "ELSE (SELECT count + 1 FROM search_keyword WHERE keyword = :keyword) END)",
            nativeQuery = true)
    void upsertKeyword(@Param("keyword") String keyword);

    /**
     * 상위 10개의 인기 키워드를 조회합니다.
     */
    List<SearchKeyword> findTop10ByOrderByCountDesc();
}
