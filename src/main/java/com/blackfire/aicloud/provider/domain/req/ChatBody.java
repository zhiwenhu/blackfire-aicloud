package com.blackfire.aicloud.provider.domain.req;

import lombok.Data;

@Data
public class ChatBody extends AbstractReq{
    private String question;
}
