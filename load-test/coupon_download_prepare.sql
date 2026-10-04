USE book_recommend;

-- 쿠폰 부하테스트용 회원 60,000명을 생성합니다.
-- 생성 범위: 900000000000000001 ~ 900000000000060000
-- 로그인 API를 사용하지 않으므로 비밀번호는 인증에 사용되지 않습니다.
-- 재실행 시 이미 존재하는 member_id 또는 login_id는 변경하지 않습니다.
INSERT INTO member (
    member_id,
    login_id,
    password,
    member_type,
    created,
    updated
)
SELECT
    900000000000000000 + numbers.seq,
    CONCAT('coupon_load_user_', LPAD(numbers.seq, 5, '0')),
    'LOAD_TEST_ONLY',
    'LOAD_TEST',
    NOW(),
    NOW()
FROM (
    SELECT
        ones.n
        + tens.n * 10
        + hundreds.n * 100
        + thousands.n * 1000
        + ten_thousands.n * 10000
        + 1 AS seq
    FROM
        (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) ones
    CROSS JOIN
        (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) tens
    CROSS JOIN
        (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) hundreds
    CROSS JOIN
        (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) thousands
    CROSS JOIN
        (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5) ten_thousands
) numbers
WHERE numbers.seq <= 60000
ON DUPLICATE KEY UPDATE member_id = member.member_id;

-- 결과가 60,000이면 정상입니다.
SELECT COUNT(*) AS load_test_member_count
FROM member
WHERE member_id BETWEEN 900000000000000001 AND 900000000000060000;

SELECT MIN(member_id) AS first_member_id,
       MAX(member_id) AS last_member_id
FROM member
WHERE member_id BETWEEN 900000000000000001 AND 900000000000060000;

-- 필요하면 테스트 전에 대상 쿠폰을 이미 받은 회원이 없는지 확인합니다.
-- SET @coupon_id = 1;
-- SELECT COUNT(*) FROM member_coupon
-- WHERE coupon_id = @coupon_id
--   AND member_id BETWEEN 900000000000000001 AND 900000000000060000;

-- 테스트 종료 후 전용 회원과 발급 내역을 제거해야 할 경우 아래 순서로 직접 실행합니다.
-- DELETE FROM member_coupon WHERE member_id BETWEEN 900000000000000001 AND 900000000000060000;
-- DELETE FROM member WHERE member_id BETWEEN 900000000000000001 AND 900000000000060000;
