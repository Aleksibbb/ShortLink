package com.nageoffer.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 修改短链接分组，请求参数
 */
@Data
public class ShortLinkGroupUpdateReqDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名
     */
    private String name;
}
