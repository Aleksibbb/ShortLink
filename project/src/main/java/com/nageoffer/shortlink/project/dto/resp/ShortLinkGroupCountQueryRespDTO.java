package com.nageoffer.shortlink.project.dto.resp;

import lombok.Data;

/**
 * 短链接分组查询数量 返回参数
 */
@Data
public class ShortLinkGroupCountQueryRespDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 短链接数量
     */
    private Integer shortLinkCount;
}
