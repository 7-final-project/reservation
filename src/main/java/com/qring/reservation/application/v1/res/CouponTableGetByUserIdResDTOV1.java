package com.qring.reservation.application.v1.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponTableGetByUserIdResDTOV1 {

    private Set<UserCoupon> userCouponSet;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserCoupon {

        private String useStatus;
        private Coupon coupon;

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Coupon {

            private Long id;
            private String name;
            private LocalDateTime openAt;
            private LocalDateTime expiredAt;
            private String couponStatus;
            private String issuanceStatus;

        }
    }
}
