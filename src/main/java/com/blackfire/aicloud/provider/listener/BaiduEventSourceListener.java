package com.blackfire.aicloud.provider.listener;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Data
public class BaiduEventSourceListener extends EventSourceListener {

    private HttpServletResponse rp;
    private StringBuffer output = new StringBuffer();
    @Getter
    private CountDownLatch countDownLatch = new CountDownLatch(1);

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
                rp.getWriter().write("data:" +  JSON.parseObject(data).getString("result"));
                rp.getWriter().flush();
            }
        } catch (IOException e) {
            log.error("baidu  sse信息接收异常:", e);
            eventSource.cancel();
            countDownLatch.countDown();
        }
    }

    @Override
    public void onClosed(@NotNull EventSource eventSource) {
        super.onClosed(eventSource);
        log.info("baidu  sse连接关闭");
        countDownLatch.countDown();
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
        countDownLatch.countDown();
    }
}