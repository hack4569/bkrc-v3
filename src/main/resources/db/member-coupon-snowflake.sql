-- 기존 member_coupon 테이블을 이미 생성한 환경에서 한 번 실행합니다.
-- 신규 환경에서는 JPA 매핑에 따라 member_coupon_id가 일반 BIGINT PK로 생성됩니다.
ALTER TABLE member_coupon MODIFY COLUMN member_coupon_id BIGINT NOT NULL;
