# 상품 서비스 설계 문서 (Product Service)

## 1. 개요

상품 서비스는 상품 정보 관리와 **재고(Inventory)의 정확한 동시성 제어**를 핵심 도메인으로 담당합니다. 특히 대규모 프로모션이나 타임 세일 상황에서도 시스템의 가용성을 유지하고 데이터 정합성을 보장하기
위해 **Redis 선점 후 DB 후행 반영**이라는 하이브리드 아키텍처를 채택하고 있습니다.

## 2. 핵심 도메인 모델

### 2-1. 상품 (Product)

- **Status**: ON_SALE(판매중), OUT_OF_STOCK(품절), STOPPED(판매중지).
- **Price**: 정가와 할인가를 구분하여 관리.

### 2-2. 상품 재고 (ProductStock)

- **MySQL (Single Source of Truth)**: 최종 결제가 완료된 확정 재고를 관리합니다.
- **Redis (Performance Layer)**: 주문 생성 단계에서 실시간 경합을 처리하는 선점 재고를 관리합니다.

## 3. 재고 동시성 제어 전략 (Hybrid Inventory Management)

대량의 주문이 동시에 몰리는 상황에서 DB의 Lock 점유 시간을 최소화하고 응답 속도를 높이기 위해 재고 처리를 두 단계(Two-Phase)로 분리했습니다.

### 3-1. 1단계: 재고 선점 (Reservation - Redis)

- **시점**: 주문서 작성 및 결제 진입 시 (`OrderFacade.createOrder`)
- **기술**: Redis Lua Script
- **설계 결정**:
    - **Atomic Operation**: `GET` -> `Check` -> `DECR` 연산을 Lua 스크립트로 작성하여 Redis 내부에서 원자적으로 실행함으로써 Race Condition을 원천
      차단합니다.
    - **Performance**: 분산 락(Redisson 등)을 사용할 때 발생하는 락 획득/해제의 네트워크 오버헤드를 제거하여 처리량을 극대화했습니다.

### 3-2. 2단계: 재고 확정 (Confirmation - MySQL)

- **시점**: 결제 완료 이벤트 소비 시 (`OrderEventProcessor.processPaymentCompleted`)
- **기술**: Atomic Update Query
- **설계 결정**:
    - **Final Guard**: DB 업데이트 시 `WHERE stock >= :quantity` 조건을 포함하여, 만약 Redis와 DB 간의 수치 불일치가 발생하더라도 재고가 음수가 되는 것을
      방지합니다.
    - **Async Update**: Kafka 이벤트를 통한 비동기 처리를 통해 사용자에게 빠른 응답을 제공합니다.

## 4. 재고 흐름 및 보상 트랜잭션 (Compensation)

주문 프로세스의 각 단계에서 발생하는 실패에 대해 다음과 같은 재고 복구 전략을 수행합니다.

| 상황            | 대응 로직                            | 처리 방식                    |
|:--------------|:---------------------------------|:-------------------------|
| **재고 부족**     | 주문 생성 즉시 차단                      | 동기 (Exception)           |
| **쿠폰/결제 실패**  | 선점된 Redis 재고 복구 (`releaseStock`) | 동기 (OrderFacade 내 catch) |
| **주문 취소/반품**  | Redis 및 DB 재고 모두 복구              | 비동기 (Kafka Event)        |
| **결제 미완료 만료** | 스케줄러를 통한 PENDING 주문 일괄 복구        | 배치 (Scheduler)           |

## 5. 상세 설계 포인트 (Engineering Trade-offs)

### 5-1. 재고 동시성 제어 전략 비교 및 채택 이유

재고 관리의 정합성과 성능을 모두 잡기 위해 검토했던 3가지 전략을 비교합니다.

| 분류         | 1) Atomic SQL Update                | 2) Distributed Lock + DB             | 3) Redis Lua Reservation (채택)             |
|:-----------|:------------------------------------|:-------------------------------------|:------------------------------------------|
| **관리 주체**  | **MySQL (Single)**                  | **MySQL (Single)**                   | **Redis + MySQL (Hybrid)**                |
| **동시성 제어** | DB Row Lock (비관적)                   | Redis Distributed Lock               | Redis Lua Script (Atomic)                 |
| **장점**     | 구현이 단순하고 정합성이 완벽함.                  | 복잡한 로직 보호 및 DB 락 경합 전 줄 세우기 가능.      | **최고 수준의 처리량(Throughput)**. DB 트래픽 원천 차단. |
| **단점**     | **DB 커넥션 고갈** 및 행 잠금 경합으로 인한 성능 저하. | 락 획득/해제 오버헤드. 여전히 모든 요청이 DB I/O를 유발. | 두 저장소 간 정합성 관리(보상 트랜잭션) 비용 발생.            |

#### 상세 분석 및 채택 이유

**1) Atomic SQL Update (DB 직접 차감)**

- `UPDATE ... SET stock = stock - :qty WHERE stock >= :qty`와 같이 DB의 원자적 연산을 활용합니다. 하지만 대규모 트래픽 시 모든 주문 요청이 DB 커넥션을 점유한 채
  Row Lock 해제를 기다리게 되어, DB 병목이 시스템 전체의 장애로 번질 위험이 큽니다.

**2) Distributed Lock + Atomic Update (DB 직접 차감 + 분산락)**

- DB Row Lock 경합을 줄이기 위해 Redis 분산 락으로 애플리케이션 레벨에서 요청을 직렬화합니다. DB 부하는 다소 완화되지만, 락 획득을 위한 추가적인 네트워크 왕복 비용이 발생하며, 결국 모든 요청이
  DB에 직접 도달한다는 근본적인 한계는 여전합니다.

**3) Redis Lua Reservation (Hybrid 관리 - 현재 방식)**

- **결정적 이유**: 본 프로젝트는 **"트래픽 격리(Traffic Isolation)"**를 최우선 설계 원칙으로 합니다.
- 재고의 실시간 선점(Reservation)은 0.1ms 단위로 처리되는 Redis가 담당하게 하여 DB 부하를 원천 차단했습니다. DB는 이미 검증된 '결제 완료' 건만 비동기(Kafka)로 최종 반영하므로,
  초당 수만 건의 요청에도 안정적인 응답 속도를 유지할 수 있습니다.
- 이러한 구조는 타임 세일이나 한정판 출시와 같은 **고부하 상황에서 시스템 가용성을 보장하는 가장 확장성 있는 설계**라고 판단했습니다.

### 5-2. 멱등성 보장 (Idempotency)

`reserveStock`과 `releaseStock` 요청 시 주문 서버의 `Idempotency-Key`에 행위별 접미사(Suffix)를 붙여 관리합니다. 이를 통해 네트워크 재시도로 인한 중복 차감이나 중복
복구를 방지합니다.

## 6. 리스크 관리 및 고도화 계획 (Risk Management & Advancement)

재고 불일치가 발생할 수 있는 주요 시나리오를 분석하고, 이를 보완하기 위한 단계적 대응 전략을 수립합니다.

### 6-1. 주문 프로세스 장애 (Stock Leak)

- **리스크**: `OrderFacade`에서 선점 성공 후, 결제 단계 진입 전 서버가 다운되거나 네트워크 오류로 `releaseStock`이 호출되지 않으면 Redis 재고가 실제보다 적게 남는 현상.
- **대응 전략**:
    - **단기**: Redis 선점 시 **10~30분 TTL(Time-To-Live)**을 설정하여, 명시적 해제가 없어도 일정 시간 후 자동 복구되도록 개선.
    - **중기**: `PENDING` 상태로 방치된 주문을 추적하여 Redis 수치를 보정하는 정합성 스케줄러 도입.

### 6-2. 분산 환경의 이벤트 유실 (Inconsistency)

- **리스크**: 결제 완료 후 발행되는 Kafka 이벤트가 유실되어 DB 확정 재고가 차감되지 않거나, 중복 소비되어 이중 차감되는 현상.
- **대응 전략**:
    - **단기**: `common-module`에 구현된 **Transactional Outbox 패턴**을 모든 도메인에 적용하여 이벤트 발행의 원자성 보장.
    - **장기**: `ProcessedEvent` 멱등성 테이블을 활용한 중복 소비 방지 및 전수 조사 배치 프로세스(Reconciliation) 운영.

### 6-3. 인프라 장애 및 수동 수정 (Infrastructure & Admin)

- **리스크**: Redis 장애로 인한 데이터 휘발, 또는 운영자가 관리자 도구로 DB 재고를 직접 수정했을 때 Redis에 반영되지 않는 현상.
- **대응 전략**:
    - **가용성**: Redis 장애 시 DB에서 직접 재고를 차감하는 **Failover(Circuit Breaker)** 모드 설계.
    - **동기화**: 관리자 도구의 DB 수정 시 Redis 키를 함께 갱신하거나 삭제(Evict)하는 동기화 로직 강제화.

### 6-4. 정합성 보정 자동화 (Reconciliation)

- **계획**: 매일 새벽 DB(진실의 원천)와 Redis의 가용 재고를 전수 대조하여 오차를 수정하고, 불일치 발생 원인을 분석하는 통합 모니터링 시스템 구축.

### 6-5. 배치 처리 시의 부분 실패 (Partial Batch Failure)

- **리스크**: 여러 상품을 동시에 주문할 때, `StockBatchReserve` 호출 시 일부 상품만 재고가 부족하거나 Redis 연산 중 일부만 성공하여 데이터가 파편화되는 현상.
- **대응 전략**:
    - **Lua 스크립트 내 원자성**: 배치 선점용 Lua 스크립트를 작성하여, 모든 상품의 재고가 충분할 때만 일괄 차감하고 하나라도 부족하면 전체를 롤백(All-or-Nothing)하는 방식으로 설계.
    - **로직 예시 (Look-before-you-leap 패턴)**:
      ```lua
      -- 1. 전수 검증 (Pre-check)
      for i=1, #KEYS do
          local current = redis.call('GET', KEYS[i])
          if not current or tonumber(current) < tonumber(ARGV[i]) then
              return -1 -- 하나라도 부족하면 즉시 실패 반환 (롤백 효과)
          end
      end
      -- 2. 전수 차감 (Execution)
      for i=1, #KEYS do
          redis.call('DECRBY', KEYS[i], ARGV[i])
      end
      return 1 -- 전체 성공
      ```
    - **애플리케이션 레벨 롤백**: 만약 Redis 통신 오류로 배치가 중단될 경우, 이미 성공한 항목들에 대해 즉시 보상 트랜잭션(`releaseStock`)을 수행하는 Fail-fast 로직 강화.

## 7. Redis 키 설계 규칙

- **Stock Key**: `product:stock:{productId}` (Value: Integer)
- **Idempotency Key**: `product:idempotency:{scope}:{key}`
