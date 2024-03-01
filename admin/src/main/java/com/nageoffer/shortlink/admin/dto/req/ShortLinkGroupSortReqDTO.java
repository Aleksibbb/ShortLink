package com.nageoffer.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 创建短链接分组，请求参数
 */
@Data
public class ShortLinkGroupSortReqDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组排序
     */
    private Integer sortOrder;
}
