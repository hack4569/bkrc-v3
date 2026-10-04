USE book_recommend;

-- 부하테스트에 사용한 쿠폰 ID로 변경하세요.
SET @coupon_id = 1;
SET @first_member_id = 900000000000000001;
SET @last_member_id  = 900000000000060000;
SET @expected_count  = 60000;

-- 1. 쿠폰 재고, DB 발급 수량, 실제 member_coupon 행 수를 비교합니다.
-- RabbitMQ Consumer 처리가 끝났다면 issued_count와 actual_issued_count가 같아야 합니다.
SELECT
    c.coupon_id,
    c.stock,
    c.issued_count,
    COUNT(mc.member_coupon_id) AS actual_issued_count,
    c.issued_count = COUNT(mc.member_coupon_id) AS count_matches,
    COUNT(mc.member_coupon_id) <= c.stock AS stock_not_exceeded
FROM coupon c
LEFT JOIN member_coupon mc ON mc.coupon_id = c.coupon_id
WHERE c.coupon_id = @coupon_id
GROUP BY c.coupon_id, c.stock, c.issued_count;

-- 2. 이번 부하테스트 회원에게 발급된 건수를 확인합니다.
-- 새 쿠폰으로 60,000건이 모두 성공했다면 issued_to_load_test_members가 60,000이어야 합니다.
SELECT
    COUNT(*) AS issued_to_load_test_members,
    COUNT(DISTINCT member_id) AS distinct_issued_members,
    COUNT(*) = @expected_count AS expected_count_matches
FROM member_coupon
WHERE coupon_id = @coupon_id
  AND member_id BETWEEN @first_member_id AND @last_member_id;

-- 3. 동일 쿠폰이 같은 회원에게 두 번 이상 지급됐는지 확인합니다.
-- 정상이라면 결과가 0행입니다.
SELECT
    coupon_id,
    member_id,
    COUNT(*) AS duplicate_count
FROM member_coupon
WHERE coupon_id = @coupon_id
GROUP BY coupon_id, member_id
HAVING COUNT(*) > 1;

-- 4. 아직 RabbitMQ 발행 전인 쿠폰 Outbox 건수를 확인합니다.
-- Consumer 저장 여부가 아니라 RabbitMQ 발행 대기 여부이며, 정상적으로 발행됐다면 0이어야 합니다.
SELECT
    outbox_status,
    COUNT(*) AS outbox_count
FROM outbox
WHERE event_type = 'COUPON_ISSUED'
GROUP BY outbox_status;

-- 5. 발급된 테스트 회원이 member 테이블에 모두 존재하는지 확인합니다.
-- 외래키가 정상이라면 missing_member_count는 0입니다.
SELECT COUNT(*) AS missing_member_count
FROM member_coupon mc
LEFT JOIN member m ON m.member_id = mc.member_id
WHERE mc.coupon_id = @coupon_id
  AND mc.member_id BETWEEN @first_member_id AND @last_member_id
  AND m.member_id IS NULL;
