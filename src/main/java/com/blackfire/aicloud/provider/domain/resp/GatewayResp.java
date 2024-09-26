package com.blackfire.aicloud.provider.domain.resp;


import lombok.Data;

@Data
public class GatewayResp<T> {


    private int code = 0;


    private String msg = "success";


    private T data;

    /**
     * 构造方法
     */
    public GatewayResp() {
    }

    public GatewayResp(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public GatewayResp(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static GatewayResp ok() {
        return new GatewayResp(0, "success");
    }

    public static <T> GatewayResp<T> ok(T data) {
        return new GatewayResp(0, "success", data);
    }

    public static GatewayResp error() {
        return new GatewayResp(500, "服务器内部错误，请稍后重试！");
    }

    public static GatewayResp error(int code, String msg) {
        return new GatewayResp(code, msg);
    }

    public static GatewayResp error(String msg) {
        return new GatewayResp(500, msg);
    }

}
