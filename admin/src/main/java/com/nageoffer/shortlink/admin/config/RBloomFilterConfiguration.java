package com.nageoffer.shortlink.admin.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.nageoffer.shortlink.admin.common.constant.RedisCacheConstant.BLOOM_FILTER;


/**
 * 布隆过滤器配置
 */
@Configuration
public class RBloomFilterConfiguration {

    /**
     * 防止用户注册查询数据库的布隆过滤器
     */
    @Bean
    public RBloomFilter<String> userRegisterCachePenetrationBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> cachePenetrationBloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER);
        cachePenetrationBloomFilter.tryInit(100000000L, 0.001); // 用户量：一亿   误判率：0.1%
        return cachePenetrationBloomFilter;
    }
}