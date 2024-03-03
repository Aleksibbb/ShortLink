package com.nageoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 创建短链接分组
     * @param groupName：分组名称
     */
    void saveGroup(String groupName);

    /**
     * 查询短链接分组
     * @return：短链接分组集合
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改短链接分组名称
     * @param requestParam
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);

    /**
     * 删除短链接分组
     */
    void deleteGroup(String gid);

    /**
     * 短链接分组排序
     * @param requestParam：短链接分组集合
     * 需求：前端将排序后的集合返回，我们只需要遍历集合，复制、更新到数据库即可
     */
    void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam);
}
