package com.bkrc.bkrcv3.coupon.application;

import com.bkrc.bkrcv3.coupon.entity.Coupon;
import com.bkrc.bkrcv3.coupon.entity.MemberCoupon;
import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.common.shared.Snowflake;
import com.bkrc.bkrcv3.exception.BusinessException;
import com.bkrc.bkrcv3.member.application.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/** 쿠폰 노출, 발급, 보유 내역 조회 및 사용 처리를 담당합니다. */
public class CouponService {
    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final MemberRepository memberRepository;
    private final Clock couponClock;
    private final Snowflake snowflake;

    @Transactional(readOnly = true)
    public List<CouponResponse> getDownloadable() {
        // 서버 기본 타임존과 무관하게 쿠폰 정책은 한국 시각을 사용합니다.
        return couponRepository.findDownloadable(LocalDateTime.now(couponClock)).stream().map(CouponResponse::from).toList();
    }

    /**
     * 쿠폰 행 잠금부터 재고 증가와 발급 내역 저장까지 하나의 트랜잭션에서 처리합니다.
     * 따라서 여러 요청이 동시에 들어와도 재고 확인과 차감 사이에 다른 요청이 끼어들 수 없습니다.
     */
    @Transactional
    public CouponResponse.Issued download(Long couponId, Long memberId) {
        LocalDateTime now = LocalDateTime.now(couponClock);
        Coupon coupon = couponRepository.findByIdForUpdate(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        if (!coupon.isDownloadableAt(now)) throw new BusinessException(ErrorCode.COUPON_NOT_DOWNLOADABLE);
        if (memberCouponRepository.existsByCouponIdAndMemberId(couponId, memberId))
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED);
//        if (!memberRepository.existsById(memberId)) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        coupon.issue();
        try {
            // 발급 내역의 PK는 DB 자동 증가 값 대신 프로젝트 공통 Snowflake ID를 사용합니다.
            MemberCoupon issued = memberCouponRepository.save(
                    MemberCoupon.issue(snowflake.nextId(), couponId, memberId, now));
            return CouponResponse.Issued.from(issued, coupon);
        } catch (DataIntegrityViolationException e) {
            // 서로 다른 서버에서 동일 회원의 요청이 경합해도 DB 유니크 제약 위반을 업무 오류로 변환합니다.
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED, e);
        }
    }

    @Transactional(readOnly = true)
    public List<CouponResponse.Issued> getMine(Long memberId) {
        List<MemberCoupon> issuedCoupons = memberCouponRepository.findByMemberIdOrderByIssuedAtDesc(memberId);
        Map<Long, Coupon> couponsById = couponRepository.findAllById(
                        issuedCoupons.stream().map(MemberCoupon::getCouponId).distinct().toList())
                .stream().collect(Collectors.toMap(Coupon::getCouponId, Function.identity()));
        return issuedCoupons.stream()
                .filter(value -> couponsById.containsKey(value.getCouponId()))
                .map(value -> CouponResponse.Issued.from(value, couponsById.get(value.getCouponId())))
                .toList();
    }

    @Transactional
    public CouponResponse.Issued use(Long memberCouponId, Long memberId) {
        // memberId를 조건에 포함해 본인에게 발급된 쿠폰만 사용할 수 있게 합니다.
        MemberCoupon value = memberCouponRepository.findByMemberCouponIdAndMemberId(memberCouponId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_COUPON_NOT_FOUND));
        Coupon coupon = couponRepository.findById(value.getCouponId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        value.use(LocalDateTime.now(couponClock), coupon);
        return CouponResponse.Issued.from(value, coupon);
    }
}
