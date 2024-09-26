package com.blackfire.aicloud.common.utils;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 树形结构
 * </p>
 *
 * @author QiuZJ
 * @date 2020年09月16日 15:25
 */
@Data
public class TreeBo<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    private Long pkId;
    /**
     * 父ID
     */
    private Long parentId;
    /**
     * 子节点列表
     */
    private List<T> children = new ArrayList<>();
}
