package com.blackfire.aicloud.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author hanbaoqiang
 */
@Data
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    //错误码
    private int errorCode;
    //错误消息
    private String errorMsg;

    public BusinessException(String msg) {
        errorCode = 500;
        errorMsg = msg;
    }
}
