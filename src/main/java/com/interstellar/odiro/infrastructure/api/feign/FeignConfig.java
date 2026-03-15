package com.interstellar.odiro.infrastructure.api.feign;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.interstellar.odiro.infrastructure.api")
public class FeignConfig {
    // 공통 로깅 레벨이나 에러 디코더가 필요하면 여기에 Bean으로 등록합니다.
}