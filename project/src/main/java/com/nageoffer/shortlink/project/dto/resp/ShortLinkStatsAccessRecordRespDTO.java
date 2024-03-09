package com.nageoffer.shortlink.project.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 短链接监控访问记录响应参数、分组短链接监控访问记录响应参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsAccessRecordRespDTO {
    /**
     * 访问时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * IP
     */
    private String ip;

    /**
     * 地区
     */
    private String locale;

    /**
     * 设备类型
     */
    private String device;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 访问网络
     */
    private String network;

    /**
     * 访客类型
     */
    private String uvType;

    /**
     * 用户信息
     */
    private String user;
}
