-- Outbox 저장 트랜잭션이 롤백된 경우에만 예약을 보상한다.
if redis.call('SREM', KEYS[2], ARGV[1]) == 1 then
    local issuedCount = tonumber(redis.call('HGET', KEYS[1], 'issuedCount') or '0')
    if issuedCount > 0 then
        redis.call('HINCRBY', KEYS[1], 'issuedCount', -1)
    end
    return 1
end
return 0
