package com.bkrc.bkrcv3.coupon.application;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.coupon.domain.Coupon;
import com.bkrc.bkrcv3.coupon.domain.CouponException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
public class CouponRedisRepository {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DefaultRedisScript<List> ISSUE_SCRIPT = script(
            "redis/coupon/issue-coupon.lua", List.class);
    private static final DefaultRedisScript<Long> ROLLBACK_SCRIPT = script(
            "redis/coupon/rollback-coupon.lua", Long.class);
    private static final DefaultRedisScript<Long> COMPENSATE_DUPLICATE_SCRIPT = script(
            "redis/coupon/compensate-duplicate-coupon.lua", Long.class);

    private final StringRedisTemplate redisTemplate;

    public CouponRedisRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** Redis 발급 카운터가 이미 초기화되어 있는지 확인합니다. */
    public boolean isInitialized(Long couponId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(stateKey(couponId)));
    }

    /** Lua 한 번으로 기간, 중복, 재고 검사와 예약을 원자적으로 처리합니다. */
    public int issue(Coupon coupon, Long memberId, LocalDateTime now, long initialIssuedCount) {
        List<?> result = redisTemplate.execute(
                ISSUE_SCRIPT,
                List.of(stateKey(coupon.getCouponId()), membersKey(coupon.getCouponId())),
                String.valueOf(memberId),
                String.valueOf(epoch(now)),
                coupon.isActive() ? "1" : "0",
                String.valueOf(epoch(coupon.getPublishFrom())),
                String.valueOf(epoch(coupon.getPublishUntil())),
                String.valueOf(epoch(coupon.getValidFrom())),
                String.valueOf(epoch(coupon.getValidUntil())),
                String.valueOf(coupon.getStock()),
                String.valueOf(initialIssuedCount)
        );
        if (result == null || result.size() < 2) throw new CouponException(ErrorCode.SERVER_ERROR);

        long code = ((Number) result.get(0)).longValue();
        if (code == -2) throw new CouponException(ErrorCode.COUPON_NOT_DOWNLOADABLE);
        if (code == -3) throw new CouponException(ErrorCode.COUPON_ALREADY_ISSUED);
        if (code == -4) throw new CouponException(ErrorCode.COUPON_OUT_OF_STOCK);
        if (code != 1) throw new CouponException(ErrorCode.SERVER_ERROR);
        return ((Number) result.get(1)).intValue();
    }

    /** Redis 카운터가 없으면 DB의 실제 발급 건수를 사용합니다. */
    public int remainingStock(Coupon coupon, long databaseIssuedCount) {
        Object cached = redisTemplate.opsForHash().get(stateKey(coupon.getCouponId()), "issuedCount");
        long issuedCount = cached == null ? databaseIssuedCount : Long.parseLong(cached.toString());
        return (int) Math.max(0, coupon.getStock() - issuedCount);
    }

    /** Outbox 저장이 롤백되면 Redis 예약을 원자적으로 되돌립니다. */
    public void rollback(Long couponId, Long memberId) {
        redisTemplate.execute(ROLLBACK_SCRIPT,
                List.of(stateKey(couponId), membersKey(couponId)), String.valueOf(memberId));
    }

    /** DB에서 중복 발급이 확인되면 회원 표시는 유지하고 이번 예약의 카운터 증가만 멱등하게 취소합니다. */
    public void compensateDuplicate(Long couponId, Long memberId, Long memberCouponId) {
        redisTemplate.execute(COMPENSATE_DUPLICATE_SCRIPT,
                List.of(stateKey(couponId), membersKey(couponId), compensationsKey(couponId)),
                String.valueOf(memberId), String.valueOf(memberCouponId));
    }

    private static long epoch(LocalDateTime value) {
        return value.atZone(SEOUL).toEpochSecond();
    }

    // 중괄호 hash tag로 Redis Cluster에서도 두 키를 같은 슬롯에 배치합니다.
    private static String stateKey(Long couponId) { return "coupon::{" + couponId + "}::state"; }
    private static String membersKey(Long couponId) { return "coupon::{" + couponId + "}::members"; }
    private static String compensationsKey(Long couponId) {
        return "coupon::{" + couponId + "}::duplicate-compensations";
    }

    private static <T> DefaultRedisScript<T> script(String path, Class<T> resultType) {
        DefaultRedisScript<T> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(path));
        script.setResultType(resultType);
        return script;
    }
}
