package com.xzy.usercenter.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Redission 配置
 */
@Component
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String host;
    private String port;
    private String password;

    @Bean
    public RedissonClient redissonClient() {
        // 1. 创建配置
        /*Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(3).setPassword(password);
        // 2. 创建实例
        return Redisson.create(config);*/

        //配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379").setDatabase(3).setPassword("123456");

        //创建RedissonClient对象
        return Redisson.create(config);
    }
}
