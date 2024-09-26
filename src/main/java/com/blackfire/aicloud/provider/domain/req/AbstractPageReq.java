package com.blackfire.aicloud.provider.domain.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class AbstractPageReq implements Serializable {
    //页码
    private Integer pageNum = 1;
    //每页条数
    private Integer pageSize = 15;
}
