package com.nageoffer.shortlink.admin.dto.resp;

import lombok.Data;

/**
 * 查询短链接分组，返回实体对象
 */
@Data
public class ShortLinkGroupRespDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组排序
     */
    private Integer sortOrder;
}
