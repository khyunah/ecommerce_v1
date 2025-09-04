# Kafka 파티션 키 기반 이벤트 순서 보장

## 개요
동일한 도메인 객체(상품, 주문)에 대한 이벤트들의 순서를 보장하기 위해 파티션 키 전략을 구현한다.

## 파티션 키 전략

### 기본 원칙
1. **동일 집계 루트 = 동일 파티션**: 같은 productId/orderId는 같은 파티션으로 전송
2. **다른 집계 루트 = 다른 파티션**: 서로 다른 상품/주문은 병렬 처리 가능
3. **순서 보장**: 같은 파티션 내에서는 이벤트 순서가 보장됨

### 파티션 키 매핑

#### Catalog Events (catalog-events 토픽)
- **StockAdjustedEvent**: `productId` → 동일 상품의 재고 변경 순서 보장
- **LikeChangedEvent**: `productId` → 동일 상품의 좋아요 변경 순서 보장  
- **ProductViewedEvent**: `productId` → 동일 상품의 조회 이벤트 순서 보장

#### Order Events (order-events 토픽)
- **OrderPaidEvent**: `orderId` → 동일 주문의 이벤트 순서 보장

## 구현 상세

### 파티션 키 생성 로직
```java
// BaseEvent에서 파티션 키 제공
public abstract String getPartitionKey();

// 구체적인 이벤트에서 구현
public class StockAdjustedEvent extends BaseEvent {
    @Override
    public String getPartitionKey() {
        return productId.toString(); // "1", "2", "3", ...
    }
}
```

### 파티션 키 검증
- 길이 제한: 255자 이하
- 특수 문자 제외: 개행문자, 캐리지 리턴 등
- null/empty 체크

### 순서 보장 설정
```yaml
# Producer 설정 (KafkaProducerConfig)
spring:
  kafka:
    producer:
      acks: all  # 모든 replica 확인
      enable-idempotence: true  # 중복 방지
      max-in-flight-requests-per-connection: 5  # 순서 보장과 성능 균형
```

## 순서 보장 시나리오

### 시나리오 1: 상품 재고 변경
```
상품 ID: 1
이벤트 순서: 재고 10 → 재고 8 → 재고 5
파티션 키: "1" → "1" → "1"
결과: 같은 파티션에서 순서대로 처리됨
```

### 시나리오 2: 다중 상품 동시 처리
```
상품 A(ID:1): 재고 변경 → 좋아요 변경 (파티션 키: "1")
상품 B(ID:2): 재고 변경 → 조회 이벤트 (파티션 키: "2")
결과: 상품별로 독립적인 파티션에서 병렬 처리
```

### 시나리오 3: 주문 처리 순서
```
주문 ID: 100
이벤트 순서: 주문 생성 → 결제 완료 → 배송 시작
파티션 키: "100" → "100" → "100"  
결과: 같은 파티션에서 순서 보장
```

## 성능 고려사항

### 파티션 분산
- **Hot Partition 회피**: 인기 상품에 이벤트 집중 시 특정 파티션 과부하 가능
- **해결책**: 상품 ID 기반 해싱으로 분산, 필요 시 파티션 수 증가

### 처리량 vs 순서 보장
- **Trade-off**: 완전한 순서 보장 vs 높은 처리량
- **현재 전략**: 집계 루트 단위 순서 보장 (적절한 균형점)

### 파티션 수 설계
```
권장 파티션 수 = max(상품 수 / 1000, Consumer 인스턴스 수)
예시: 10만 상품 → 최소 100개 파티션
```

## 장애 상황 대응

### 파티션 키 오류
1. **Invalid Key 감지**: DefaultPartitionKeyStrategy에서 검증
2. **Fallback**: 이벤트 타입 기반 파티션 키 사용
3. **로깅**: 오류 상황 모니터링 및 알림

### 순서 오류 감지
1. **버전 필드**: 이벤트에 version 필드로 순서 추적
2. **Consumer 검증**: 처리 시 순서 오류 감지
3. **복구 방안**: 순서 오류 시 재처리 또는 수동 개입

## 모니터링 지표

### 파티션 분산 모니터링
- 파티션별 이벤트 수 분포
- Hot Partition 감지 (특정 파티션 과부하)
- 파티션 키 분산 균등성

### 순서 보장 모니터링  
- 순서 오류 발생 횟수
- 평균 이벤트 처리 지연 시간
- 파티션별 Consumer Lag

이 전략을 통해 도메인 객체별 이벤트 순서를 보장하면서도 전체적인 처리 성능을 유지할 수 있다.
