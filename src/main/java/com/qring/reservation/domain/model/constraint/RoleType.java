package com.qring.reservation.domain.model.constraint;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoleType {
    public static final String CUSTOMER = "고객";
    public static final String OWNER = "점주";
    public static final String ADMIN = "관리자";
}
