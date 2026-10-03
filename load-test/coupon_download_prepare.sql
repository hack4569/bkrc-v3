-- 2,000 TPS를 30초간 실행할 수 있도록 부하테스트 전용 회원 60,000명을 준비합니다.
-- 로그인 API를 사용하지 않고 k6에서 JWT를 생성하므로 비밀번호는 인증에 사용되지 않습니다.
SET SESSION cte_max_recursion_depth = 60001;

INSERT INTO member (member_id, login_id, password)
WITH RECURSIVE sequence AS (
    SELECT 1 AS number
    UNION ALL
    SELECT number + 1
    FROM sequence
    WHERE number < 60000
)
SELECT 900000000000000000 + number,
       CONCAT('coupon_load_user_', LPAD(number, 4, '0')),
       'LOAD_TEST_ONLY'
FROM sequence
ON DUPLICATE KEY UPDATE member_id = VALUES(member_id);

-- 필요하면 테스트 전에 대상 쿠폰을 이미 받은 회원이 없는지 확인합니다.
-- SET @coupon_id = 1;
-- SELECT COUNT(*) FROM member_coupon
-- WHERE coupon_id = @coupon_id
--   AND member_id BETWEEN 900000000000000001 AND 900000000000060000;

-- 테스트 종료 후 전용 회원과 발급 내역을 제거해야 할 경우 아래 순서로 직접 실행합니다.
-- DELETE FROM member_coupon WHERE member_id BETWEEN 900000000000000001 AND 900000000000060000;
-- DELETE FROM member WHERE member_id BETWEEN 900000000000000001 AND 900000000000060000;
