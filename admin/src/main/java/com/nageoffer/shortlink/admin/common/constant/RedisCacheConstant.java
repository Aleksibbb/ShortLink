package com.nageoffer.shortlink.admin.common.constant;

/**
 * 短链接后管 Redis 缓存常量类
 */
public class RedisCacheConstant {

    /**
     * 用户注册分布式锁
     */
    public static final String LOCK_USER_REGISTER_KEY = "short-link:admin:register:lock_user-register:";

    /**
     * 分组创建分布式锁
     */
    public static final String LOCK_GROUP_CREATE_KEY = "short-link:lock_group-create:%s";

    /**
     * 用户注册布隆过滤器
     */
    public static final String BLOOM_FILTER = "short-link:admin:register:BloomFilter";

    /**
     * 用户登录 Token Key
     */
    public static final String USER_LOGIN_KEY = "short-link:admin:login:token:";
    /**
     * 用户登录Token 有效期
     */
    public static final Long USER_LOGIN_TTL = 30L;      //原本是30分钟，为了方便测试，改为3000分钟


}
