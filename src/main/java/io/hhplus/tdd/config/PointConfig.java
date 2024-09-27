package io.hhplus.tdd.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class PointConfig {

    @Value("${point.max-balance}")
    private long maxBalance; // 포인트 최대 잔고
}
