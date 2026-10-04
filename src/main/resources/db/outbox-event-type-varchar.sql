-- 기존 MySQL outbox.event_type이 ENUM으로 생성된 환경에서 한 번 실행합니다.
-- COUPON_ISSUED처럼 새 이벤트 타입을 추가해도 스키마 변경 없이 저장할 수 있도록 문자열 컬럼으로 변경합니다.
ALTER TABLE outbox
    MODIFY COLUMN event_type VARCHAR(50) NOT NULL;
