package com.nageoffer.shortlink.project.dto.biz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接统计实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsRecordDTO {

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 访问用户IP
     */
    private String remoteAddr;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作设备
     */
    private String device;

    /**
     * 网络
     */
    private String network;

    /**
     * UV
     */
    private String uv;

    /**
     * UV初次访问标识
     */
    private Boolean uvFirstFlag;

    /**
     * UIP初次访问标识
     */
    private Boolean uipFirstFlag;

    /**
     * UV今日初次访问标识
     */
    private Boolean uvTodayFirstFlag;

    /**
     * UIP今日初次访问标识
     */
    private Boolean uipTodayFirstFlag;
}
