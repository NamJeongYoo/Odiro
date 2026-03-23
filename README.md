# 📍 장소 통합 검색 서비스 (Odiro Place Search)

카카오 장소 API와 네이버 지역 검색 API를 통합하여 최적의 장소 정보를 제공하는 스프링 부트 기반의 검색 엔진 프로젝트입니다. 대용량 트래픽 환경에서의 안정성과 데이터 정합성을 고려하여 설계되었습니다.

---

## 🚀 핵심 설계 포인트

### 1. 고성능 병렬 검색 (Parallel Fetching)
* **비동기 처리**: `CompletableFuture`를 사용하여 카카오와 네이버 API를 동시에 호출합니다.
* **응답 시간 단축**: 순차적 호출 대비 약 **40~50%의 레이턴시(Latency) 감소** 효과를 거두었으며, 외부 API 지연이 전체 서비스 지연으로 이어지는 것을 방지합니다.



### 2. Redis 기반 동시성 제어 및 통계 (Concurrency & Stats)
* **정확한 카운팅**: 여러 서버/스레드에서 동시에 발생하는 검색 요청을 처리하기 위해 Redis의 원자적(Atomic) 연산인 `ZINCRBY`를 활용합니다.
* **실시간 랭킹**: 별도의 무거운 DB 연산 없이도 실시간 인기 검색어 순위를 $O(\log N)$의 속도로 관리합니다.

### 3. 장애 방지 설계 (Fault Tolerance)
* **API 독립성**: 특정 외부 API(예: 카카오)에 장애가 발생하거나 타임아웃이 발생해도, `try-catch` 및 예외 핸들링을 통해 **정상적인 API의 결과만이라도 즉시 반환**합니다. (Graceful Degradation)
* **연결 최적화**: `Lettuce` 커넥션 풀을 사용하여 Redis와의 연결 효율을 높였습니다.

### 4. 스마트 매칭 및 정렬 (Smart Merging)
* **매칭 알고리즘**: 상호명 내 공백 제거 및 소문자화(`cleanTitle`) 처리를 통해 서로 다른 API의 데이터 포맷 차이를 보정하고 동일 업체를 식별합니다.
* **우선순위**: 양쪽 API에서 공통으로 발견된 신뢰도 높은 장소(`BOTH`)를 리스트 최상단에 배치합니다.

---

## 🛠 기술 스택

| 구분 | 기술 |
| :--- | :--- |
| **Framework** | Spring Boot 3.2.x |
| **Data Storage** | **Redis** (Docker), MySQL |
| **Library** | Spring Data Redis, Lombok, RestTemplate |
| **Concurrency** | Java CompletableFuture |
| **Infrastructure** | Docker (Containerization) |

---

## 🏗 시스템 아키텍처



1. **Client**: `/api/places?keyword=...` 호출
2. **Service**:
    - Redis에 검색 키워드 스코어 업데이트 (`ZSet`)
    - 카카오/네이버 API 비동기 병렬 요청
3. **Merging**: 데이터 정규화 후 `BOTH` > `KAKAO` > `NAVER` 순으로 정렬 로직 수행
4. **Response**: 최종 10개의 통합 장소 리스트