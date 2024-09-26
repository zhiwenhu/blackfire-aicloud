package com.blackfire.aicloud.provider.exception;

/**
 * 文件上传 异常信息枚举
 *
 * @Author ding ya
 * @Date: 2022年7月13日
 */
public enum FileErrorMessageEnum {
    /**
     * 上传失败,上传文件为空！
     */
    FILE_EMPTY(3001, "上传失败,上传文件为空！"),
    /**
     * 上传失败,文件类型不允许!
     */
    FILE_TYPE_ERR(3002, "上传失败,文件类型不允许!"),
    /**
     * 上传失败,上传文件为空！
     */
    FILE_SIZE_ERR(3003, "上传失败,请上传小于10MB的文件！"),
    ;
    /**
     * 错误码
     */
    private int errCode;

    /**
     * 错误信息
     */
    private String errMsg;

    FileErrorMessageEnum(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public int getErrCode() {
        return errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
