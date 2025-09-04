-- 이벤트 멱등성 처리 테이블
CREATE TABLE event_handled (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id VARCHAR(255) NOT NULL UNIQUE COMMENT '이벤트 고유 ID',
    event_type VARCHAR(100) NOT NULL COMMENT '이벤트 타입',
    consumer_name VARCHAR(100) NOT NULL COMMENT '처리한 컨슈머명',
    processed_at DATETIME(6) NOT NULL COMMENT '처리 완료 시간',
    created_at DATETIME(6) NOT NULL COMMENT '생성 시간',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 시간',
    deleted_at DATETIME(6) NULL COMMENT '삭제 시간',
    PRIMARY KEY (id),
    UNIQUE KEY uk_event_handled_event_id (event_id),
    INDEX idx_event_handled_processed_at (processed_at),
    INDEX idx_event_handled_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='이벤트 처리 이력 테이블';

-- 감사 로그 테이블
CREATE TABLE event_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id VARCHAR(255) NOT NULL COMMENT '이벤트 고유 ID',
    event_type VARCHAR(100) NOT NULL COMMENT '이벤트 타입',
    aggregate_id VARCHAR(100) NULL COMMENT '집계 루트 ID (productId, orderId 등)',
    aggregate_type VARCHAR(50) NULL COMMENT '집계 루트 타입 (Product, Order 등)',
    payload TEXT NOT NULL COMMENT '이벤트 데이터 (JSON)',
    event_occurred_at DATETIME(6) NOT NULL COMMENT '이벤트 발생 시간',
    created_at DATETIME(6) NOT NULL COMMENT '로그 생성 시간',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 시간',
    deleted_at DATETIME(6) NULL COMMENT '삭제 시간',
    PRIMARY KEY (id),
    INDEX idx_event_log_event_id (event_id),
    INDEX idx_event_log_event_type (event_type),
    INDEX idx_event_log_created_at (created_at),
    INDEX idx_event_log_aggregate_id (aggregate_id),
    INDEX idx_event_log_event_occurred_at (event_occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='이벤트 감사 로그 테이블';

-- 상품 메트릭스 집계 테이블
CREATE TABLE product_metrics (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '상품 ID',
    metric_date DATE NOT NULL COMMENT '집계 날짜',
    likes_count BIGINT NOT NULL DEFAULT 0 COMMENT '누적 좋아요 수',
    sales_count BIGINT NOT NULL DEFAULT 0 COMMENT '누적 판매량',
    view_count BIGINT NOT NULL DEFAULT 0 COMMENT '누적 조회 수',
    likes_delta BIGINT NOT NULL DEFAULT 0 COMMENT '일일 좋아요 증감',
    sales_delta BIGINT NOT NULL DEFAULT 0 COMMENT '일일 판매량 증감',
    views_delta BIGINT NOT NULL DEFAULT 0 COMMENT '일일 조회 수 증감',
    created_at DATETIME(6) NOT NULL COMMENT '생성 시간',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 시간',
    deleted_at DATETIME(6) NULL COMMENT '삭제 시간',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_metrics_product_date (product_id, metric_date),
    INDEX idx_product_metrics_metric_date (metric_date),
    INDEX idx_product_metrics_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상품별 일일 메트릭스 집계 테이블';
