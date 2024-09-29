package com.blackfire.aicloud.provider.listener;

import com.blackfire.aicloud.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Data
public class BaiduEventSourceListener extends EventSourceListener {

    private HttpServletResponse rp;
    private StringBuffer output = new StringBuffer();

    public BaiduEventSourceListener(HttpServletResponse response) {
        this.rp = response;
        this.rp.setStatus(200);
        this.rp.setContentType("text/event-stream");
        this.rp.setCharacterEncoding("UTF-8");
        this.rp.setHeader("Cache-Control", "no-cache");
        this.rp.setHeader("connection", "keep-alive");
        this.rp.setHeader("Access-Control-Allow-Origin", "*");  // 允许跨域
    }

    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("建立baidu sse连接...");
    }

    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        try {
            output.append(data);
            if (rp != null) {
                if ("\n".equals(data)) {
                    rp.getWriter().write("data:\n\n");
                    rp.getWriter().flush();
                } else {
                    String[] dataArr = data.split("\\n");
                    for (String s : dataArr) {
                        rp.getWriter().write("data:" + s + "\n");
                        rp.getWriter().flush();
                    }
                }
            }
        } catch (IOException e) {
            log.error("baidu  sse信息接收异常:", e);
            eventSource.cancel();
        }
    }

    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        if (Objects.isNull(response)) {
            log.error("baidu  sse连接异常:", t);
            eventSource.cancel();
            return;
        }
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
            log.error("baidu  sse连接异常data：{}，异常：", body.string(), t);
        } else {
            log.error("baidu  sse连接异常data：{}，异常：", response, t);
        }
        eventSource.cancel();
    }
}