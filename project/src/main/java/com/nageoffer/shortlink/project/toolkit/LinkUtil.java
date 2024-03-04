package com.nageoffer.shortlink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Optional;

import static com.nageoffer.shortlink.project.common.constant.ShortLinkConstant.DEFAULT_CACHE_VALID_TIME;

/**
 * 短链接工具类
 */
public class LinkUtil {
    /**
     * 获取短链接缓存有效期时间
     * @param valiDate 有效期时间
     * @return 有效期时间戳
     * 如果传入的是具体有效期，那么就在当前时间基础上，加上有效期时间
     * 如果传入的为null（永久有效），那么就加一个月
     */
    public static long getLinkCacheValidDate(Date valiDate){
        return Optional.ofNullable(valiDate)
                .map(each -> DateUtil.between(new Date(), each, DateUnit.MS))
                .orElse(DEFAULT_CACHE_VALID_TIME);
    }
}