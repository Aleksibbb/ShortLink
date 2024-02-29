package com.nageoffer.shortlink.admin.common.constant;

/**
 * 短链接后管 Redis 缓存常量类
 */
public class RedisCacheConstant {

    public static final String LOCK_USER_REGISTER_KEY = "short-link:admin:register:lock_user-register:";  // 用户注册——分布式锁
    public static final String BLOOMFILTER = "short-link:admin:register:BloomFilter";

    // token
    public static final String USER_LOGIN_KEY = "short-link:admin:login:token:";
    public static final Long USER_LOGIN_TTL = 30L;      //原本是30分钟，为了方便测试，改为3000分钟


}
