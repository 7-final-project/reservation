package com.qring.reservation.infrastructure.auditing;

import com.qring.reservation.infrastructure.util.PassportUtil;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String passport = attributes.getRequest().getHeader("X-Passport-Token");

        if (passport != null) {
            String username = PassportUtil.getUsername(passport);
            return Optional.ofNullable(username);
        }

        return Optional.of("Guest");
    }
}
