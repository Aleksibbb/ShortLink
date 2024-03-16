package com.nageoffer.shortlink.admin.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Date;

@Primary
@Component(value = "myMetaObjectHandlerByAdmin")
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入数据时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        //UserDO、GroupDO
        strictInsertFill(metaObject, "createTime", Date::new, Date.class);
        strictInsertFill(metaObject, "updateTime", Date::new, Date.class);
        strictInsertFill(metaObject, "delFlag", () -> 0, Integer.class);
    }

    /**
     * 更新数据时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        strictInsertFill(metaObject, "updateTime", Date::new, Date.class); // 起始版本 3.3.3(推荐)
    }
}
