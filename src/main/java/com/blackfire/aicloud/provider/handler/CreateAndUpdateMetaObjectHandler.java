package com.blackfire.aicloud.provider.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.blackfire.aicloud.provider.dao.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MP注入处理器
 *
 * @author zql
 * @date 2021/4/25
 */
@Slf4j
@Component
public class CreateAndUpdateMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity) {
            BaseEntity baseEntity = (BaseEntity) metaObject.getOriginalObject();
            Date current = ObjectUtil.isNotNull(baseEntity.getCreateDate())
                    ? baseEntity.getCreateDate() : new Date();
            baseEntity.setCreateDate(current);
            baseEntity.setUpdateDate(current);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity) {
            BaseEntity baseEntity = (BaseEntity) metaObject.getOriginalObject();
            Date current = new Date();
            // 更新时间填充(不管为不为空)
            baseEntity.setUpdateDate(current);
        }
    }
}
