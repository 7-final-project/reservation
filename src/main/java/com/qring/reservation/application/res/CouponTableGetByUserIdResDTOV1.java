package com.qring.reservation.application.res;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class CouponTableGetByUserIdResDTOV1 {

    private Set<UserCoupon> userCouponSet;

    @Getter
    @NoArgsConstructor
    public static class UserCoupon {

        private Coupon coupon;

        @Getter
        @NoArgsConstructor
        public static class Coupon {

            // NOTE: 현재 coupon 의 ID 만 필요하므로, id 필드만 포함합니다.
            private Long id;

        }
    }

    /**
     * 특정 couponId가 userCouponSet에 존재하는지 확인합니다.
     *
     * @param couponId 확인하려는 쿠폰 ID
     * @return 해당 쿠폰이 존재하면 true, 그렇지 않으면 false
     */
    public boolean hasCoupon(Long couponId) {
        return  getCouponIdSet().contains(couponId);
    }

    private Set<Long> getCouponIdSet() {
        return this.userCouponSet.stream()
                .map(userCoupon -> userCoupon.getCoupon().getId())
                .collect(Collectors.toSet());
    }
}
