package com.nageoffer.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 创建短链接分组，请求参数
 */
@Data
public class ShortLinkGroupSaveReqDTO {
    /**
     * 分组名
     */
    private String name;
}
