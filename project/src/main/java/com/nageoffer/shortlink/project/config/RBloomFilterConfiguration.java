package com.nageoffer.shortlink.project.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.SHORT_URI_BLOOM_FILTER;

/**
 * 布隆过滤器配置
 */
@Configuration
public class RBloomFilterConfiguration {

    /**
     * 防止创建短链接查询数据库（ full_short_url 是否存在）的布隆过滤器
     */
    @Bean
    public RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> cachePenetrationBloomFilter = redissonClient.getBloomFilter(SHORT_URI_BLOOM_FILTER);
        cachePenetrationBloomFilter.tryInit(100000000L, 0.001); // 用户量：一亿   误判率：0.1%
        return cachePenetrationBloomFilter;
    }
}