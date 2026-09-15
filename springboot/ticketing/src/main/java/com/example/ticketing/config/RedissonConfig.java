//package com.jpa.springboot.ticketing.config; // 본인 프로젝트의 config 패키지 경로
package com.example.ticketing.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 로컬 Redis 서버 접속 정보 (IP 및 Port)
        config.useSingleServer()
              .setAddress("redis://127.0.0.1:6379");

        return Redisson.create(config);
    }
}