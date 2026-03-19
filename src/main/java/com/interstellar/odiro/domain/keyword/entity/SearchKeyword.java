package com.interstellar.odiro.domain.keyword.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "search_keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자 (외부 생성 방지)
public class SearchKeyword {

    @Id
    @Column(name = "keyword", nullable = false)
    private String keyword; // 검색어 (Primary Key)

    @Column(name = "count", nullable = false)
    private Long count; // 검색 횟수

    // 정적 팩토리 메서드 또는 생성자
    public SearchKeyword(String keyword) {
        validateKeyword(keyword);
        this.keyword = keyword;
        this.count = 1L; // 처음 생성 시 1부터 시작
    }

    /**
     * 비즈니스 로직: 카운트 증가
     * 객체 지향적인 설계를 위해 엔티티 스스로 상태를 변경하게 합니다.
     */
    public void incrementCount() {
        this.count++;
    }

    // 도메인 제약 조건 검증
    private void validateKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("키워드는 비어있을 수 없습니다.");
        }
    }
}