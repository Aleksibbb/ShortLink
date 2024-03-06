package com.nageoffer.shortlink.project.common.constant;

/**
 * Redis Key 常量类
 */
public class RedisKeyConstant {
    /**
     * 短链接跳转前缀 Key
     */
    public static final String GOTO_SHORT_LINK_KEY = "short-link:goto:%s";

    /**
     * 短链接空值跳转前缀 Key
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "short-link:is-null:goto_%s";

    /**
     * 短链接跳转分布式锁 前缀 Key
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short-link:lock:goto:%s";

    /**
     * 短链接统计判断是否新用户缓存标识（UV）
     */
    public static final String SHORT_LINK_STATS_UV_KEY = "short-link:stats:uv:";
}
