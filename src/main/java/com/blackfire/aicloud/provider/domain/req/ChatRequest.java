package com.blackfire.aicloud.provider.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest extends AbstractReq{
    @NotBlank(message = "发送问题不能为空")
    private String question;
    private String model;
}
