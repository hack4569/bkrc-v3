-- KEYS[1]: coupon::{couponId}::state
-- KEYS[2]: coupon::{couponId}::members
-- KEYS[3]: coupon::{couponId}::duplicate-compensations
-- ARGV[1]: memberId, ARGV[2]: rejected memberCouponId

-- RabbitMQ가 같은 이벤트를 재전송해도 동일 예약은 한 번만 보상한다.
if redis.call('SADD', KEYS[3], ARGV[2]) == 0 then
    return 0
end

local issuedCount = tonumber(redis.call('HGET', KEYS[1], 'issuedCount') or '0')
if issuedCount > 0 then
    redis.call('HINCRBY', KEYS[1], 'issuedCount', -1)
end

-- DB에는 기존 발급 내역이 있으므로 이후 중복 요청을 Redis에서 차단한다.
redis.call('SADD', KEYS[2], ARGV[1])
return 1
