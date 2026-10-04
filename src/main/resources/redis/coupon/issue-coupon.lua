-- KEYS[1]: coupon::{couponId}::state, KEYS[2]: coupon::{couponId}::members
-- ARGV: memberId, nowEpochSecond, active, publishFrom, publishUntil,
--       validFrom, validUntil, stock, initialIssuedCount
if redis.call('EXISTS', KEYS[1]) == 0 then
    redis.call('HSET', KEYS[1], 'issuedCount', ARGV[9])
end

-- 관리자가 변경할 수 있는 정책 값은 DB에서 읽은 최신 값으로 갱신한다.
redis.call('HSET', KEYS[1],
    'active', ARGV[3],
    'publishFrom', ARGV[4],
    'publishUntil', ARGV[5],
    'validFrom', ARGV[6],
    'validUntil', ARGV[7],
    'stock', ARGV[8])

local now = tonumber(ARGV[2])
if ARGV[3] ~= '1'
    or now < tonumber(ARGV[4])
    or now > tonumber(ARGV[5])
    or now < tonumber(ARGV[6])
    or now > tonumber(ARGV[7]) then
    return {-2, 0}
end

if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return {-3, 0}
end

local issuedCount = tonumber(redis.call('HGET', KEYS[1], 'issuedCount') or '0')
local stock = tonumber(ARGV[8])
if issuedCount >= stock then
    return {-4, 0}
end

redis.call('SADD', KEYS[2], ARGV[1])
local currentIssuedCount = redis.call('HINCRBY', KEYS[1], 'issuedCount', 1)
return {1, stock - currentIssuedCount}
